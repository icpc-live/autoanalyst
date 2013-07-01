package katalyzeapp;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;

import config.TwitterConfig;

import rules.*;
import teamscore.ExtendedScoreDump;
import web.FileWebPublisher;
import web.WebPublisher;

import charts.ChartDumperHook;

import messageHandlers.ContestMessages;
import messageHandlers.PassthroughHandler;
import model.Analyzer;
import model.Contest;
import model.DatabaseNotificationTarget;
import model.ModelDumperHook;
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

			DatabaseNotificationTarget notifier = new DatabaseNotificationTarget(dbConfig);
			StandingsUpdatedEvent rule = new AllSubmissions(dbConfig);
			
			analyzer.addNotifier(notifier);
			analyzer.addRule(rule);
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

			
		analyzer.addNotifier(new TwitterNotificationTarget(twitterConfig));
	}
	
	private boolean ruleEnabled(String ruleName) {
		return (config.getBoolean("rule."+ruleName+".enable", false));
	}
	
	private boolean featureEnabled(String featureName) {
		return (config.getBoolean(featureName+".enable", false));
	}
	
	private void setupRules(Analyzer analyzer) {
		if (ruleEnabled("problemFirstSolved")) {
			analyzer.addRule(new ProblemFirstSolved(analyzer));
		}
		
		if (ruleEnabled("newLeader")) {
			analyzer.addRule(new NewLeader(analyzer, config.getInt("rule.newLeader.ranks", 10)));
		}
		
		if (ruleEnabled("rejectedSubmission")) {
			analyzer.addRule(new RejectedSubmission(analyzer, config.getInt("rule.RejectedSubmission.ranks", 10)));
		}
		
		if (ruleEnabled("rankPredictor")) {
			analyzer.addRule(new RankPredictor(analyzer, config.getInt("rule.rankPredictor.ranks", 10)));
		}
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
	
	private void setupOutputStream(Analyzer analyzer, ContestMessages messageHandlers) {
		if (featureEnabled("eventStream")) {
			String target = config.getString("eventStream.target");
		
			
			File f = new File(target);
			if(f.exists()) { 
				f.lastModified();
				File newFileName = new File(target+"."+Long.toString(f.lastModified()));
				f.renameTo(newFileName);
			}
			
			try {
				PrintStream outputStream = new PrintStream(f, "UTF-8");
				
				PassthroughHandler outgoingEventFeed = new PassthroughHandler(outputStream);
				messageHandlers.add(outgoingEventFeed);
				analyzer.addNotifier(outgoingEventFeed);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	public void setupWebPublisher(Contest contest, Analyzer analyzer) {
		KatalyzerHttpHandler httpHandler;
		if (featureEnabled("web")) {
			int port = config.getInteger("web.port", 8099);
			boolean useCompression = config.getBoolean("web.compress", true);
			
			WebPublisher webPublisher = new WebPublisher(useCompression);
			
			httpHandler = new KatalyzerHttpHandler(contest, webPublisher, port);
			
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
		
		setupRules(analyzer);
		setupCharts(contest, analyzer);
		setupDatabaseNotifier(analyzer);
		setupTwitterNotifier(analyzer);
		setupWebPublisher(contest, analyzer);
		setupFilePublisher(contest, analyzer);
		
		setupOutputStream(analyzer, messageHandlers);	
		
		
	}

}
