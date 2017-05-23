package katalyzeapp;

import charts.ChartDumperHook;
import clics.ExtendedScoreDump;
import com.github.mbredel.commons.configuration.YAMLConfiguration;
import config.TwitterConfig;
import icat.AnalystMessageSource;
import io.EventFeedFile;
import messageHandlers.ContestMessages;
import messageHandlers.PassthroughHandler;
import model.Analyzer;
import model.Contest;
import model.DatabaseNotificationTarget;
import model.ModelDumperHook;
import model.ShellNotificationTarget;
import model.TwitterNotificationTarget;
import model.WebNotificationTarget;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import rules.AllSubmissions;
import rules.NewLeader;
import rules.ProblemFirstSolved;
import rules.RankPredictor;
import rules.RejectedSubmission;
import rules.StandingsUpdatedEvent;
import rules.StateComparingRuleBase;
import web.EventFeedStreamer;
import web.FileWebPublisher;
import web.WebPublisher;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

public class ConfigReader {
	static Logger logger = Logger.getLogger(ConfigReader.class);
	
	Configuration config;
	private DatabaseNotificationConfig dbConfig;


	public ConfigReader(Reader in) throws ConfigurationException {
		
		YAMLConfiguration loadedConfig = new YAMLConfiguration();
		loadedConfig.load(in);
		this.config = loadedConfig;
	}
	
	public ConfigReader(Configuration config) {
		this.config = config;
	}
	

	private void setupDatabaseNotifier(Analyzer analyzer) {
		if (!config.getBoolean("katalyzer.db.enable", false)) {
			return;
		}

		String connection = "jdbc:mysql://"
		                  + config.getString("database.host") + "/"
		                  + config.getString("database.name") + "?user="
		                  + config.getString("database.user") + "&password=";


		logger.info("Connecting to database: "+connection+"????");

		connection += config.getString("database.password");

		dbConfig = new DatabaseNotificationConfig(
				"com.mysql.jdbc.Driver", connection);
		
		try {
			logger.info("Enabling database notifier");

			if (config.getBoolean("katalyzer.db.exportMessages",true)) {
				DatabaseNotificationTarget notifier = new DatabaseNotificationTarget(dbConfig);
				notifier.suppressUntil(config.getInt("katalyzer.notifications.suppressUntil", 0));
			
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
		if (!config.getBoolean("katalyzer.twitter.enable", false)) {
			return;
		}
		
		logger.info("will export to twitter");
		
		TwitterConfig twitterConfig = new TwitterConfig(
				config.getStringArray("katalyzer.twitter.oAuthConsumer"),
				config.getStringArray("katalyzer.twitter.accessToken"),
				config.getString("katalyzer.twitter.hashtag"),
				config.getInt("katalyzer.notifications.suppressUntil", 0)
				);

		TwitterNotificationTarget twitterNotifier = new TwitterNotificationTarget(twitterConfig);
		analyzer.addNotifier(twitterNotifier);
	}
	
	private boolean ruleEnabled(String ruleName) {
		return (config.getBoolean("katalyzer.rule."+ruleName+".enable", false));
	}
	
	private boolean featureEnabled(String featureName) {
		return (config.getBoolean("katalyzer."+featureName+".enable", false));
	}
	
	
	private void addRuleIfEnabled(Analyzer analyzer, String ruleName, StateComparingRuleBase newRule) {
		if (!ruleEnabled(ruleName)) {
			return;
		}
		
		analyzer.addRule(newRule);
		String execTemplate = config.getString("katalyzer.rule."+ruleName+".exec", "");
		if (!"".equals(execTemplate)) {
			logger.info(String.format("Adding trigger on rule %s: %s", newRule, execTemplate));
			ShellNotificationTarget executer = new ShellNotificationTarget(execTemplate);
			executer.suppressUntil(config.getInt("katalyzer.notifications.suppressUntil", 0));
			newRule.addNotificationTarget(executer);
		}
	}
	
	private void setupRules(Analyzer analyzer) {
		addRuleIfEnabled(analyzer, "problemFirstSolved", new ProblemFirstSolved());
		addRuleIfEnabled(analyzer, "newLeader", new NewLeader(config.getInt("katalyzer.rule.newLeader.breakingRanks",4), config.getInt("katalyzer.rule.newLeader.ranks", 10)));
		addRuleIfEnabled(analyzer, "rejectedSubmission", new RejectedSubmission(config.getInt("katalyzer.rule.RejectedSubmission.ranks", 10)));
		addRuleIfEnabled(analyzer, "rankPredictor", new RankPredictor(config.getInt("katalyzer.rule.rankPredictor.ranks", 10)));
	}
	
	private void setupCharts(Contest contest, Analyzer analyzer) {
		if (config.getBoolean("katalyzer.charts.enable",false)) {
			logger.info("going to create charts");
			String targetDirectory = config.getString("katalyzer.charts.directory", "output");
			ChartDumperHook chartDumperHook = new ChartDumperHook(contest, new File(targetDirectory));
			analyzer.addOutputHook(chartDumperHook);
			analyzer.addOutputHook(new ModelDumperHook(contest, chartDumperHook));			
		}
	}
	
	private EventFeedFile setupOutputStream(Analyzer analyzer, ContestMessages messageHandlers) {
		if (featureEnabled("eventStream")) {
			String target = config.getString("katalyzer.eventStream.target");
		
			
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
			int port = config.getInteger("katalyzer.web.port", 8099);
			boolean useCompression = config.getBoolean("katalyzer.web.compress", true);
			
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
			String targetDirectory = config.getString("katalyzer.file.targetDirectory");
			
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

	public Connection getConnection() throws Exception {
		return dbConfig.createConnection();
	}
}
