package model;

import stats.LanguageStats;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

public class Contest {
	final List<Problem> problems;
	final List<Team> teams;
	final Analyzer analyzer;
	final List<Submission> submissions;
	final List<Language> languages;
	final LanguageStats stats;
	double contestTime = 0;
	int lengthInMinutes = 300;
	
	public Contest() {
		this.problems = new ArrayList<Problem>();
		this.teams = new ArrayList<Team>();
		this.analyzer = new Analyzer(this, 0);
		this.languages = new ArrayList<Language>();
		this.submissions = new ArrayList<Submission>();
		this.stats = new LanguageStats();
		
		analyzer.addRule(stats.submissionsPerLanguage);
	}
	
	public Team registerTeam(int teamNumber, String teamName) {
		Team newTeam = new Team(this,teamNumber, teamName, teamName);
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
		problems.add(newProblem);
	}
	
	public void addLanguage(Language language) {
		languages.add(language);
		stats.submissionsPerLanguage.addLanguage(language.getName());
	}

	public List<Problem> getProblems() {
		return problems;
	}

	public void addTeam(Team newTeam) {
		teams.add(newTeam);
	}
	
	public Team getTeam(int teamNumber) throws InvalidKeyException {
		for (Team candidate : teams) {
			if (teamNumber == candidate.getTeamNumber()) {
				return candidate;
			}
		}
		throw new InvalidKeyException(String.format("%n is not a known team", teamNumber));
	}

	public Problem getProblem(String problemId) throws InvalidKeyException {
		for (Problem problem : problems) {
			if (problemId.equals(problem.getId())) {
				return problem;
			}
		}
		throw new InvalidKeyException(String.format("%s is not a known problem", problemId));
	}

	public Problem getProblemByAbbreviation(String problemId) throws InvalidKeyException {
		for (Problem problem : problems) {
			if (problemId.equalsIgnoreCase(problem.getLetter())) {
				return problem;
			}
		}
		throw new InvalidKeyException(String.format("%s is not a known problem", problemId));
	}	
	
	

}
