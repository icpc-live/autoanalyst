package teamscore;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import web.StaticWebDocument;
import web.WebPublisher;

import model.Contest;
import model.FakeScore;
import model.OutputHook;
import model.Problem;
import model.Score;
import model.ScoreTableComparer;
import model.ScoreTableEntry;
import model.Standings;
import model.Team;

public class ExtendedScoreDump implements OutputHook {
	static final Logger log = Logger.getLogger(ExtendedScoreDump.class);

	final Contest contest;
	final WebPublisher publisherTarget;
	final static ScoreTableComparer comparator = new ScoreTableComparer();
	
	class ScoreDumper {
		Standings standings;
		int minutesFromStart;
		ArrayList<Score> scoresAbove = new ArrayList<Score>();
		
		
		public ScoreDumper(Standings standings, int minutesFromStart) {
			this.standings = standings;
			this.minutesFromStart = minutesFromStart;
		}
		
		public JSONObject DumpScore(Score score) {
			
			Team team = score.getTeam();
			
			JSONArray problems = new JSONArray();
			for (Problem p: contest.getProblems()) {
				boolean isSolved = score.isSolved(p);
				JSONObject problemInfo = new JSONObject()
					.element("id", p.getLetter())
					.element("solved", isSolved)
					.element("attempts", score.submissionCount(p))
					.element("time", score.scoreContribution(p));
				if (!isSolved) {
					ScoreTableEntry fake = FakeScore.PretendProblemSolved(score, p, minutesFromStart);
					JSONObject potential = calcFictiousRank(scoresAbove, fake);
					problemInfo = problemInfo.element("potential", potential);
				}
				problems.add(problemInfo);
			}
			
		
			JSONObject target = new JSONObject()
				.element("rank", standings.rankOf(team))
				.element("team", new JSONObject()
					.element("id", team.getTeamNumber())
					.element("tag", team.toString())
					.element("name", team.getName()))
				.element("nSolved", score.getNumberOfSolvedProblems())
				.element("totalTime", score.getTimeIncludingPenalty())
				.element("problems", problems);
			
			return target;
		}
		
		private JSONArray getProblems(Contest contest) {
			JSONArray result = new JSONArray();
			
			for (Problem p : contest.getProblems()) {
				JSONObject problemInfo = new JSONObject()
					.element("tag", p.getLetter())
					.element("name", p.getName());
				result.add(problemInfo);
			}
			
			return result;
		}
		
		private JSONObject getContestInfo(Contest contest) {
			return new JSONObject()
				.element("length", contest.getLengthInMinutes())
				.element("problems", getProblems(contest))
				.element("submissions", contest.getSubmissionCount());
		}
		
		public String execute() {
			scoresAbove.clear();
			
			JSONArray resultArray = new JSONArray();

			for (Score score : standings) {
				scoresAbove.add(score);
				resultArray.add(DumpScore(score));
			}

			JSONObject contestInfo = getContestInfo(standings.getContest());			
			
			JSONObject contestStatus = new JSONObject()
				.element("scoreBoard", resultArray)
				.element("contestInfo", contestInfo);
			
			return contestStatus.toString();
		}

		private JSONObject calcFictiousRank(ArrayList<Score> scoresAbove,
				ScoreTableEntry fake) {
			
			JSONObject target = new JSONObject();
			int fakeIndex = scoresAbove.size()-1;
			
			while (fakeIndex>=0 && comparator.compare(fake, scoresAbove.get(fakeIndex))<=0) {
				fakeIndex--;
			}
			int margin = -1;
			target.element("rank", fakeIndex+2);
			if (fakeIndex < scoresAbove.size()-1) {
				ScoreTableEntry next = scoresAbove.get(fakeIndex+1);
				if (next.getNumberOfSolvedProblems() == fake.getNumberOfSolvedProblems()) {
					margin = next.getTimeIncludingPenalty() - fake.getTimeIncludingPenalty();
					target.element("before", margin);
				}
			}
			
			return target;
		}
		
		
	}
	
	public ExtendedScoreDump(Contest contest, WebPublisher target) {
		this.contest = contest;
		this.publisherTarget = target;
	}
	
	
	@Override
	public void execute(int minutesFromStart) {
		log.debug("publishing Standings... " + minutesFromStart);
		int submissionsAtTime = contest.getSubmissionsAtTime(minutesFromStart);
				
		ScoreDumper scoreDumper = new ScoreDumper(contest.getStandings(submissionsAtTime), minutesFromStart);
		String scoreTable = scoreDumper.execute();
		
		StaticWebDocument scoreDoc = new StaticWebDocument("application/json", scoreTable);
		publisherTarget.publish("/Standings", scoreDoc);
		publisherTarget.publish(String.format("/Standings.%03d", minutesFromStart), scoreDoc);
		
	}
	
	

}
