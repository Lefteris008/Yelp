package yelp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHandling {

	Connection conn;

	public DBHandling() {
		conn = dbConnection();
	}
        
        public void closeDB() throws SQLException {
            conn.close();
        }

	public Connection dbConnection() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
					"postgres", "password");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
		return c;
	}

	public void createTables() throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();
                //Create a table with the ID of the business, its geolocation, name, stars and custom category
		String sql1 = "CREATE TABLE IF NOT EXISTS BUSINESS_LOCATION"
				+ "(ID char(100) PRIMARY KEY     NOT NULL," + " latitude  double precision  NOT NULL, "
				+ " longitude double precision NOT NULL, " + " business_name char(100), "
                                + " stars double precision, " + " custom_category int) ";
                stmt.executeUpdate(sql1);
                
                System.out.println("Successfully created table BUSINESS_LOCATION");
                
                String sql2 = "CREATE TABLE IF NOT EXISTS CHECKIN_INFO"
				+ "(ID  SERIAL PRIMARY KEY, business_id char(100)," + " checkin_day  int NOT NULL, "
				+ " checkin_hour int NOT NULL, " + " checkin_count int NOT NULL) ";
                stmt.executeUpdate(sql2);
                
                System.out.println("Successfully created table CHECKIN_INFO");
                
		stmt.close();
	}

	public void insert(String sqlStatement) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();
		conn.setAutoCommit(false);
//		System.out.println(sqlStatement);
		stmt.executeUpdate(sqlStatement);
		stmt.close();
		conn.commit();
	}
}
