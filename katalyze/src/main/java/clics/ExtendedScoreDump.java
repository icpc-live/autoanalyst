package clics;

import java.util.ArrayList;

import net.sf.json.JSON;
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
import model.StandingsPublisher;
import model.Team;

public class ExtendedScoreDump implements OutputHook, StandingsPublisher {
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

		public JSONObject dumpScore(Score score) {

			Team team = score.getTeam();

			JSONArray problems = new JSONArray();

			int place = scoresAbove.size();
			for (Problem p : contest.getProblems()) {
				boolean isSolved = score.isSolved(p);
				JSONObject problemInfo = new JSONObject()
					.element("label", p.getLabel())
					.element("num_judged", score.submissionCount(p))
// FIXME:			.element("num_pending", score.submissionCount(p))
					.element("solved", isSolved)
					.element("time", score.scoreContribution(p));

                int lastSubmissionTime = score.lastSubmissionTime(p);
                if (lastSubmissionTime != 0) {
                    problemInfo = problemInfo.element("lastUpd", lastSubmissionTime);
                }

				if (!isSolved) {
					ScoreTableEntry fake = FakeScore.PretendProblemSolved(score, p, minutesFromStart);
					JSONObject potential = new JSONObject();
					place = calcFictiousRank(scoresAbove, fake, place, potential);
					problemInfo = problemInfo.element("potential", potential);
				}
				String language = team.languageFor(p);
				if (language != null) {
					problemInfo = problemInfo.element("lang", language);
				}
				problems.add(problemInfo);
			}


			JSONObject target = new JSONObject()
				.element("rank", standings.rankOf(team))
				.element("team", team.getId())
				.element("main_lang", team.getMainLanguage())
				.element("score", new JSONObject()
					.element("num_solved", score.getNumberOfSolvedProblems())
					.element("total_time", score.getTimeIncludingPenalty()))
				.element("problems", problems);

			return target;
		}

		private JSONArray getProblems(Contest contest) {
			JSONArray result = new JSONArray();

			for (Problem p : contest.getProblems()) {
				JSONObject problemInfo = new JSONObject()
					.element("tag", p.getLabel())
					.element("name", p.getNameAndLabel());
				result.add(problemInfo);
			}

			return result;
		}

		private JSONObject getContestInfo(Contest contest) {
			return new JSONObject()
				.element("length", contest.getLengthInMinutes())
				.element("problems", getProblems(contest))
				.element("submissions", contest.getSubmissionCount())
				.element("time", contest.getMinutesFromStart());
		}

		public JSONArray execute() {
			scoresAbove.clear();
			boolean isFirstScore = true;

			JSONArray resultArray = new JSONArray();
			ArrayList<JSONObject> jsonScores = new ArrayList<JSONObject>();

			for (Score score : standings) {
				scoresAbove.add(score);
				JSONObject scoreRow = dumpScore(score);
				if (isFirstScore) {
					scoreRow.put("contestTime", contest.getMinutesFromStart());
					isFirstScore = false;
				}
				jsonScores.add(scoreRow);
			}

			resultArray.addAll(jsonScores);

			return resultArray;
		}

		private int calcFictiousRank(ArrayList<Score> scoresAbove,
									 ScoreTableEntry fake, int startFrom, JSONObject result) {

			int fakeIndex = startFrom;

			while (fakeIndex > 0 && comparator.compare(fake, scoresAbove.get(fakeIndex - 1)) <= 0) {
				fakeIndex--;
			}
			while (fakeIndex < scoresAbove.size() && comparator.compare(fake, scoresAbove.get(fakeIndex)) > 0) {
				fakeIndex++;
			}
			int margin = -1;
			result.element("rank", fakeIndex + 1);
			if (fakeIndex < scoresAbove.size()) {
				ScoreTableEntry next = scoresAbove.get(fakeIndex);
				if (next.getNumberOfSolvedProblems() == fake.getNumberOfSolvedProblems()) {
					margin = next.getTimeIncludingPenalty() - fake.getTimeIncludingPenalty();
					result.element("before", margin);
				}
			}
			return fakeIndex;
		}


	}

	public ExtendedScoreDump(Contest contest, WebPublisher target) {
		this.contest = contest;
		this.publisherTarget = target;
	}


	private JSONObject teamAsJson(Team team) {
		JSONObject target = new JSONObject();
		target.put("id", team.getId());
		target.put("name", team.getName());

		return target;
	}

	public JSONArray getAllTeams() {
		JSONArray target = new JSONArray();
		Team[] allTeams = contest.getTeams();
		for (Team team : allTeams) {
			target.add(teamAsJson(team));
		}
		return target;
	}

	public void publishStandings() {
		execute(contest.getMinutesFromStart());
	}


	@Override
	public void execute(int minutesFromStart) {
		log.debug("preparing Standings... ");

		ScoreDumper scoreDumper = new ScoreDumper(contest.getStandings(), minutesFromStart);
		JSON scoreTable = scoreDumper.execute();

		log.debug("publishing Standings... ");

		publisherTarget.publish("/scoreboard", new StaticWebDocument(scoreTable));
		publisherTarget.publish("/teams", new StaticWebDocument(getAllTeams()));

	}



}
