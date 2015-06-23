package yelp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author  Tzanakas Alexandros
 * @author  Paraskevas Eleftherios
 * @version 2015.06.24_0107
 */
public class Query {

    private static final HashMap distinct = new HashMap();

    /*
    *   Input:  The variables needed for the query
    *   Output: The JSON as a string, 0 for no retry or 1 for retry and the
    *           total time of the execution of the query in milliseconds
    */
    public static String makeQuery(int hops, double latitude, double longitude, int radius, int day, int time, int interval, ArrayList categories, int retry, Configuration conf) throws SQLException {
        int hop = 1;
        int missedTries = 0;
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
        if (!categories.isEmpty()) {
            list.add("WHERE category='" + categories.get(0) + "' \n");
        } else {
            list.add("\n");
        }
        double startTime = System.currentTimeMillis();
        results = db.executeStmtWithResults(stringQuery(list, conf));
        for (Object result : results) {
            aux = (ArrayList) result;
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
                aux = db.executeStmtWithResults(stringQuery(list, conf));
                if(aux.isEmpty()) { //No POIs are returned
                    missedTries++;
                    if(missedTries > (hops * hops / 2)) {
                        db.closeDB();
                        return null;
                    }
                    continue;
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
        double endTime = System.currentTimeMillis();
        db.closeDB();
        
        return JSON.createJSONResultString(finalResults) + " " + retry + " " + (endTime - startTime);
    }

    private static String stringQuery(ArrayList list, Configuration conf) {
        String sqlStmt;
        sqlStmt = "SELECT business_id, checkin_day, checkin_time, checkin_count, " + conf.businessTableName + ".business_name, " + conf.businessTableName + ".latitude, " + conf.businessTableName + ".longitude, " + conf.businessTableName + ".stars, difference\n"
                + "FROM " + conf.businessTableName + "\n"
                + "INNER JOIN\n"
                + "	(SELECT b.business_id, b.checkin_day, b.checkin_time, b.checkin_count, b.checkin_count - t.checkin_count AS difference\n"
                + "	FROM (  SELECT *\n"
                + "		FROM " + conf.checkinTableName + "\n"
                + "		WHERE checkin_time = " + list.get(5) + " AND checkin_day = " + list.get(3) + ") b\n"
                + "	INNER JOIN (SELECT *\n"
                + "		    FROM " + conf.checkinTableName + "\n"
                + "		    WHERE checkin_time = " + list.get(4) + " AND checkin_day = " + list.get(3) + ") AS t\n"
                + "		ON t.business_id = b.business_id\n"
                + "	WHERE  b.business_id IN (\n"
                + "		SELECT " + conf.businessTableName + ".id \n"
                + "		FROM " + conf.businessTableName + "\n"
                + "		WHERE earth_box(ll_to_earth(" + list.get(0) + "," + list.get(1)
                + ")," + list.get(2) + "/1.609) @> ll_to_earth(latitude, longitude)\n"
                + "		ORDER by earth_distance(ll_to_earth(" + list.get(0) + "," + list.get(1)
                + "), ll_to_earth(latitude, longitude)), stars DESC)\n"
                + "	ORDER BY difference DESC) x ON x.business_id = " + conf.businessTableName + ".id "
                + list.get(7)
                + "LIMIT " + list.get(6) + " \n ";
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
