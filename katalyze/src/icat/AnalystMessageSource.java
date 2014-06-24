package icat;


import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.PreparedStatement;

import katalyzeapp.DatabaseNotificationConfig;

public class AnalystMessageSource {
	DatabaseNotificationConfig config;
	Connection db;
	final String NoExportHashTag = "#int";
	int lastReadMessageId = -1;
	
	
	public AnalystMessageSource(DatabaseNotificationConfig config) {
		this.config = config;
	}
	
	public void open() throws Exception {

	}
	
	public ArrayList<AnalystMessage> getNewMessages(int contestTime) throws Exception {
		db = config.createConnection();
		try {
			PreparedStatement s = db.prepareStatement("select * from entries where user <> 'katalyzer' and contest_time <= ? and id > ?");
			s.setInt(1, contestTime);
			s.setInt(2, lastReadMessageId);
			ResultSet results = s.executeQuery();

			ArrayList<AnalystMessage> messages = new ArrayList<AnalystMessage>();
			while (results.next()) {
				AnalystMessage message = AnalystMessage.fromSQL(results);
				if (message.id > lastReadMessageId) {
					lastReadMessageId = message.id;
				}
				
				// Don't export if the message is only for internal analyst use
				String lowerCaseMessageText = message.text.toLowerCase();				
				if (lowerCaseMessageText.contains(NoExportHashTag)) {
					continue;
				}
	
				if (message.contestTime == 0) {
					continue;
				}
				
				messages.add(message);
			}
			return messages;
		}
		finally {
			db.close();
		}
	}

}
