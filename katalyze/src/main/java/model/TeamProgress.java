package model;

import java.util.*;

public class TeamProgress {
	private final Map<Problem, ProblemJudgements> submissions = new HashMap<Problem, ProblemJudgements>();
	private final Map<Problem,String> languages = new HashMap<Problem, String>();
	private final Team team;
	private String mainLanguage = null;
	
	public TeamProgress(Team team) {
		this.team = team;
	}
	
	public Score calculateScore() {
		HashSet<Problem> solvedProblems = new HashSet<Problem>();
		
		int timeIncludingPenalty = 0;
		
		for (Problem p : submissions.keySet()) {
			ProblemJudgements submissionsForProblem = submissions.get(p);
			if (submissionsForProblem.isSolved()) {
				solvedProblems.add(p);				
				timeIncludingPenalty += submissionsForProblem.scoreContribution();
			}
		}
		
		return new Score(team, timeIncludingPenalty, solvedProblems, submissions);
	}
	
	private ProblemJudgements getSubmissionsFor(Problem problem) {
		
		if (submissions.containsKey(problem)) {
			return submissions.get(problem);
		} else {
			ProblemJudgements submissionsForProblem = new ProblemJudgements(problem);
			submissions.put(problem, submissionsForProblem);
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
	}


	public boolean register(Judgement newSubmission) {
		ProblemJudgements submissions = getSubmissionsFor(newSubmission.getProblem());
		return submissions.add(newSubmission);
	}
	
	

}
