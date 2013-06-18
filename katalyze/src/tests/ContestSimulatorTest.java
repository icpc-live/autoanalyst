package tests;

import model.Contest;
import model.Problem;
import model.Team;

public abstract class ContestSimulatorTest {
	protected Contest contest;
	protected Problem[] problems;
	protected Team[] teams;

	protected void InitContest(int nProblems, int nTeams) {
		
		contest = new Contest();
		for (int i=0; i<nProblems; i++) {
			Problem p = new Problem(i+1, String.format("Problem %n", i));
			contest.addProblem(p);
		}
		
		for (int i=0; i<nTeams; i++) {
			contest.registerTeam(i, String.format("Team %d", i));
		}
		
		teams = contest.getTeams();
		problems = contest.getProblems().toArray(new Problem[0]);
	}
		
	
	public static void Accepted(Team team, Problem problem, int minutesFromStart) {
		team.submit(problem, minutesFromStart, "AC", true, false, "unknown");
	}
	
	public static void WrongAnswer(Team team, Problem problem, int minutesFromStart) {
		team.submit(problem, minutesFromStart, "WA", false, true, "unknown");
	}	

}
