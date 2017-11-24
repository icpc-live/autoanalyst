package rules;

public interface StandingsUpdatedEvent {
	
	void onStandingsUpdated(StandingsTransition transition);

}
