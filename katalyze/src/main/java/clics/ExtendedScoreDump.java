package clics;

import java.util.ArrayList;

import com.google.gson.*;
import model.*;

import org.apache.log4j.Logger;

import web.StaticWebDocument;
import web.WebPublisher;

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

		public JsonObject dumpScore(Score score) {

			Team team = score.getTeam();

			JsonArray problems = new JsonArray();

			int place = scoresAbove.size();
			for (Problem p : contest.getProblems()) {
				boolean isSolved = score.isSolved(p);
				JsonObject problemInfo = new JsonObject();
				problemInfo.addProperty("problem_id", p.getId());
				problemInfo.addProperty("label", p.getLabel());
				problemInfo.addProperty("num_judged", score.submissionCount(p));
// FIXME:		problemInfo.addProperty("num_pending", score.submissionCount(p))
				problemInfo.addProperty("solved", isSolved);
				problemInfo.addProperty("time", score.scoreContribution(p));

                int lastSubmissionTime = score.lastSubmissionTime(p);
                if (lastSubmissionTime != 0) {
                    problemInfo.addProperty("lastUpd", lastSubmissionTime);
                }

				if (!isSolved) {
					ScoreTableEntry fake = FakeScore.PretendProblemSolved(score, p, minutesFromStart);
					JsonObject potential = new JsonObject();
					place = calcFictiousRank(scoresAbove, fake, place, potential);
					problemInfo.add("potential", potential);
				}
				String language = team.languageFor(p);
				if (language != null) {
					problemInfo.addProperty("lang", language);
				}
				problems.add(problemInfo);
			}


			JsonObject target = new JsonObject();
			target.addProperty("rank", standings.rankOf(team));
			target.addProperty("team_id", team.getId());
			target.addProperty("main_lang", team.getMainLanguage());
			JsonObject json_score = new JsonObject();
			json_score.addProperty("num_solved", score.getNumberOfSolvedProblems());
			json_score.addProperty("total_time", score.getTimeIncludingPenalty());
			target.add("score", json_score);
			target.add("problems", problems);

			return target;
		}

		private JsonArray getProblems(Contest contest) {
			JsonArray result = new JsonArray();

			for (Problem p : contest.getProblems()) {
				JsonObject problemInfo = new JsonObject();
				problemInfo.addProperty("tag", p.getLabel());
				problemInfo.addProperty("name", p.getNameAndLabel());
				result.add(problemInfo);
			}

			return result;
		}

		private JsonObject getContestInfo(Contest contest) {
			JsonObject result = new JsonObject();
			result.addProperty("length", contest.getLengthInMinutes());
			result.add("problems", getProblems(contest));
			result.addProperty("submissions", contest.getSubmissionCount());
			result.addProperty("time", contest.getMinutesFromStart());
			return result;
		}

		public JsonArray execute() {
			scoresAbove.clear();
			boolean isFirstScore = true;

			JsonArray resultArray = new JsonArray();

			for (Score score : standings) {
				scoresAbove.add(score);
				JsonObject scoreRow = dumpScore(score);
				if (isFirstScore) {
					scoreRow.addProperty("contestTime", contest.getMinutesFromStart());
					isFirstScore = false;
				}
				resultArray.add(scoreRow);
			}

			return resultArray;
		}

		private int calcFictiousRank(ArrayList<Score> scoresAbove,
									 ScoreTableEntry fake, int startFrom, JsonObject result) {

			int fakeIndex = startFrom;

			while (fakeIndex > 0 && comparator.compare(fake, scoresAbove.get(fakeIndex - 1)) <= 0) {
				fakeIndex--;
			}
			while (fakeIndex < scoresAbove.size() && comparator.compare(fake, scoresAbove.get(fakeIndex)) > 0) {
				fakeIndex++;
			}
			int margin = -1;
			result.addProperty("rank", fakeIndex + 1);
			if (fakeIndex < scoresAbove.size()) {
				ScoreTableEntry next = scoresAbove.get(fakeIndex);
				if (next.getNumberOfSolvedProblems() == fake.getNumberOfSolvedProblems()) {
					margin = next.getTimeIncludingPenalty() - fake.getTimeIncludingPenalty();
					result.addProperty("before", margin);
				}
			}
			return fakeIndex;
		}


	}

	public ExtendedScoreDump(Contest contest, WebPublisher target) {
		this.contest = contest;
		this.publisherTarget = target;
	}

	private JsonArray stringsAsJson(String[] input) {
		JsonArray target = new JsonArray();
		for (String desktopUrl : input) {
			target.add(desktopUrl);
		}
		return target;
	}

	private JsonObject teamAsJson(Team team) {
		JsonObject target = new JsonObject();
		target.addProperty("id", team.getId());
		target.addProperty("name", team.getName());

		target.add("webcams", JsonHelpers.toJsonArray(team.getVideoLinks()));
		target.add("desktops", JsonHelpers.toJsonArray(team.getDesktopLinks()));

		Organization org = team.getOrganization();
		if (org != null) {
            target.addProperty("organization", org.getFullName());
            target.addProperty("displayname", org.getDisplayName());
        }
		return target;
	}

	public JsonArray getAllTeams() {
		JsonArray target = new JsonArray();
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
		JsonElement scoreTable = scoreDumper.execute();

		log.debug("publishing Standings... ");

		publisherTarget.publish("/scoreboard", new StaticWebDocument(scoreTable));
		publisherTarget.publish("/teams", new StaticWebDocument(getAllTeams()));

	}



}
