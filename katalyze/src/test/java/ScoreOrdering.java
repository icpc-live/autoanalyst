package tests;


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.*;

import java.util.*;


public class ScoreOrdering extends tests.ContestSimulatorTest {

	
	private Set<Problem> problems(Problem... problems) {
		HashSet<Problem> target = new HashSet<Problem>();
		for (Problem p : problems) {
			target.add(p);
		}
		return target;
	}
	
	@Test
	public void mostSolvedWins() {
		ScoreTableComparer comparator = new ScoreTableComparer();
		InitContest(3,2);
		Map<Problem, ProblemJudgements> noSubmissions = new HashMap<Problem, ProblemJudgements>();
		
		Score scoreA = new Score(teams[0], 100, problems(problems[0]), noSubmissions);
		Score scoreB = new Score(teams[1], 130, problems(problems[0], problems[1]), noSubmissions);

		Assertions.assertEquals(1, comparator.compare(scoreA,scoreB));

	}
	
	@Test
	public void allTeamsHaveSameScoreAndRankWhenContestStarts() {
		InitContest(8,10);

		Standings standings = contest.getStandings();
		
		ScoreTableEntry scoreOfPreviousTeam = null;
		int rankOfPreviousTeam = -1;
		for (ScoreTableEntry score : standings) {
			if (scoreOfPreviousTeam != null) {
				assertEquals(scoreOfPreviousTeam, score);
			}
			int rank = standings.rankOf(score.getTeam());
			
			if (rankOfPreviousTeam != -1) {
				assertEquals(rankOfPreviousTeam, rank);
			}
			scoreOfPreviousTeam = score;
			rankOfPreviousTeam = rank;
		}
	}
	
	
	@Test
	public void sameScoreGivesSameRank() {
		InitContest(8, 100);
		
		Accepted(teams[0], problems[0], 99);
		Accepted(teams[1], problems[0], 100);
		Accepted(teams[2], problems[0], 100);
		Accepted(teams[3], problems[0] ,101);
		
		Standings standings = contest.getStandings();
		
		assertEquals(1, standings.rankOf(teams[0]));
		assertEquals(2, standings.rankOf(teams[1]));
		assertEquals(2, standings.rankOf(teams[2]));
		assertEquals(4, standings.rankOf(teams[3]));
		
	}
	
	@Test
	public void sameScoreMeansLastSubmissionCounts() {
		InitContest(8, 2);
		
		Accepted(teams[0], problems[0],5);
		Accepted(teams[1], problems[0],30);
		Accepted(teams[1], problems[1],50);
		Accepted(teams[0], problems[1],75);
		
		Standings standings = contest.getStandings();
		
		assertEquals(1, standings.rankOf(teams[1]));
		assertEquals(2, standings.rankOf(teams[0]));		
	}
	
	

}
