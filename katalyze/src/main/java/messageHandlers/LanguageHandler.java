package messageHandlers;

import legacyfeed.SimpleMessage;
import model.Language;
import org.apache.log4j.Logger;

public class LanguageHandler extends SingleMessageHandler {

	static Logger logger = Logger.getLogger(LanguageHandler.class);
	
	public LanguageHandler() { super("language"); }
		
	public void process(SimpleMessage message) {
		int id = message.getInt("id");
		String languageName = message.get("name").trim();
	    Language language = new Language(Integer.toString(id), languageName);
		contest.addLanguage(language);
	}

}
