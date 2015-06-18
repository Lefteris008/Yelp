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

    public static void makeQuery(int hops, double latitude, double longitude, int radius, int day, int time, int interval) throws SQLException {
        DBHandling db = new DBHandling();
        String sqlStmt;
        HashMap results = new HashMap();
        HashMap resultsHops = new HashMap();
        ArrayList list = new ArrayList();
        ArrayList aux = new ArrayList();
        list.add(Float.toString((float) latitude));
        list.add(Float.toString((float) longitude));
        list.add(Integer.toString(radius));
        list.add(Integer.toString(day));
        list.add(Integer.toString(time));
        list.add(Integer.toString(time + interval));
        list.add(Integer.toString(3));
        results = db.executeStmtWithResults(stringQuery(list));
        for (Object k : results.keySet()) {
                System.out.println(results.get(k));
            }
        for (int i = 1; i < hops; i++) {
            time = time + interval;
            System.out.println(time);
            for (Object k : results.keySet()) {
                aux = (ArrayList) results.get(k);
                list.set(0, aux.get(1));
                list.set(1, aux.get(2));
                list.set(5, time);
                list.set(6, 1);
                resultsHops = db.executeStmtWithResults(stringQuery(list));
                System.out.println(resultsHops);
            }
            results = resultsHops;
        }
    }

    private static String stringQuery(ArrayList list) {
        String sqlStmt;
        sqlStmt = "SELECT t.business_id, t.checkin_day, t.checkin_time, t.checkin_count, business_location.business_name, business_location.latitude, business_location.longitude,\n"
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
}
