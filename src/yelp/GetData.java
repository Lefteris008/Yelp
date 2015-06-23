package yelp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import org.json.simple.JSONArray;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author  Tzanakas Alexandros
 * @author  Paraskevas Eleftherios
 * @version 2015.06.24_0107
 */
public class GetData {
    
    public static HashMap<String, Integer> checkInCount = new HashMap<>();
    
    public static String cleanString(String s) {
        String cleanString;
        cleanString = s.replaceAll("\n", "");
        cleanString = cleanString.replaceAll("\'", " ");
        cleanString = cleanString.replaceAll(",", " ");
        
        return cleanString;
    }
    
    /*
    *   Stores data into the two main tables of the DB
    *   Data are extracted from the business and check in
    *   JSON files and are stored into the BUSINESS_LOCATION
    *   and CHECKIN_INFO tables respectively
    */
    public static void storeData(Clustering clus, Configuration conf) throws SQLException {

        BufferedReader br1 = null;
        BufferedReader br2 = null;
        JSONParser parser = new JSONParser();
        DBHandling db = new DBHandling(conf);
        db.createTables(conf);
        try {

            String sCurrentLine, sqlStmt;

            br1 = new BufferedReader(new FileReader(conf.businessFilePath));
            br2 = new BufferedReader(new FileReader(conf.checkinFilePath));

            br2 = new BufferedReader(new FileReader(conf.checkinFilePath));
            
            System.out.println("Started filling table " + conf.checkinTableName);
            while ((sCurrentLine = br2.readLine()) != null) {
                Object obj;
                int sum;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String businessID = (String) jsonObject.get("business_id");
                    String day, time;
                    JSONObject checkinInfo = (JSONObject) jsonObject.get("checkin_info");
                    sum = 0;
                    for (Object key : checkinInfo.keySet()) {
                        day = key.toString().substring(key.toString().indexOf("-")+1);
                        time = key.toString().substring(0, key.toString().indexOf("-"));
                        String count = (String) checkinInfo.get(key).toString();
                        sum += Integer.parseInt(count);
                        sqlStmt = "INSERT INTO " + conf.checkinTableName + " (business_id, checkin_day, checkin_time, checkin_count) "
                                + "VALUES ('" + businessID + "'," + day + ", " + time + "," + count + ");";
                        db.executeStmt(sqlStmt);
                    }
                    checkInCount.put(businessID, sum);
                } catch (ParseException e) {
                    ///
                }
            }
            System.out.println("Successfully filled table " + conf.checkinTableName);
            
            System.out.println("Started filling table " + conf.businessTableName);
            while ((sCurrentLine = br1.readLine()) != null) {

                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String businessId = (String) jsonObject.get("business_id");
                    if(!isFilteredOut(businessId)) {
                        continue;
                    }
                    double latitude = (double) jsonObject.get("latitude");
                    double longitude = (double) jsonObject.get("longitude");
                    String businessName = (String) jsonObject.get("name");
                    businessName = cleanString(businessName);
                    double stars = (double) jsonObject.get("stars");
                    String businessAddress = (String) jsonObject.get("full_address");
                    businessAddress = cleanString(businessAddress);
                    String city = (String) jsonObject.get("city");
                    city = cleanString(city);
                    JSONArray category = (JSONArray) jsonObject.get("categories");
                    String customCategory = "";
                    for (Object category1 : category) {
                        if (clus.isParentCluster(category1.toString())) {
                            customCategory = category1.toString();
                            break;
                        }
                    }
                    sqlStmt = "INSERT INTO " + conf.businessTableName + " (id,latitude,longitude,business_name,stars,full_address,city,category) "
                            + "VALUES ('" + businessId + "'," + latitude + ", " + longitude + ", '" + businessName + "', " + stars + ", '" + businessAddress + "', '" + city + "', '" + customCategory + "');";
                    db.executeStmt(sqlStmt);
                } catch (ParseException e) {
                    ///
                }
            }
            System.out.println("Successfully filled table " + conf.businessTableName);
            sqlStmt = "CREATE INDEX checkin_index_on_time ON " + conf.checkinTableName + " (checkin_time)";
            db.executeStmt(sqlStmt);
            sqlStmt = "CREATE INDEX checkin_index_on_day ON " + conf.checkinTableName + " (checkin_day)";
            db.executeStmt(sqlStmt);
            sqlStmt = "CREATE INDEX business_index_on_cities ON " + conf.businessTableName + " (city)";
            db.executeStmt(sqlStmt);
            sqlStmt = "CREATE INDEX business_index_on_categories ON " + conf.businessTableName + " (category)";
            db.executeStmt(sqlStmt);
            
            sqlStmt = "CREATE EXTENSION IF NOT EXISTS cube";
            db.executeStmt(sqlStmt);
            sqlStmt = "CREATE EXTENSION IF NOT EXISTS earthdistance";
            db.executeStmt(sqlStmt);
        } catch (IOException e) {
            ///
        } finally {
            try {
                if (br1 != null) {
                    br1.close();
                }
                if (br2 != null){
                    br2.close();
                }
                db.closeDB();
            } catch (IOException e) {
                ///
            }
        }
    }
    
    /*
    *   Check whether the business has at least 100
    *   check ins and if not, return false (filter it out)
    */
    public static boolean isFilteredOut(String businessId) {
        if(checkInCount.containsKey(businessId)) {
            if(checkInCount.get(businessId) >= 100) {
                return true;
            }
        }
        return false;
    }
}
