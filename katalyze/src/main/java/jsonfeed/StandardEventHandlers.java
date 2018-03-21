package jsonfeed;

import model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StandardEventHandlers {
    static private Logger log = LogManager.getLogger(StandardEventHandlers.class);

    Map<String, JsonEventHandler> handlers = new ConcurrentHashMap<>();
    Map<String, JsonEventHandler> unknownEvents = new ConcurrentHashMap<>();

    public class NullEventHandler implements JsonEventHandler {

        @Override
        public void process(Contest contest, JsonEvent src) throws Exception {
            // Do nothing.
        }
    }


    public StandardEventHandlers() {

        handlers.put("teams", (contest, src) -> {
            String organizationId = src.getString("organization_id");
            Organization org = (organizationId != null) ? (contest.getOrganization(organizationId)) : null;
            contest.registerTeam(src.getString("id"), src.getString("name"), org);
        });
        handlers.put("contests", (contest, src)  -> {
            ContestProperties properties = ContestProperties.fromJSON(src.getRawData());
            contest.init(properties);
        });
        handlers.put("languages", (contest, src) -> contest.addLanguage(new Language(src.getString("id"), src.getString("name"))));
        handlers.put("organizations", (contest,src) -> {
            contest.addOrganization(
                    new Organization(src.getString("id"), src.getString("name"), src.getStringOrNull("formal_name"), src.getStringOrNull("country"), src.getStringOrNull("twitter_hashtag"))
            );
        });
        handlers.put("problems", (contest, src) -> contest.addProblem(
                new Problem(src.getString("id"), src.getString("name"), src.getString("label"), src.getStringOrNull("rgb"))));
        handlers.put("judgement-types", (contest, src) -> {
            JudgementType newJudgementType = new JudgementType(src.getString("id"), src.getBoolean("solved"), src.getBoolean("penalty"));
            contest.addJudgementType(newJudgementType);
        });
        handlers.put("submissions", (contest, src) -> {
            String submissionId = src.getString("id");
            String teamId = src.getString("team_id");
            String problemId = src.getString("problem_id");
            Problem problem = contest.getProblem(problemId);
            Team team = contest.getTeam(teamId);

            int contestTimeMilliseconds = (int) (src.getTimespan("contest_time"));
            team.freshSubmission(new InitialSubmission(submissionId, team, problem, src.getString("language_id"),
                    contestTimeMilliseconds));
        });
        handlers.put("judgements", (contest, src) -> {

            Analyzer analyzer = contest.getAnalyzer();

            String judgementId = src.getString("id");
            String submissionId = src.getString("submission_id");

            InitialSubmission submission = analyzer.submissionById(submissionId);

            if (submission == null) {
                log.warn(String.format("Judgment '%s' that references an non-existing submission '%s'",judgementId, submissionId));
            }


            String verdictId = src.getString("judgement_type_id");
            JudgementType verdict = contest.getJudgementType(verdictId);
            if (verdict != null) {
                if (submission == null) {
                    log.error(String.format("Lost judgement '%s' due to missing submission %s", judgementId, submissionId));
                } else {
                    int judgementContestTime = (int) (src.getTimespan("end_contest_time"));
                    // Only submit if there is a verdict.
                    submission.team.submit(submission, judgementContestTime, judgementId, submission.problem, verdict.getId(), verdict.isAccepted(), verdict.hasPenalty());
                }
            }

        });
    }

    public JsonEventHandler getHandlerFor(JsonEvent src) {
        String srcType = src.getType();
        JsonEventHandler handler = handlers.get(srcType);
        if (handler == null) {
            handler = unknownEvents.get(srcType);
            if (handler == null) {
                handler = new NullEventHandler();
                log.warn(String.format("Encountered unexpected event type '%s'. This and subsequent messages of the same type will be ignored", srcType));
                unknownEvents.put(srcType, handler);
            }
        }

        return handler;

    }





}
