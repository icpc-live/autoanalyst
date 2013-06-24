package model;

import java.util.HashMap;

public class JudgingOutcomes {
	
	HashMap<Integer, InitialSubmission> initialSubmissions = new HashMap<Integer, InitialSubmission>();
	HashMap<InitialSubmission, TestCaseExecution> failedCase = new HashMap<InitialSubmission, TestCaseExecution>();
	
	public void newSubmission(InitialSubmission sub) {
		initialSubmissions.put(sub.id, sub);
	}
	
	public InitialSubmission getSubmission(Integer id) {
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
