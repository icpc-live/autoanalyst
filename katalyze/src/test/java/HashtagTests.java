package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import rules.HashtagFinder;

public class HashtagTests {
	HashtagFinder finder = new HashtagFinder();

	@Test
	public void findsOneTag() {
		List<String> foundTeams = finder.teams("New problem solved by #t43, wow!");
		String foundMatch = foundTeams.get(0);
		assertEquals("#t43", foundMatch, "Expects to find one team in the string");
	}

	@Test
	public void findsSeveralTags() {
		List<String> foundTeams = finder.teams("New problem solved by #t43, wow! #t29 is still trying, though.");
		assertEquals("#t43", foundTeams.get(0), "Expects to find one team in the string");
		assertEquals("#t29", foundTeams.get(1), "Expects to find one team in the string");
	}
	
	@Test
	public void findsProblem() {
		assertEquals("#pA", finder.problems("Team #t43 solved #pA in 43 minutes").get(0), "simple case");
	}
	
	@Test
	public void doesntFindMalformedProblem() {
		assertEquals(0, finder.problems("Team #t43 solved #p43 in 43 minutes").size(), "simple case");
	}

}
