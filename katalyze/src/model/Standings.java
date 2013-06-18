package model;

import java.util.*;

public class Standings implements Iterable<Score> {
	static Comparator<ScoreTableEntry> comparator = new ScoreTableComparer();
	
	private Contest contest;
	private List<Score> scores;
	private int submissionCount;

	@Override
	public Iterator<Score> iterator() {
		return scores.iterator();
	}
	
	public Standings(Contest contest, Collection<Score> teamScores, int submissionCount) {
		this.contest = contest;
		this.submissionCount = submissionCount;
		scores = new ArrayList<Score>(teamScores);
		Collections.sort(scores, comparator);
	}
	
	public Score scoreOf(Team team) {
		for (Score s : scores) {
			if (s.getTeam() == team) {
				return s;
			}
		}
		throw new AssertionError(String.format("%s is not a known team", team));
	}

	public Contest getContest() {
		return this.contest;
	}
	
	public int getSubmissionCount() {
		return this.submissionCount;
	}
	
	public int rankOf(Team team) {
		ScoreTableEntry previousScore = null;
		int rank = 0;
		
		for (int i=0; i<scores.size(); i++) {
			ScoreTableEntry thisScore = scores.get(i);
			if (!thisScore.equals(previousScore)) {
				rank = i+1;
			}
			
			if (team == thisScore.getTeam()) {
				return rank;
			}
			
			previousScore = thisScore;
		}
		throw new AssertionError(String.format("%s is not a recognized team", team));
	}
	
	public boolean isSolved(Problem problem) {
		for (Score s : scores) {
			if (s.solvedProblems.contains(problem)) {
				return true;
			}
		}
		return false;
	}

}
