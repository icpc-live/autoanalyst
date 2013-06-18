package model;

import java.security.InvalidKeyException;
import java.util.*;

public class Contest {
	final List<Problem> problems;
	final List<Team> teams;
	final Analyzer analyzer;
	final List<Submission> submissions;
	int lengthInMinutes = 300;
	
	public Contest() {
		this.problems = new ArrayList<Problem>();
		this.teams = new ArrayList<Team>();
		this.analyzer = new Analyzer(this, 184);
		this.submissions = new ArrayList<Submission>();
	}
	
	public Team registerTeam(int teamNumber, String teamName) {
		Team newTeam = new Team(this,teamNumber, teamName);
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

	public Standings getPreviousStandings() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void addProblem(Problem newProblem) {
		problems.add(newProblem);
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
	
	

}
