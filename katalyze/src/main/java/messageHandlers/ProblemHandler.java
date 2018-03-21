package messageHandlers;

import legacyfeed.SimpleMessage;
import org.apache.log4j.Logger;
import model.*;

public class ProblemHandler extends SingleMessageHandler {

	static Logger logger = Logger.getLogger(ProblemHandler.class);
	
	public ProblemHandler() { super("problem"); }

	private static String getProblemAbbreviation(String id) {
		try {
			int idAsInteger = Integer.parseInt(id);
			return ""+Character.toChars(64+idAsInteger)[0];
		} catch (NumberFormatException e) {
			return id;
		}
	}


	public void process(SimpleMessage message) {
		String id = message.get("id");

		String abbrev = getProblemAbbreviation(id);

		String problemName = message.get("name").trim();
	    logger.info("addProblem(" + message.get("id") + ", " + id + ", " + abbrev + ", " + problemName + ")");
	    Problem newProblem = new Problem(id, problemName, abbrev, null);
		contest.addProblem(newProblem);
	}

}
