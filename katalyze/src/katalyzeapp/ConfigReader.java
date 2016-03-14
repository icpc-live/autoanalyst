package katalyzeapp;

import icat.AnalystMessageSource;
import io.EventFeedFile;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;

import config.TwitterConfig;
import rules.*;
import clics.ExtendedScoreDump;
import web.EventFeedStreamer;
import web.FileWebPublisher;
import web.WebPublisher;
import charts.ChartDumperHook;
import messageHandlers.ContestMessages;
import messageHandlers.PassthroughHandler;
import model.Analyzer;
import model.Contest;
import model.DatabaseNotificationTarget;
import model.ModelDumperHook;
import model.ShellNotificationTarget;
import model.TwitterNotificationTarget;
import model.WebNotificationTarget;

public class ConfigReader {
	static Logger logger = Logger.getLogger(ConfigReader.class);
	
	BaseConfiguration config;
	

	public ConfigReader(Reader in) throws ConfigurationException {
		
		PropertiesConfiguration loadedConfig = new PropertiesConfiguration();
		loadedConfig.load(in);
		this.config = loadedConfig;
	}
	
	public ConfigReader(BaseConfiguration config) {
		this.config = config;
	}
	

	private void setupDatabaseNotifier(Analyzer analyzer) {
		if (!config.getBoolean("db.enable", false)) {
			return;
		}
		
		DatabaseNotificationConfig dbConfig = new DatabaseNotificationConfig(
				config.getString("db.driver", "com.mysql.jdbc.Driver"),
				config.getString("db.connection"));
		
		try {
			logger.info("Enabling database notifier");

			if (config.getBoolean("db.exportMessages",true)) {
				DatabaseNotificationTarget notifier = new DatabaseNotificationTarget(dbConfig);
				notifier.suppressUntil(config.getInt("notifications.suppressUntil", 0));
			
				StandingsUpdatedEvent rule = new AllSubmissions(dbConfig);
				analyzer.addNotifier(notifier);
				analyzer.addRule(rule);				
			}
			
			AnalystMessageSource msgSource = new AnalystMessageSource(dbConfig);
			msgSource.open();
			analyzer.setAnalystMsgSource(msgSource);

		}
		catch (Exception e) {
			logger.error(String.format("Failed to add Database Notifier to analyzer. Error: %s", e));
		}		
	}
	
	private void setupTwitterNotifier(Analyzer analyzer) {
		if (!config.getBoolean("twitter.enable", false)) {
			return;
		}
		
		logger.info("will export to twitter");
		
		TwitterConfig twitterConfig = new TwitterConfig(
				config.getStringArray("twitter.oAuthConsumer"),
				config.getStringArray("twitter.accessToken"),
				config.getString("twitter.hashtag")
				);

		TwitterNotificationTarget twitterNotifier = new TwitterNotificationTarget(twitterConfig);
		twitterNotifier.suppressUntil(config.getInt("notifications.suppressUntil", 0));

		analyzer.addNotifier(twitterNotifier);
	}
	
	private boolean ruleEnabled(String ruleName) {
		return (config.getBoolean("rule."+ruleName+".enable", false));
	}
	
	private boolean featureEnabled(String featureName) {
		return (config.getBoolean(featureName+".enable", false));
	}
	
	
	private void addRuleIfEnabled(Analyzer analyzer, String ruleName, StateComparingRuleBase newRule) {
		if (!ruleEnabled(ruleName)) {
			return;
		}
		
		analyzer.addRule(newRule);
		String execTemplate = config.getString("rule."+ruleName+".exec", "");
		if (!"".equals(execTemplate)) {
			logger.info(String.format("Adding trigger on rule %s: %s", newRule, execTemplate));
			ShellNotificationTarget executer = new ShellNotificationTarget(execTemplate);
			executer.suppressUntil(config.getInt("notifications.suppressUntil", 0));
			newRule.addNotificationTarget(executer);
		}
	}
	
	private void setupRules(Analyzer analyzer) {
		addRuleIfEnabled(analyzer, "problemFirstSolved", new ProblemFirstSolved());
		addRuleIfEnabled(analyzer, "newLeader", new NewLeader(config.getInt("rule.newLeader.breakingRanks",4), config.getInt("rule.newLeader.ranks", 10)));
		addRuleIfEnabled(analyzer, "rejectedSubmission", new RejectedSubmission(config.getInt("rule.RejectedSubmission.ranks", 10)));
		addRuleIfEnabled(analyzer, "rankPredictor", new RankPredictor(config.getInt("rule.rankPredictor.ranks", 10)));
	}
	
	private void setupCharts(Contest contest, Analyzer analyzer) {
		if (config.getBoolean("charts.enable",false)) {
			logger.info("going to create charts");
			String targetDirectory = config.getString("charts.directory", "output");
			ChartDumperHook chartDumperHook = new ChartDumperHook(contest, new File(targetDirectory));
			analyzer.addOutputHook(chartDumperHook);
			analyzer.addOutputHook(new ModelDumperHook(contest, chartDumperHook));			
		}
	}
	
	private EventFeedFile setupOutputStream(Analyzer analyzer, ContestMessages messageHandlers) {
		if (featureEnabled("eventStream")) {
			String target = config.getString("eventStream.target");
		
			
			File f = new File(target);
			if(f.exists()) { 
				f.lastModified();
				File newFileName = new File(target+"."+Long.toString(f.lastModified()));
				f.renameTo(newFileName);
			}
			
			try {
				EventFeedFile outStream = new EventFeedFile(f);
				
				PassthroughHandler outgoingEventFeed = new PassthroughHandler(outStream);
				messageHandlers.add(outgoingEventFeed);
				analyzer.addNotifier(outgoingEventFeed);
				return outStream;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void setupWebPublisher(Contest contest, Analyzer analyzer, EventFeedFile augmentedEventFeed) {
		KatalyzerHttpHandler httpHandler;
		if (featureEnabled("web")) {
			int port = config.getInteger("web.port", 8099);
			boolean useCompression = config.getBoolean("web.compress", true);
			
			WebPublisher webPublisher = new WebPublisher(useCompression);
			
			httpHandler = new KatalyzerHttpHandler(contest, webPublisher, port);
		    
			httpHandler.addHandler(new EventFeedStreamer(augmentedEventFeed));
			
			analyzer.addOutputHook(new ExtendedScoreDump(contest, webPublisher));
			analyzer.addNotifier(new WebNotificationTarget(webPublisher));
			analyzer.manageLifeCycle(httpHandler);
		
		}
	}
	
	public void setupFilePublisher(Contest contest, Analyzer analyzer) {
		if (featureEnabled("file")) {
			String targetDirectory = config.getString("file.targetDirectory");
			
			try {
				FileWebPublisher publisher = new FileWebPublisher(targetDirectory);
				analyzer.addNotifier(new WebNotificationTarget(publisher));
			} catch (IOException e) {
				logger.error(String.format("Failed to initialize file publisher. Reason: %s", e));
			}
			
		}
	}
	
	
	
	public void SetupAnalyzer(Contest contest, Analyzer analyzer, ContestMessages messageHandlers) {
		EventFeedFile augmentedEventFeed = setupOutputStream(analyzer, messageHandlers);
		
		setupRules(analyzer);
		setupCharts(contest, analyzer);
		setupDatabaseNotifier(analyzer);
		setupTwitterNotifier(analyzer);
		setupWebPublisher(contest, analyzer, augmentedEventFeed);
		setupFilePublisher(contest, analyzer);
		

		
		
	}

}
