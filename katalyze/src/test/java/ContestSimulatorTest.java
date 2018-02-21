package tests;

import model.Contest;
import model.InitialSubmission;
import model.Problem;
import model.Team;

public abstract class ContestSimulatorTest {
	protected Contest contest;
	protected Problem[] problems;
	protected Team[] teams;
	protected static int submissionId = 0;

	protected void InitContest(int nProblems, int nTeams) {
		
		contest = new Contest();
		for (int i=0; i<nProblems; i++) {
			Problem p = new Problem(Integer.toString(i+1), String.format("Problem %n", i), String.format("P%n",i));
			contest.addProblem(p);
		}
		
		for (int i=0; i<nTeams; i++) {
			contest.registerTeam(Integer.toString(i), String.format("Team %d", i));
		}
		
		teams = contest.getTeams();
		problems = contest.getProblems().toArray(new Problem[0]);
	}
		
	
	public static void Accepted(Team team, Problem problem, int minutesFromStart) {
		String subId =Integer.toString(submissionId++);

		InitialSubmission newSubmission = new InitialSubmission(subId,minutesFromStart, team, problem, "Fortran");
		team.freshSubmission(newSubmission);

		team.submit(newSubmission, subId, problem, minutesFromStart, "AC", true, false);
	}
	
	public static void WrongAnswer(Team team, Problem problem, int minutesFromStart) {
		String subId =Integer.toString(submissionId++);
		InitialSubmission newSubmission = new InitialSubmission(subId,minutesFromStart, team, problem, "Fortran");

		team.freshSubmission(newSubmission);

		team.submit(newSubmission, subId, problem, minutesFromStart, "WA", false, true);
	}	

}
