package cs.vsb.ski_rental.database.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ConnectionProvider {

	public static Connection getConnection() throws SQLException {
		try {
			// oracle
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return DriverManager.getConnection(System.getProperty("connection_strings." + System.getProperty("app_settings.dbms","oracle")));
	}
}
