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
            c = DriverManager.getConnection(Configuration.postgresConn,
                    Configuration.dbName, Configuration.dbPassword);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return c;
    }

    public void createTables() throws SQLException {
        Statement stmt = null;
        stmt = conn.createStatement();
        String sql;
        
        sql = "DROP TABLE IF EXISTS BUSINESS_LOCATION";
        stmt.executeUpdate(sql);
        
        sql = "DROP TABLE IF EXISTS CHECKIN_INFO";
        stmt.executeUpdate(sql);
        
        //Create a table with the ID of the business, its geolocation, name, stars and custom category
        sql = "CREATE TABLE IF NOT EXISTS BUSINESS_LOCATION"
                + "(ID char(100) PRIMARY KEY     NOT NULL," + " latitude  double precision  NOT NULL, "
                + " longitude double precision NOT NULL, " + " business_name char(100), "
                + " stars double precision, " + " full_address char(200), " + "city char(100)) ";
        stmt.executeUpdate(sql);

        System.out.println("Successfully created table BUSINESS_LOCATION");

        sql = "CREATE TABLE IF NOT EXISTS CHECKIN_INFO"
                + "(ID  SERIAL PRIMARY KEY, business_id char(100)," + " checkin_time char(10), " + " checkin_count int NOT NULL) ";
        stmt.executeUpdate(sql);

        System.out.println("Successfully created table CHECKIN_INFO");

        stmt.close();
    }

    public void executeStmt(String sqlStatement) throws SQLException {
        Statement stmt = null;
        stmt = conn.createStatement();
        conn.setAutoCommit(false);
        stmt.executeUpdate(sqlStatement);
        stmt.close();
        conn.commit();
    }
}
