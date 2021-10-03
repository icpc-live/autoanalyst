package model;

import io.EntityOperation;
import stats.LanguageStats;

import java.security.InvalidKeyException;
import java.util.*;

public class Contest {
	final Map<String, Problem> problems;
	final TreeSet<Problem> problemsByLabel;

	final Map<String, Group> groups;
	final Map<String, JudgementType> judgementTypes;
	final EntityMap<Organization> organizations = new EntityMap<>();
	final EntityMap<Team> teams;
	final Analyzer analyzer;
	final List<Judgement> submissions;
	final List<Language> languages;
	final LanguageStats stats;

	private ContestState state = ContestState.BeforeStart;
	private ContestProperties properties;
	private long contestTimeMillis = 0;

	public Contest() {
		this.problems = new TreeMap<>();
		this.problemsByLabel = new TreeSet<Problem>(Comparator.comparing(x -> x.label));

		this.groups = new HashMap<>();
		this.judgementTypes = new HashMap<>();
		this.teams = new EntityMap<Team>();
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
	    if (properties == null) {
	        return contestTimeMillis > 3600000*4;
        } else {
	        long freezeTime = properties.getDurationMillis() - properties.getScoreboardFreezeMillis();
	        return (contestTimeMillis > freezeTime);
        }
	}

	public void init(ContestProperties properties) {
        EntityOperation op = (properties==null) ? EntityOperation.CREATE : EntityOperation.UPDATE;
		this.properties = properties;
        analyzer.entityChanged(properties, op);
	}
	
	public Team registerTeam(String teamId, String teamName, Organization org, Group[] groups,
							 String[] webcams, String[] desktops, EntityOperation op) {
		Team newTeam = new Team(this,teamId, teamName, teamName, org, groups, webcams, desktops);

		teams.upsert(op, newTeam);

		analyzer.entityChanged(newTeam, op);
		return newTeam;
	}

	public Group registerGroup(String groupId, String groupName) {
		Group newGroup = new Group(groupId, groupName);
		groups.put(groupId, newGroup);
		return newGroup;
	}

	public Group getGroup(String groupId) {
	    return groups.get(groupId);
    }
	
	public Team[] getTeams() {
		return teams.getAll().toArray(new Team[0]);
	}

	public Standings getStandings() {
		List<Score> teamScores = new ArrayList<Score>();
		for (Team team : teams.getAll()) {
			teamScores.add(team.getCurrentScore());
		}
		return new Standings(this, teamScores, this.contestTimeMillis);
	}
	
	public int getSubmissionCount() {
		return submissions.size();
	}
	
	public void updateTime(long contestTimeMillis) {
		if (contestTimeMillis > this.contestTimeMillis) {
			this.contestTimeMillis = contestTimeMillis;
		}
	}
	
	public int getMinutesFromStart() {
		return (int) (contestTimeMillis / 60000);
	}

	public void updateState(ContestState newState) {
	    ContestState oldState = state;
	    state = newState;
	    analyzer.contestStateChanged(oldState, newState);
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

		boolean judegementMadeNoDifference = team.registerJudgement(newJudgement);
		submissions.add(newJudgement);

		if (!judegementMadeNoDifference) {
		    // Don't process rules again if judgement didn't affect the state of the contest
            Standings after = getStandings();
            analyzer.processRules(before, after, newJudgement);
        }
		analyzer.notifyHooks((int)(newJudgement.getJudgementTimeMillis()/60000));
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
		Team foundTeam = teams.get(teamNumber);
		if (foundTeam != null) {
			return foundTeam;
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
