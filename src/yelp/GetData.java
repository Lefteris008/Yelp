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
 * @author  Paraskevas Eleftherios (585)
 * @author  Pliakis Nikolaos (589)
 * @author  Tzanakas Alexandros (597)
 * @version 2015.06.16_1640
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
    
    public static void storeData(Clustering clus) throws SQLException {

        BufferedReader br1 = null;
        BufferedReader br2 = null;
        JSONParser parser = new JSONParser();
        DBHandling db = new DBHandling();
        db.createTables();
        try {

            String sCurrentLine, sqlStmt;

            br1 = new BufferedReader(new FileReader(Configuration.businessFilePath));
            br2 = new BufferedReader(new FileReader(Configuration.checkinFilePath));

            br2 = new BufferedReader(new FileReader(Configuration.checkinFilePath));
            
            System.out.println("Started filling table " + Configuration.checkinTableName);
            while ((sCurrentLine = br2.readLine()) != null) {
                Object obj;
                int sum;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String businessID = (String) jsonObject.get("business_id");
                    JSONObject checkinInfo = (JSONObject) jsonObject.get("checkin_info");
                    sum = 0;
                    for (Object key : checkinInfo.keySet()) {
                        String count = (String) checkinInfo.get(key).toString();
                        sum += Integer.parseInt(count);
                        sqlStmt = "INSERT INTO " + Configuration.checkinTableName + " (business_id, checkin_time, checkin_count) "
                                + "VALUES ('" + businessID + "','" + key.toString() + "', " + count + ");";
                        //db.executeStmt(sqlStmt);
                    }
                    checkInCount.put(businessID, sum);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully filled table " + Configuration.checkinTableName);
            
            System.out.println("Started filling table " + Configuration.businessTableName);
            while ((sCurrentLine = br1.readLine()) != null) {

                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String businessId = (String) jsonObject.get("business_id");
                    if(!isBusinessPromising(businessId)) {
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
                    sqlStmt = "INSERT INTO " + Configuration.businessTableName + " (id,latitude,longitude,business_name,stars,full_address,city,category) "
                            + "VALUES ('" + businessId + "'," + latitude + ", " + longitude + ", '" + businessName + "', " + stars + ", '" + businessAddress + "', '" + city + "', '" + customCategory + "');";
                    db.executeStmt(sqlStmt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully filled table " + Configuration.businessTableName);
            sqlStmt = "CREATE INDEX checkin_index ON " + Configuration.checkinTableName + " (checkin_time)";
            db.executeStmt(sqlStmt);
            sqlStmt = "CREATE INDEX business_index ON " + Configuration.businessTableName + " (city)";
            db.executeStmt(sqlStmt);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br1 != null) {
                    br1.close();
                }
                if (br2 != null){
                    br2.close();
                }
                db.closeDB();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static boolean isBusinessPromising(String businessId) {
        if(checkInCount.containsKey(businessId)) {
            if(checkInCount.get(businessId) >= 100) {
                return true;
            }
        }
        return false;
    }
}
