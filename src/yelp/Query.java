/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yelp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Alex
 */
public class Query {

    private static HashMap distinct = new HashMap();

    public static void makeQuery(int hops, double latitude, double longitude, int radius, int day, int time, int interval, List<String> userCategories) throws SQLException {
        
        double startTime = System.currentTimeMillis();
        
        int hop = 1;
        boolean categorizedSearch = false;
        HashMap finalResults = new HashMap();
        DBHandling db = new DBHandling();
        ArrayList results = new ArrayList();
        ArrayList resultsHops = new ArrayList();
        ArrayList list = new ArrayList();
        ArrayList aux = new ArrayList();
        ArrayList finalList = new ArrayList();
        list.add(Float.toString((float) latitude));
        list.add(Float.toString((float) longitude));
        list.add(Integer.toString(radius));
        list.add(Integer.toString(day));
        list.add(Integer.toString(time));
        list.add(Integer.toString(time + interval));
        list.add(Integer.toString(1500));
        
        //User may or may not want to take categories
        //into account
        if(!userCategories.isEmpty()) {
            list.add(userCategories.get(0));
            categorizedSearch = true;
        }
        if(categorizedSearch) { //Manual Search
            results = db.executeStmtWithResults(stringQueryWithCategories(list));
        } else { //Automatic search
            results = db.executeStmtWithResults(simpleStringQuery(list));
        }
        for (int i = 0; i < results.size(); i++) {
            aux = (ArrayList) results.get(i);
            distinct.put(aux.get(0),hop);
            finalList.add(aux);
            finalResults.put(hop, finalList);
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
                list.set(6, 1500);
                if(categorizedSearch) {
                    list.set(7, userCategories.get(hop-1));
                    aux = db.executeStmtWithResults(stringQueryWithCategories(list));
                } else {
                    aux = db.executeStmtWithResults(simpleStringQuery(list));
                }
                aux = containsKey(aux);
                resultsHops.add(aux.get(0));
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
        
        db.closeDB();
        // Key are the number of hop and value is an ArrayList with all businesses in the specific hop
        for(Object c : finalResults.keySet()){
            System.out.println(finalResults.get(c)+" "+c);
        }
        
        double endTime = System.currentTimeMillis();
        System.out.println("Query run for " + (endTime - startTime) + " milliseconds");
    }

    private static String simpleStringQuery(ArrayList list) {
        String sqlStmt;
        sqlStmt = "SELECT t.business_id, t.checkin_day, t.checkin_time, t.checkin_count, business_location.business_name, business_location.latitude, business_location.longitude, business_location.stars,\n"
                + "       CASE WHEN lTime - checkin_time = 1 THEN lCount - checkin_count\n"
                + "            ELSE NULL\n"
                + "       END as difference \n"
                + "FROM (\n"
                + "  SELECT business_id, checkin_day, checkin_time, checkin_count,\n"
                + "         LEAD(checkin_time) OVER w AS lTime,\n"
                + "         LEAD(checkin_count) OVER w AS lCount\n"
                + "  FROM checkin_info\n"
                + "  WHERE checkin_info.business_id IN (\n"
                + "	SELECT business_location.id \n"
                + "	FROM business_location\n"
                + "	WHERE earth_box(ll_to_earth(" + list.get(0) + "," + list.get(1)
                + ")," + list.get(2) + "/1.609) @> ll_to_earth(latitude, longitude)\n"
                + "	ORDER by earth_distance(ll_to_earth(" + list.get(0) + "," + list.get(1) + "), ll_to_earth(latitude, longitude)), stars DESC\n"
                + "	) AND (checkin_info.checkin_time = " + list.get(4) + " OR checkin_info.checkin_time = " + list.get(5) + ") AND (checkin_info.checkin_day = " + list.get(3) + ")\n"
                + "  WINDOW w AS (PARTITION BY business_id, checkin_day ORDER BY checkin_time) ) t\n"
                + "INNER JOIN business_location ON business_location.id = t.business_id\n"
                + "WHERE lCount IS NOT NULL\n"
                + "ORDER BY difference DESC\n"
                + "LIMIT " + list.get(6);
        return sqlStmt;
    }
    
    private static String stringQueryWithCategories(ArrayList list) {
        String sqlStmt;
        sqlStmt = "SELECT t.business_id, t.checkin_day, t.checkin_time, t.checkin_count, " + Configuration.businessTableName + ".business_name, " + Configuration.businessTableName + ".latitude, " + Configuration.businessTableName + ".longitude, " + Configuration.businessTableName + ".stars,\n"
                + "       CASE WHEN lTime - checkin_time = 1 THEN lCount - checkin_count\n"
                + "            ELSE NULL\n"
                + "       END as difference \n"
                + "FROM (\n"
                + "  SELECT business_id, checkin_day, checkin_time, checkin_count,\n"
                + "         LEAD(checkin_time) OVER w AS lTime,\n"
                + "         LEAD(checkin_count) OVER w AS lCount\n"
                + "  FROM " + Configuration.checkinTableName + ", " + Configuration.businessTableName + "\n"
                + "  WHERE " + Configuration.checkinTableName + ".business_id IN (\n"
                + "	SELECT " + Configuration.businessTableName + ".id \n"
                + "	FROM " + Configuration.businessTableName + "\n"
                + "	WHERE earth_box(ll_to_earth(" + list.get(0) + "," + list.get(1)
                + ")," + list.get(2) + "/1.609) @> ll_to_earth(latitude, longitude)\n"
                + "	ORDER by earth_distance(ll_to_earth(" + list.get(0) + "," + list.get(1) + "), ll_to_earth(latitude, longitude)), stars DESC\n"
                + "	) AND (" + Configuration.checkinTableName + ".checkin_time = " + list.get(4) + " OR " + Configuration.checkinTableName + ".checkin_time = " + list.get(5) + ") AND (" + Configuration.checkinTableName + ".checkin_day = " + list.get(3) + ") AND (REPLACE(" + Configuration.businessTableName + ".category, ' ', '') = REPLACE('" + list.get(7) + "', ' ',''))\n"
                + "  WINDOW w AS (PARTITION BY business_id, checkin_day ORDER BY checkin_time) ) t\n"
                + "INNER JOIN " + Configuration.businessTableName + " ON " + Configuration.businessTableName + ".id = t.business_id\n"
                + "WHERE lCount IS NOT NULL\n"
                + "ORDER BY difference DESC\n"
                + "LIMIT " + list.get(6);
        return sqlStmt;
    }
    
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
