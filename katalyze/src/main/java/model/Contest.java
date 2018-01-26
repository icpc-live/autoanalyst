package model;

import stats.LanguageStats;

import java.security.InvalidKeyException;
import java.util.*;

public class Contest {
	final Map<String, Problem> problems;
	final Map<String, JudgementType> judgementTypes;
	final List<Team> teams;
	final Analyzer analyzer;
	final List<Submission> submissions;
	final List<Language> languages;
	final LanguageStats stats;
	double contestTime = 0;
	int lengthInMinutes = 300;
	private String name;
	private int penaltyTime = 20;
	
	public Contest() {
		this.problems = new TreeMap<>();
		this.judgementTypes = new HashMap<>();
		this.teams = new ArrayList<Team>();
		this.analyzer = new Analyzer(this, 0);
		this.languages = new ArrayList<Language>();
		this.submissions = new ArrayList<Submission>();
		this.stats = new LanguageStats();
		this.name = name;
		this.penaltyTime = penaltyTime;
		
		analyzer.addRule(stats.submissionsPerLanguage);
	}

	public void init(String name, int penaltyTime) {
		this.name = name;
		this.penaltyTime = penaltyTime;
	}
	
	public Team registerTeam(String teamId, String teamName) {
		Team newTeam = new Team(this,teamId, teamName, teamName);
		teams.add(newTeam);
		return newTeam;
	}
	
	public Team[] getTeams() {
		return teams.toArray(new Team[0]);
	}

	public Standings getStandings() {
		return getStandings(getSubmissionCount());
	}
	
	public Standings getStandings(int submissionCount) {
		List<Score> teamScores = new ArrayList<Score>();
		for (Team team : teams) {
			teamScores.add(team.getScore(submissionCount));
		}
		return new Standings(this, teamScores, submissionCount);
	}
	
	public int getSubmissionCount() {
		return submissions.size();
	}
	
	public void updateTime(double contestTime) {
		if (contestTime > this.contestTime) {
			this.contestTime = contestTime;
		}
	}
	
	public int getMinutesFromStart() {
		return (int) (contestTime / 60.0);
	}
	
	public int getSubmissionsAtTime(int minutes) {
		int count = 0;
		for (Submission s : submissions) {
			if (s.getMinutesFromStart()>minutes) {
				break;
			}
			count++;
		}
		return count;
	}
	
	public List<Submission> getSubmissions() {
		return submissions;
	}
	
	public int getLengthInMinutes() {
		return this.lengthInMinutes;
	}
	
	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public void processSubmission(Submission newSubmission) {
		submissions.add(newSubmission);
		analyzer.notifySubmission(newSubmission);
	}
	
	public void addProblem(Problem newProblem) {
		problems.put(newProblem.getId(), newProblem);
	}

	public void addJudgementType(JudgementType judgementType) {
		judgementTypes.put(judgementType.getId(), judgementType);
	}

	public JudgementType getJudgementType(String id) {
		return judgementTypes.get(id);
	}
	
	public void addLanguage(Language language) {
		languages.add(language);
		stats.submissionsPerLanguage.addLanguage(language.getName());
	}

	public Collection<Problem> getProblems() {
		return problems.values();
	}

	public void addTeam(Team newTeam) {
		teams.add(newTeam);
	}
	
	public Team getTeam(String teamNumber) throws InvalidKeyException {
		for (Team candidate : teams) {
			if (teamNumber.equals(candidate.getTeamId())) {
				return candidate;
			}
		}
		throw new InvalidKeyException(String.format("%s is not a known team", teamNumber));
	}

	public Problem getProblem(String problemId) throws InvalidKeyException {
		if (problems.containsKey(problemId)) {
			return problems.get(problemId);
		} else {
			throw new InvalidKeyException(String.format("%s is not a known problem", problemId));
		}
	}

	public Problem getProblemByAbbreviation(String abbrev) throws InvalidKeyException {
		for (Map.Entry<String, Problem> problem : problems.entrySet()) {
			if (abbrev.equalsIgnoreCase(problem.getValue().getLetter())) {
				return problem.getValue();
			}
		}
		throw new InvalidKeyException(String.format("%s is not a known problem", abbrev));
	}	
	
	

}
