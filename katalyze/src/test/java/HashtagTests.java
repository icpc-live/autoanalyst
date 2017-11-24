package tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import rules.HashtagFinder;

public class HashtagTests {
	HashtagFinder finder = new HashtagFinder();

	@Test
	public void findsOneTag() {
		List<String> foundTeams = finder.teams("New problem solved by #t43, wow!");
		String foundMatch = foundTeams.get(0);
		assertEquals("Expects to find one team in the string", "#t43", foundMatch);
	}

	@Test
	public void findsSeveralTags() {
		List<String> foundTeams = finder.teams("New problem solved by #t43, wow! #t29 is still trying, though.");
		assertEquals("Expects to find one team in the string", "#t43", foundTeams.get(0));
		assertEquals("Expects to find one team in the string", "#t29", foundTeams.get(1));
	}
	
	@Test
	public void findsProblem() {
		assertEquals("simple case", "#pA", finder.problems("Team #t43 solved #pA in 43 minutes").get(0));
	}
	
	@Test
	public void doesntFindMalformedProblem() {
		assertEquals("simple case", 0, finder.problems("Team #t43 solved #p43 in 43 minutes").size());
	}

}
