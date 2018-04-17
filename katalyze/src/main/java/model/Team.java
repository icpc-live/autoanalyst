package model;

public class Team implements ApiEntity {
	private final String name;
	private final String teamId;
	private final TeamProgress progress;
	private final Contest contest;
	private final String shortName;
	private final Organization organization;


	public Team(Contest contest, String teamId, String name, String shortName, Organization organization) {
		this.contest = contest;
		this.teamId = teamId;
		this.name = name;
		this.shortName = shortName;
		this.progress = new TeamProgress(this);
		this.organization = organization;
	}

	@Override
	public String getId() {
		return teamId;
	}
	
	@Override
	public String toString() {
		return String.format("#t%s", teamId);
	}
	
	public Judgement submit(InitialSubmission initialSubmission, int judgementContestTime, String judgementId, Problem problem, String judgement, Boolean accepted, Boolean penalty) {
		
		Analyzer analyzer = contest.getAnalyzer();
		TestCaseExecution judgeOutcome = analyzer.getFailureInfo(initialSubmission);
		
		Judgement newSubmission = new Judgement(initialSubmission, judgementContestTime, judgementId, this, problem, judgement, accepted, penalty, judgeOutcome);
		contest.processSubmission(newSubmission);
		
		return newSubmission;
	}


	public boolean registerJudgement(Judgement judgement) {
		return progress.register(judgement);
	}
	
	public void freshSubmission(InitialSubmission submission) {
		progress.registerInitialSubmission(submission);
		contest.getAnalyzer().freshSubmission(submission);
	}
	
	public Score getCurrentScore() {
		return progress.calculateScore();
	}
	
	public Contest getContest() {
		return this.contest;
	}
	
	public String languageFor(Problem problem) {
		return progress.languageFor(problem);
	}
	
	public String getMainLanguage() {
		return progress.getMainLanguage();
	}

	public String getName() {
		return this.name;
	}

	public String getShortName() {
		return this.shortName;
	}

	public Organization getOrganization() { return this.organization; }
}
