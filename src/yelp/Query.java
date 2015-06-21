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

/**
 *
 * @author Alex
 */
public class Query {

    private static HashMap distinct = new HashMap();

    public static void makeQuery(int hops, double latitude, double longitude, int radius, int day, int time, int interval, ArrayList categories) throws SQLException {
        int hop = 1;
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
        list.add(Integer.toString(3));
        if (!categories.isEmpty()) {
            list.add("WHERE category='" + categories.get(0) + "' \n");
        } else {
            list.add("\n");
        }
        results = db.executeStmtWithResults(stringQuery(list));
        for (int i = 0; i < results.size(); i++) {
            aux = (ArrayList) results.get(i);
            distinct.put(aux.get(0), hop);
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
                list.set(6, 3);
                if (!categories.isEmpty()) {
                    list.set(7, "WHERE category = '" + categories.get(i) + "' \n");
                } else {
                    list.set(7, "\n");
                }
                aux = db.executeStmtWithResults(stringQuery(list));
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
        // Key are the number of hop and value is an ArrayList with all businesses in the specific hop
        for (Object c : finalResults.keySet()) {
            System.out.println(finalResults.get(c) + " " + c);
        }
    }

    private static String stringQuery(ArrayList list) {
        String sqlStmt;
        sqlStmt = sqlStmt = "SELECT business_id, checkin_day, checkin_time, checkin_count, business_location.business_name, business_location.latitude, business_location.longitude, business_location.stars, difference\n"
                + "FROM business_location\n"
                + "INNER JOIN\n"
                + "	(SELECT b.business_id, b.checkin_day, b.checkin_time, b.checkin_count, b.checkin_count - t.checkin_count AS difference\n"
                + "	FROM (  SELECT *\n"
                + "		FROM checkin_info\n"
                + "		WHERE checkin_time = " + list.get(5) + " AND checkin_day = " + list.get(3) + ") b\n"
                + "	INNER JOIN (SELECT *\n"
                + "		    FROM checkin_info\n"
                + "		    WHERE checkin_time = " + list.get(4) + " AND checkin_day = " + list.get(3) + ") AS t\n"
                + "		ON t.business_id = b.business_id\n"
                + "	WHERE  b.business_id IN (\n"
                + "		SELECT business_location.id \n"
                + "		FROM business_location\n"
                + "		WHERE earth_box(ll_to_earth(" + list.get(0) + "," + list.get(1)
                + ")," + list.get(2) + "/1.609) @> ll_to_earth(latitude, longitude)\n"
                + "		ORDER by earth_distance(ll_to_earth(" + list.get(0) + "," + list.get(1)
                + "), ll_to_earth(latitude, longitude)), stars DESC)\n"
                + "	ORDER BY difference DESC) x ON x.business_id = business_location.id "
                + list.get(7)
                + "LIMIT " + list.get(6) + " \n";
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
