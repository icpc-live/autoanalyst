package model;

public class TestCaseExecution {
	public final InitialSubmission submission;
	public final int caseNumber;
	public final int totalCaseCount;
	public final double time;
	public final boolean solved;
	public final String outcome;
	
	public TestCaseExecution(InitialSubmission submission, int caseNumber, int totalCaseCount, double time, boolean solved, String outcome) {
		this.submission = submission;
		this.caseNumber = caseNumber;
		this.totalCaseCount = totalCaseCount;
		this.time = time;
		this.solved = solved;
		this.outcome = outcome;
	}
}