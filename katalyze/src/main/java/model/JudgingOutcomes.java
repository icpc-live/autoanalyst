package model;

import java.util.HashMap;

public class JudgingOutcomes {
	
	HashMap<String, InitialSubmission> initialSubmissions = new HashMap<String, InitialSubmission>();
	HashMap<InitialSubmission, TestCaseExecution> failedCase = new HashMap<InitialSubmission, TestCaseExecution>();
	
	public void newSubmission(InitialSubmission sub) {
		initialSubmissions.put(sub.id, sub);
	}
	
	public InitialSubmission getSubmission(String id) {
		return initialSubmissions.get(id);
	}
	
	public void testCaseRun(TestCaseExecution testCase) {
		if (!testCase.solved) {
			failedCase.put(testCase.submission, testCase);
		}
	}
	
	public TestCaseExecution getFailureInfo(InitialSubmission sub) {
		return failedCase.get(sub);
	}
		
}
