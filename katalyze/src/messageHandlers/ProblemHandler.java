package messageHandlers;

import org.apache.log4j.Logger;
import model.*;
import io.*;

public class ProblemHandler extends SingleMessageHandler {

	static Logger logger = Logger.getLogger(ProblemHandler.class);
	
	public ProblemHandler() { super("problem"); }
		
	public void process(SimpleMessage message) {
		int id = message.getInt("id");
		String abbrev = ""+Character.toChars(64+id)[0];
		String problemName = message.get("name").trim();
	    logger.info("addProblem(" + message.get("id") + ", " + id + ", " + abbrev + ", " + problemName + ")");
	    Problem newProblem = new Problem(id, problemName);
		contest.addProblem(newProblem);
	}

}
