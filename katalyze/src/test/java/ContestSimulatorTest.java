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
			contest.registerTeam(Integer.toString(i), String.format("Team %d", i), null);
		}
		
		teams = contest.getTeams();
		problems = contest.getProblems().toArray(new Problem[0]);
	}
		
	
	public static void Accepted(Team team, Problem problem, int minutesFromStart) {
		String subId =Integer.toString(submissionId++);

		int contestTime =  minutesFromStart*60000;

		InitialSubmission newSubmission = new InitialSubmission(subId, team, problem, "Fortran", contestTime);
		team.freshSubmission(newSubmission);

		team.submit(newSubmission, contestTime, subId, problem, "AC", true, false);
	}
	
	public static void WrongAnswer(Team team, Problem problem, int minutesFromStart) {
		String subId =Integer.toString(submissionId++);

		int contestTime =  minutesFromStart*60000;

		InitialSubmission newSubmission = new InitialSubmission(subId,team, problem, "Fortran", contestTime);

		team.freshSubmission(newSubmission);

		team.submit(newSubmission, contestTime, subId, problem,"WA", false, true);
	}	

}
