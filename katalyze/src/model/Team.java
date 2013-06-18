package model;

public class Team {
	private final String name;
	private final int teamNumber;
	private final TeamProgress progress;
	private final Contest contest;

	
	public Team(Contest contest, int teamNumber, String name) {
		this.contest = contest;
		this.teamNumber = teamNumber;
		this.name = name;
		this.progress = new TeamProgress(this);
	}
	
	public int getTeamNumber() {
		return teamNumber;
	}
	
	@Override
	public String toString() {
		return String.format("#t%d", teamNumber);
	}
	
	public Submission submit(Problem problem, int minutesFromStart, String judgement, Boolean accepted, Boolean penalty, String language) {
		Submission newSubmission = new Submission(contest.getSubmissionCount(), this, minutesFromStart, problem, judgement, accepted, penalty, language);

		progress.register(newSubmission);
		contest.processSubmission(newSubmission);
		
		return newSubmission;
	}
	
	public void freshSubmission(Problem problem, int minutesFromStart, String language) {
		contest.getAnalyzer().freshSubmission(this, problem, minutesFromStart);
	}
	
	public Score getScore(int submissionCount) {
		return progress.calculateScore(submissionCount);
	}
	
	public Contest getContest() {
		return this.contest;
	}
	
	public ScoreTableEntry getScore() {
		return progress.calculateScore(contest.getSubmissionCount());
	}

	public String getName() {
		return this.name;
	}	
	

}
