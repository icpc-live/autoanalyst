package rules;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Contest;
import model.Problem;
import model.Team;

public class HashtagFinder {

	Pattern teamTag = Pattern.compile("#t\\d+");
	Pattern problemTag = Pattern.compile("#p[a-zA-Z]");
	
	private ArrayList<String> getMatches(String source, Pattern pattern) {
		ArrayList<String> results = new ArrayList<String>();
		
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			String match = matcher.group();
			results.add(match);
		}
		return results;
		
	}
	
	public ArrayList<String> teams(String source) {
		return getMatches(source, teamTag);
	}
	
	public ArrayList<String> problems(String source) {
		return getMatches(source, problemTag);
	}
	
	public Team getTeam(Contest contest, String tag) {
		try {
			String teamNumberString = tag.replace("#t", "");
			return contest.getTeam(teamNumberString);
		}
		catch (Exception e) {
			return null;
		}	
	}
	
	public Problem getProblem(Contest contest, String tag) {
		try {
			String problemIdString = tag.replace("#p", "");
			return contest.getProblemByAbbreviation(problemIdString);
		}
		catch (Exception e) {
			return null;
		}	
		
	}

}
