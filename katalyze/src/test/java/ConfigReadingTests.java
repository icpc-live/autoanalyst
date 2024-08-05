package tests;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import model.Analyzer;
import model.Contest;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.jupiter.api.Test;

import katalyzeapp.ConfigReader;


public class ConfigReadingTests {
	
	String testConfigWithGraphs = "katalyzer:\n"+
								  "  charts:\n"+
								  "    enable: true";
	String dummyConfig = "dummy: \n";

	String emptyConfig = "";
	
	
	private Analyzer getAnalyzerFromConfig(String config) throws ConfigurationException {
		Contest dummyContest = new Contest();
		Analyzer analyzer = dummyContest.getAnalyzer();

		if (config != null  && !config.isEmpty()) {
			ConfigReader configReader = new ConfigReader(new StringReader(config));
			configReader.SetupAnalyzer(dummyContest, analyzer, null);
		}

		return analyzer;
		
	}
	
	@Test
	public void readsAProperty() throws Exception {
		Analyzer analyzer = getAnalyzerFromConfig(testConfigWithGraphs);
		assertEquals(2, analyzer.getOutputHooks().size());
		
	}
	
	@Test
	public void dummyConfigProducesNoGraphs() throws Exception {
		Analyzer analyzer = getAnalyzerFromConfig(dummyConfig);
		assertEquals(0, analyzer.getOutputHooks().size());
	}

	@Test
	public void emptyConfigProducesNoGraphs() throws Exception {
		Analyzer analyzer = getAnalyzerFromConfig(emptyConfig);
		assertEquals(0, analyzer.getOutputHooks().size());
	}

}
