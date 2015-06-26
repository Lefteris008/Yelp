package yelp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author  Tzanakas Alexandros
 * @author  Paraskevas Eleftherios
 * @version 2015.06.26_1850
 */
public class Query {

    private static final HashMap distinct = new HashMap();
    private static String tempTableName;

    
    /**
     * This method executes an SQL query using the parameters supplied by
     * the external applications.
     * @param hops The businesses in a single path
     * @param latitude The latitude of the current location of the user
     * @param longitude The longitude of the current location of the user
     * @param radius The radius in meters in which the application will search for businesses
     * @param day The current day in 0-6 values. First day of the week is Sunday (0)
     * @param time The current time in 0-23 values
     * @param interval The hours the users wants to stay outside
     * @param categories The categories the user supplied
     * @param conf The configuration object
     * @return A string containing the JSON, a variable which denotes
     * whether the categorized search succeeded (0) or not (1) and the time
     * of the execution of the query in milliseconds
     * @throws SQLException 
     */
    public static String makeQuery(int hops, double latitude, double longitude, int radius, int day, int time, int interval, ArrayList categories, Configuration conf) throws SQLException {
        int hop = 1;
        int retry = 0;
        tempTableName = randomTempTableName();
        HashMap finalResults = new HashMap();
        DBHandling db = new DBHandling(conf);
        ArrayList results;
        ArrayList resultsHops = new ArrayList();
        ArrayList list = new ArrayList();
        ArrayList aux;
        ArrayList finalList = new ArrayList();
        list.add(Float.toString((float) latitude));
        list.add(Float.toString((float) longitude));
        list.add(Integer.toString(radius));
        list.add(Integer.toString(day));
        list.add(Integer.toString(time));
        list.add(Integer.toString(time + interval));
        list.add(Integer.toString(3));
        
        db.executeStmt(stringQueryTempTable(list, conf));
        
        if (!categories.isEmpty()) {
            list.add("AND category='" + categories.get(0) + "' \n");
        } else {
            list.add("\n");
        }
        double startTime = System.currentTimeMillis();
        results = db.executeStmtWithResults(stringQueryCheckIn(list, conf));
        for (Object result : results) {
            aux = (ArrayList) result;
            finalList.add(aux);
            finalResults.put(hop, finalList);
            db.executeStmt("DELETE FROM "+tempTableName+" WHERE id= '"+aux.get(0)+"'");
        }
        for (int i = 1; i < hops; i++) {
            hop++;
            time = time + interval;
            for (Object result : results) {
                // Change the parameters for the new query. aux has the info from previous results
                aux = (ArrayList) result;
                // Set the longitude, latitude according to previous business
                list.set(0, aux.get(2));
                list.set(1, aux.get(3));
                // Change the time
                list.set(4, time);
                list.set(5, time + interval);
                // The number of the results are 3
                list.set(6, 3);
                if (!categories.isEmpty()) {
                    list.set(7, "AND category = '" + categories.get(i) + "' \n");
                } else {
                    list.set(7, "\n");
                }
                aux = db.executeStmtWithResults(stringQueryCheckIn(list, conf));
                if(aux.isEmpty()) { //No POIs are returned
                    retry = 1;
                    list.set(7, "\n");
                    aux = db.executeStmtWithResults(stringQueryCheckIn(list, conf));   
                }
                aux = containsKey(aux);
                resultsHops.add(aux.get(0));
                db.executeStmt("DELETE FROM "+tempTableName+" WHERE id= '"+aux.get(0)+"'");
            }
            // For the next hop clear saved IDs. The user gets results that may have been excluded

            results.clear();
            // Save the results for the starting points of the next iteration
            finalList = new ArrayList();
            for (Object resultsHop : resultsHops) {
                results.add(resultsHop);
                finalList.add(resultsHop);
                finalResults.put(hop, finalList);
            }
            // Clear the auxiliary arraylist
            resultsHops.clear();
        }
        double endTime = System.currentTimeMillis();
        db.executeStmt("DROP TABLE "+ tempTableName);
        db.closeDB();
        return JSON.createJSONResultString(finalResults) + " " + retry + " " + (endTime - startTime);
    }

    /**
     * This method creates an SQL query that creates a temporary table
     * which helps in saving the categories of the businesses located in a city
     * @param list A list containing the required data fields for the SQL query
     * @param conf The configuration object
     * @return The SQL query to be executed
     */
    private static String stringQueryTempTable(ArrayList list, Configuration conf) {
        String sqlStmt;
        sqlStmt = "CREATE TEMPORARY TABLE "+tempTableName+" AS("
                + "SELECT * \n"
                + "FROM "+conf.businessTableName+" \n"
                + "WHERE earth_box(ll_to_earth("+list.get(0)+", "+list.get(1)+"), "+list.get(2)+"/1.609) "
                + "@> ll_to_earth(latitude, longitude))\n";

        return sqlStmt;
    }
    
    /**
     * This method creates an SQL query that returns the businesses that
     * have more check-ins in the next hour compared to the hour the user
     * has defined in the external application. 
     * @param list A list containing the required data fields for the SQL query
     * @param conf The configuration object
     * @return The SQL query to be executed
     */
    private static String stringQueryCheckIn(ArrayList list, Configuration conf) {
        String sqlStmt = 
                  "SELECT id, latitude, longitude, business_name, stars, full_address, city, category,\n"
                + "       CASE WHEN lTime - checkin_time = 1 THEN lCount - checkin_count\n"
                + "            ELSE NULL\n"
                + "       END as difference \n"
                + "FROM(\n"
                + "  SELECT business_id, checkin_day, checkin_time, checkin_count,\n"
                + "         LEAD(checkin_time) OVER w AS lTime,\n"
                + "         LEAD(checkin_count) OVER w AS lCount\n"
                + "  FROM "+conf.checkinTableName+"\n"
                + "  WHERE (checkin_info.checkin_time = "+list.get(4)+" OR checkin_info.checkin_time = "+list.get(5)
                +") AND (checkin_info.checkin_day = "+list.get(3)+")\n"
                + "  WINDOW w AS (PARTITION BY business_id, checkin_day ORDER BY checkin_time) ) t\n"
                + "INNER JOIN "+tempTableName+" ON id = t.business_id\n"
                + "WHERE lCount IS NOT NULL  "+list.get(7)+" \n"
                + "ORDER BY difference DESC\n"
                + "LIMIT 3";
        return sqlStmt;
    }
    
    /**
     * Generates a random name for the temporary table
     * @return The name of the temporary table
     */
    private static String randomTempTableName(){
        String temp = "";
        Random r = new Random();
        for(int i=0; i < 5 ; i++){
            temp = temp + (char)(r.nextInt(26) + 'a');
        }
        return temp;
    }

    /**
     * @param list A list containing the required data fields for the SQL query
     * @return 
     */
    private static ArrayList containsKey(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            ArrayList aux = (ArrayList) list.get(i);
            if (!distinct.containsKey(aux.get(0))) {
                list.clear();
                list.add(aux);
                distinct.put(aux.get(0), 1);
            }
        }
        return list;
    }
}
