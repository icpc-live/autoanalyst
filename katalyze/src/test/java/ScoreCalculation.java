package tests;

import model.*;

import org.junit.*;
import static org.junit.Assert.*;

public class ScoreCalculation {
	
	Team teamA;
	Problem problemA;
	Problem problemB;
	Contest contest;
	int submissionId = 0;
	String language = "java";
	
	@Before public void Setup() {
		problemA = new Problem("1", "Problem A", "A", "#FF0000");
		problemB = new Problem("2", "Problem B", "B", "#0000FF");

		contest = new Contest();
		contest.addProblem(problemA);
		contest.addProblem(problemB);
		teamA = contest.registerTeam("1", "Foo", null, new Group[0]);
	}

	private String makeSubmissionId() {
		return Integer.toString(submissionId++);
	}

	private void fail(Problem problem, int time) {
		InitialSubmission fakeInitialSubmission = new InitialSubmission(makeSubmissionId(), teamA, problem, language, time*60000);
		teamA.submit(fakeInitialSubmission, time*60000,"judgement_"+fakeInitialSubmission.id, problem,"WA", false, true);
	}
	
	private void compilationError(Problem problem, int time) {
		InitialSubmission fakeInitialSubmission = new InitialSubmission(makeSubmissionId(), teamA, problem, language, time*60000);
		teamA.submit(fakeInitialSubmission, time*60000,"judgement_"+fakeInitialSubmission.id, problem,"CE", false, false);
	}
	
	private void solve(Problem problem, int time) {
		InitialSubmission fakeInitialSubmission = new InitialSubmission(makeSubmissionId(), teamA, problem, language, time*60000);
		teamA.submit(fakeInitialSubmission, time*60000,"judgement_"+fakeInitialSubmission.id, problem,"AC", true, false);
	}
	
	private void assertScore(int score, Problem... problems) {
		
		ScoreTableEntry teamScore = teamA.getCurrentScore();
		
		assertEquals(score, teamScore.getTimeIncludingPenalty());
		assertEquals(problems.length, teamScore.getNumberOfSolvedProblems());
		
		for(Problem p : problems) {
			assertTrue(teamScore.isSolved(p));
		}
		
	}
	

	@Test public void noSolvedNoPoints() {
		fail(problemA, 20);
		fail(problemA, 194);
		
		assertScore(0);
	}
	
	@Test public void noFailedNoPenalty() {
		solve(problemA, 100);
		assertScore(100, problemA);
	}
	
	@Test public void noCompilationErrorGivesNoPenalty() {
		compilationError(problemA, 40);
		solve(problemA, 100);
		assertScore(100, problemA);
	}	
	
	@Test public void totalTimeIsSumOfIndividualResults() {
		fail(problemA, 50);
		fail(problemA, 61);
		solve(problemA, 70);
		solve(problemB, 90);
		
		assertScore(2* Judgement.CostOfFailedSubmission+70+90, problemA, problemB);
	}
	
	@Test public void failuresAfterSolutionDontCount() {
		solve(problemA, 70);
		fail(problemA, 99);
		
		assertScore(70, problemA);
	}
	
	
}
