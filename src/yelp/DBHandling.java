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
 * @version 2015.06.27_0009
 */
public final class DBHandling {

    protected Connection conn;

    /**
     * Creates a handler of the 'postgres' database
     * @param conf  The configuration object
     */
    public DBHandling(Configuration conf) {
        conn = dbConnection(conf);
    }

    /**
     * Terminates the connection of the open database
     * @throws SQLException 
     */
    public void closeDB() throws SQLException {
        conn.close();
    }

    /**
     * Creates a connection to the 'postgres' database
     * @param conf The configuration object
     * @return 
     */
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
        return c;
    }

    /**
     * This method creates tables BUSINESS_LOCATION and CHECKIN_INFO
     * @param conf The configuration object
     * @throws SQLException 
     */
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

        sql = "CREATE TABLE IF NOT EXISTS " + conf.checkinTableName
                + "(ID  SERIAL PRIMARY KEY, business_id varchar(100)," + " checkin_day int, " + "checkin_time int, " + " checkin_count int NOT NULL) ";
        stmt.executeUpdate(sql);
        
        stmt.close();
    }

    /**
     * This method executes the supplied SQL query. It is used for
     * queries without results (e.g. creation of tables etc)
     * @param sqlStatement The SQL query statement
     * @throws SQLException 
     */
    public void executeStmt(String sqlStatement) throws SQLException {
        Statement stmt = null;
        stmt = conn.createStatement();
        conn.setAutoCommit(false);
        stmt.executeUpdate(sqlStatement);
        stmt.close();
        conn.commit();
    }

    /**
     * This method executes the supplied SQL query. It is used for
     * queries that yield results (e.g. the query that returns businesses)
     * @param sqlStatement The SQL query statement
     * @return
     * @throws SQLException 
     */
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
            
            // Get Latitude and Longitude
            info.add(rs.getString(2));
            info.add(rs.getString(3));
            
            // Get the name of the business
            info.add(rs.getString(4));
            
            // Get stars
            info.add(rs.getString(5));
            
            // Get Address
            info.add(rs.getString(6));
            
            // Get City
            info.add(rs.getString(7));
            
            // Get category
            info.add(rs.getString(8));
            
            // Get difference in number of check-ins
            info.add(rs.getString(9));
            results.add(info);
        }
        return results;
    }
}
