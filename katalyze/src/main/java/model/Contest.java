package model;

import io.EntityOperation;
import stats.LanguageStats;

import java.security.InvalidKeyException;
import java.util.*;

public class Contest {
	final Map<String, Problem> problems;
	final TreeSet<Problem> problemsByLabel;

	final Map<String, JudgementType> judgementTypes;
	final EntityMap<Organization> organizations = new EntityMap<>();
	final List<Team> teams;
	final Analyzer analyzer;
	final List<Judgement> submissions;
	final List<Language> languages;
	final LanguageStats stats;
	private ContestProperties properties;
	double contestTime = 0;

	public Contest() {
		this.problems = new TreeMap<>();
		this.problemsByLabel = new TreeSet<Problem>(Comparator.comparing(x -> x.label));

		this.judgementTypes = new HashMap<>();
		this.teams = new ArrayList<Team>();
		this.analyzer = new Analyzer(this, 0);
		this.languages = new ArrayList<Language>();
		this.submissions = new ArrayList<Judgement>();
		this.stats = new LanguageStats();
		this.properties = new ContestProperties("Uninitialized Contest", 20, 3600000);
		
		analyzer.addRule(stats.submissionsPerLanguage);
	}

	public void init(String name, int penaltyTime, long freezeMillis) {
	    init(new ContestProperties(name, penaltyTime, freezeMillis));
	}

	public boolean isFrozen(long contestTimeMillis) {
		long freezeMillis = (properties == null) ? 3600*1000*4 : properties.getScoreboardFreezeMillis();
		return (contestTimeMillis < freezeMillis);
	}

	public void init(ContestProperties properties) {
        EntityOperation op = (properties==null) ? EntityOperation.CREATE : EntityOperation.UPDATE;
		this.properties = properties;
        analyzer.entityChanged(properties, op);
	}
	
	public Team registerTeam(String teamId, String teamName, Organization org) {
		Team newTeam = new Team(this,teamId, teamName, teamName, org);
		teams.add(newTeam);
		analyzer.entityChanged(newTeam, EntityOperation.CREATE);
		return newTeam;
	}
	
	public Team[] getTeams() {
		return teams.toArray(new Team[0]);
	}

	public Standings getStandings() {
		List<Score> teamScores = new ArrayList<Score>();
		for (Team team : teams) {
			teamScores.add(team.getCurrentScore());
		}
		return new Standings(this, teamScores);
	}
	
	public int getSubmissionCount() {
		return submissions.size();
	}
	
	public void updateTime(double contestTime) {
		if (contestTime > this.contestTime) {
			this.contestTime = contestTime;
		}
	}
	
	public int getMinutesFromStart() {
		return (int) (contestTime / 60.0);
	}
	
	public int getSubmissionsAtTime(int minutes) {
		int count = 0;
		for (Judgement s : submissions) {
			if (s.getInitialSubmission().minutesFromStart>minutes) {
				break;
			}
			count++;
		}
		return count;
	}
	
	public List<Judgement> getSubmissions() {
		return submissions;
	}
	
	public int getLengthInMinutes() {
		return (int) (this.properties.getDurationMillis() / 60000);
	}
	
	public Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public void processSubmission(Judgement newJudgement) {

		Standings before = getStandings();

		Team team = newJudgement.getTeam();

		boolean wasAlreadyRegistered = team.registerJudgement(newJudgement);
		submissions.add(newJudgement);
		Standings after = getStandings();

		analyzer.processRules(before, after, newJudgement);
		analyzer.notifyHooks(newJudgement.getJudgementTimeMillis()/60000);
	}
	
	public void addProblem(Problem newProblem) {
		problems.put(newProblem.getId(), newProblem);
		problemsByLabel.add(newProblem);

		analyzer.entityChanged(newProblem, EntityOperation.CREATE);
	}

	public void addJudgementType(JudgementType judgementType) {
		judgementTypes.put(judgementType.getId(), judgementType);
	}

	public JudgementType getJudgementType(String id) {
		return judgementTypes.get(id);
	}
	
	public void addLanguage(Language language) {
		languages.add(language);
		stats.submissionsPerLanguage.addLanguage(language.getName());
	}

	public Collection<Problem> getProblems() {
		return problemsByLabel;
	}


	public void addOrganization(Organization newOrganization) {
		this.organizations.add(newOrganization);
	}

	public Organization getOrganization(String id) {
		return organizations.get(id);
	}

	public void addTeam(Team newTeam) {
		teams.add(newTeam);
	}
	
	public Team getTeam(String teamNumber) throws InvalidKeyException {
		for (Team candidate : teams) {
			if (teamNumber.equals(candidate.getId())) {
				return candidate;
			}
		}
		throw new InvalidKeyException(String.format("%s is not a known team", teamNumber));
	}

	public Problem getProblem(String problemId) throws InvalidKeyException {
		if (problems.containsKey(problemId)) {
			return problems.get(problemId);
		} else {
			throw new InvalidKeyException(String.format("%s is not a known problem", problemId));
		}
	}

	public Problem getProblemByAbbreviation(String abbrev) throws InvalidKeyException {
		for (Map.Entry<String, Problem> problem : problems.entrySet()) {
			if (abbrev.equalsIgnoreCase(problem.getValue().getLabel())) {
				return problem.getValue();
			}
		}
		throw new InvalidKeyException(String.format("%s is not a known problem", abbrev));
	}	
	
	

}
