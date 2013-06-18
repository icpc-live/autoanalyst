package rules;

import model.Standings;

public interface StandingsCriterion {

	boolean isFulfilled(Standings standings);
	
	String message();

}
