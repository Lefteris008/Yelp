package yelp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Tzanakas Alexandros
 * @author Paraskevas Eleftherios
 * @version 2015.06.24_0106
 */
public final class DBHandling {

    Connection conn;

    public DBHandling(Configuration conf) {
        conn = dbConnection(conf);
    }

    public void closeDB() throws SQLException {
        System.out.println("Database was closed successfully");
        conn.close();
    }

    public Connection dbConnection(Configuration conf) {
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(conf.postgresConn,
                    conf.dbName, conf.dbPassword);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Database was opened successfully");
        return c;
    }

    public void createTables(Configuration conf) throws SQLException {
        Statement stmt = null;
        stmt = conn.createStatement();
        String sql;

        sql = "DROP TABLE IF EXISTS " + conf.businessTableName;
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS " + conf.checkinTableName;
        stmt.executeUpdate(sql);

        //Create a table with the ID of the business, its geolocation, name, stars and custom category
        sql = "CREATE TABLE IF NOT EXISTS " + conf.businessTableName
                + "(ID varchar(100) PRIMARY KEY     NOT NULL," + " latitude  double precision  NOT NULL, "
                + " longitude double precision NOT NULL, " + " business_name varchar(100), "
                + " stars double precision, " + " full_address varchar(200), " + "city varchar(100), " + "category varchar(100)) ";
        stmt.executeUpdate(sql);

        System.out.println("Successfully created table " + conf.businessTableName);

        sql = "CREATE TABLE IF NOT EXISTS " + conf.checkinTableName
                + "(ID  SERIAL PRIMARY KEY, business_id varchar(100)," + " checkin_day int, " + "checkin_time int, " + " checkin_count int NOT NULL) ";
        stmt.executeUpdate(sql);

        System.out.println("Successfully created table " + conf.checkinTableName);

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

    public ArrayList executeStmtWithResults(String sqlStatement) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        ArrayList results = new ArrayList();

        st = conn.createStatement();
        rs = st.executeQuery(sqlStatement);
        while (rs.next()) {
            ArrayList info = new ArrayList();
            // Get the ID of the business
            info.add(rs.getString(1));
            // Get the name of the business
            info.add(rs.getString(8));
            // Get Latitude and Longitude
            info.add(rs.getString(9));
            info.add(rs.getString(10));
            // Get stars
            info.add(rs.getString(11));
            // Get Address
            info.add(rs.getString(12));
            // Get City
            info.add(rs.getString(13));
            // Get category
            info.add(rs.getString(14));
            // Get difference in number of check-ins
            info.add(rs.getString(15));
            results.add(info);
        }
        return results;
    }

}
