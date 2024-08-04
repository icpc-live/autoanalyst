package tests;

import model.TestNotifier;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import rules.*;
import model.*;

public class StateComparingRules extends tests.ContestSimulatorTest {

	TestNotifier notifier = new TestNotifier();
	
	@Test
	public void firstSolutionOfProblemNotifies() {
		InitContest(5, 10);
		Analyzer analyzer = contest.getAnalyzer();
		analyzer.addNotifier(notifier);
		analyzer.addRule(new ProblemFirstSolved());
		Accepted(teams[0], problems[0], 5);
		assertTrue(notifier.containsFragment("solve"));
	}
	
	@Test
	public void topThreeRankNotifications() {
		InitContest(10, 10);
		Analyzer analyzer = contest.getAnalyzer();
		analyzer.addNotifier(notifier);
		Accepted(teams[0], problems[0], 5);
		Accepted(teams[1], problems[1], 6);

		analyzer.addRule(new NewLeader(3, 8));
		Accepted(teams[5], problems[2], 8);		
		Accepted(teams[5], problems[1], 11);
		assertTrue(notifier.containsFragment("Team 5 now leads"));
		
	}
	
	
	

}
