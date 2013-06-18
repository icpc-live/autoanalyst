package tests;

import java.io.StringReader;

import junit.framework.Assert;

import model.Analyzer;
import model.Contest;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import katalyzeapp.ConfigReader;


public class ConfigReadingTests {
	
	String testConfigWithGraphs = "charts.enable = true";
	String emptyConfig = "";
	
	
	private Analyzer getAnalyzerFromConfig(String config) throws ConfigurationException {
		ConfigReader configReader = new ConfigReader(new StringReader(config));
		Contest dummyContest = new Contest();
		Analyzer analyzer = dummyContest.getAnalyzer();
		configReader.SetupAnalyzer(dummyContest, analyzer, null);
		return analyzer;
		
	}
	
	@Test public void readsAProperty() throws Exception {

		Analyzer analyzer = getAnalyzerFromConfig(testConfigWithGraphs);
		Assert.assertEquals(2, analyzer.getOutputHooks().size());
		
	}
	
	@Test public void emptyConfigProducesNoGraphs() throws Exception {
		Analyzer analyzer = getAnalyzerFromConfig(emptyConfig);
		Assert.assertEquals(0, analyzer.getOutputHooks().size());
		
	}

}
