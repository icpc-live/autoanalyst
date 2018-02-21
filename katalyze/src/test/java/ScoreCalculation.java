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
		problemA = new Problem("1", "Problem A", "A");
		problemB = new Problem("2", "Problem B", "B");

		contest = new Contest();
		contest.addProblem(problemA);
		contest.addProblem(problemB);
		teamA = contest.registerTeam("1", "Foo");
	}

	private String makeSubmissionId() {
		return Integer.toString(submissionId++);
	}

	private void fail(Problem problem, int time) {
		InitialSubmission fakeInitialSubmission = new InitialSubmission(makeSubmissionId(), time, teamA, problem, language);
		teamA.submit(fakeInitialSubmission, "judgement_"+fakeInitialSubmission.id, problem, time, "WA", false, true);
	}
	
	private void compilationError(Problem problem, int time) {
		InitialSubmission fakeInitialSubmission = new InitialSubmission(makeSubmissionId(), time, teamA, problem, language);
		teamA.submit(fakeInitialSubmission, "judgement_"+fakeInitialSubmission.id, problem, time, "CE", false, false);
	}
	
	private void solve(Problem problem, int time) {
		InitialSubmission fakeInitialSubmission = new InitialSubmission(makeSubmissionId(), time, teamA, problem, language);
		teamA.submit(fakeInitialSubmission, "judgement_"+fakeInitialSubmission.id, problem, time, "AC", true, false);
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
		
		assertScore(2*Submission.CostOfFailedSubmission+70+90, problemA, problemB);
	}
	
	@Test public void failuresAfterSolutionDontCount() {
		solve(problemA, 70);
		fail(problemA, 99);
		
		assertScore(70, problemA);
	}
	
	
}
