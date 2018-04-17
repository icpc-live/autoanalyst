package model;

import org.apache.log4j.Logger;

import java.util.*;

public class TeamProgress {
    static Logger logger = Logger.getLogger(TeamProgress.class);

	private final Map<Problem, ProblemJudgements> judgements = new HashMap<Problem, ProblemJudgements>();
	private final Map<Problem,String> languages = new HashMap<Problem, String>();
	private final Map<String, InitialSubmission> openSubmissions = new HashMap<>();
	private final Team team;
	private String mainLanguage = null;
	
	public TeamProgress(Team team) {
		this.team = team;
	}
	
	public Score calculateScore() {
		HashSet<Problem> solvedProblems = new HashSet<Problem>();
		
		int timeIncludingPenalty = 0;
		
		for (Problem p : judgements.keySet()) {
			ProblemJudgements submissionsForProblem = judgements.get(p);
			if (submissionsForProblem.isSolved()) {
				solvedProblems.add(p);				
				timeIncludingPenalty += submissionsForProblem.scoreContribution();
			}
		}
		
		return new Score(team, timeIncludingPenalty, solvedProblems, judgements);
	}
	
	private ProblemJudgements getJudgementsFor(Problem problem) {
		
		if (judgements.containsKey(problem)) {
			return judgements.get(problem);
		} else {
			ProblemJudgements submissionsForProblem = new ProblemJudgements(problem);
			judgements.put(problem, submissionsForProblem);
			return submissionsForProblem;
		}
	}
	
	public String languageFor(Problem problem) {
		String language = languages.get(problem);
		return language;
	}
	
	private String calculateMainLanguage() {
		Collection<String> usedLanguages = languages.values();
		Map<String, Integer> langCount = new HashMap<String, Integer>();
		
		String lastUsed = null;
		
		for (String s : usedLanguages) {
			int currentValue = 0;
			if (langCount.containsKey(s)) {
				currentValue = langCount.get(s).intValue();
			}
			
			langCount.put(s, currentValue+1);
			lastUsed = s;
		}
		
		Set<String> keySet = langCount.keySet();
		if (keySet.size() == 0) {
			return null;
		}
		
		if (keySet.size() == 1) {
			return lastUsed;
		}
		else {
			return "Mixed";
		}
		
	}
	
	public String getMainLanguage() {
		return mainLanguage;
	}



	public void registerInitialSubmission(InitialSubmission initialSubmission) {
		languages.put(initialSubmission.getProblem(), initialSubmission.language);
		mainLanguage = calculateMainLanguage();
		openSubmissions.put(initialSubmission.id, initialSubmission);
	}


	public boolean register(Judgement newJudgement) {
		InitialSubmission submission = openSubmissions.remove(newJudgement.initialSubmission.id);
		if (submission == null) {
			logger.debug(String.format("Judgement %s registered for team %s although submission was already judged",
                    newJudgement.getJudgementId(), TeamNameAsOrganization.instance.apply(team)));
		}
		ProblemJudgements judgementsForProblem = getJudgementsFor(newJudgement.getProblem());
		return judgementsForProblem.add(newJudgement);
	}

	public ArrayList<InitialSubmission> getOpenSubmissions(Problem problem) {
	    ArrayList<InitialSubmission> matches = new ArrayList<>();
	    openSubmissions.forEach((id, submission) -> {
	        if (submission.problem == problem) {
	            matches.add(submission);
            }
        });
	    return matches;

    }
	

}
