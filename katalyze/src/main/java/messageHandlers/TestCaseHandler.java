package messageHandlers;

import org.apache.log4j.Logger;

import model.Analyzer;
import model.InitialSubmission;
import model.TestCaseExecution;
import legacyfeed.SimpleMessage;

public class TestCaseHandler extends SingleMessageHandler {
	static Logger logger = Logger.getLogger(TestCaseHandler.class);

	public TestCaseHandler() {
		super("testcase");
	}
	
	/*
	<testcase>
	 <i>3</i>
	 <judged>True</judged>
	 <judgement_id>1</judgement_id>
	 <n>35</n>
	 <result>AC</result>
	 <run-id>1</run-id>
	 <solved>True</solved>
	 <time>711.83203</time>
	 <timestamp>1337242312.06</timestamp>
	</testcase>
	*/	
	
	@Override
	public void process(SimpleMessage message) {
		
		String submissionId = message.get("run-id");
		int caseNumber = message.getInt("i");
		int totalCaseCount = message.getInt("n");
		String result = message.get("result");
		double time = message.getDouble("time");
		boolean solved = message.getBool("solved");
		
		Analyzer analyzer = contest.getAnalyzer();
		InitialSubmission submission = analyzer.submissionById(submissionId);
		if (submission == null) {
			logger.error(String.format("Unable to find judgement %n, which seems to be executed test cases on. Ignoring outcome!", submissionId));
		}
		TestCaseExecution outcome = new TestCaseExecution(submission, caseNumber, totalCaseCount, time, solved, result);
		
		analyzer.testCaseExecuted(outcome);
		
		
	}

}
