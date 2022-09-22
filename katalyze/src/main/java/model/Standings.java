package model;

import java.util.*;

public class Standings implements Iterable<Score> {
	static Comparator<ScoreTableEntry> comparator = new ScoreTableComparer();
	
	private Contest contest;
	private long contestTimeMillis;
	private List<Score> scores;

	@Override
	public Iterator<Score> iterator() {
		return scores.iterator();
	}

	public Standings(Contest contest, Collection<Score> teamScores, long contestTimeMillis) {
		this.contest = contest;
		this.scores = new ArrayList<Score>(teamScores);
		this.contestTimeMillis = contestTimeMillis;
		Collections.sort(scores, comparator);
	}
	
	public Score scoreOf(Team team) {
		// If team is hidden, score will always be zero
		if (team.isHidden()) {
			return new Score(team, 0, new HashSet<>(), new HashMap<>());
		}


		for (Score s : scores) {
			if (s.getTeam() == team) {
				return s;
			}
		}
		throw new AssertionError(String.format("%s is not a known team", team));
	}

	public boolean isNothingSolved() {
		return scores.get(0).solvedProblemCount() == 0;
	}

	public long getContestTimeMillis() {
		return contestTimeMillis;
	}

	public Contest getContest() {
		return this.contest;
	}

	public int rankOf(Team team) {
		if (team.isHidden()) {
			return Integer.MAX_VALUE;
		}

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
