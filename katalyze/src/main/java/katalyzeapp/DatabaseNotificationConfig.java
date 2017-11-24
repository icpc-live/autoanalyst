package katalyzeapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DatabaseNotificationConfig {
	static Logger logger = Logger.getLogger(DatabaseNotificationConfig.class);
	
	String driver;
	String connectionString;
	
	
	public DatabaseNotificationConfig(String driver, String connectionString) {
		this.driver = driver;
		this.connectionString = connectionString;
	}
	
	public Connection createConnection() throws Exception {
		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			logger.error("error creating jdbc driver: " + e);
			throw e;
		}
		try {
			return DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			logger.error("error creating database connection: " + e);
			throw e;
		}
	}
	
	public String toString() {
		return String.format("[DatabaseNotificationConfig connectionString=%s]", connectionString);
		
	}

}
