package messageHandlers;

import io.SimpleMessage;
import model.*;

public class TeamHandler extends SingleMessageHandler {

	public TeamHandler() { super("team"); }
	
	public void process(SimpleMessage message) {
		int teamNumber = Integer.parseInt(message.get("id"));
		String teamName = message.get("name");
		Team newTeam = new Team(contest, teamNumber, teamName);
		contest.addTeam(newTeam);
	}
	
}
