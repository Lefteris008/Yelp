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
 * @author Alex
 */
public class GetData {
    
    public static HashMap<String,Integer> categories = new HashMap<>();
    
    public static String cleanString(String s) {
        String cleanString;
        cleanString = s.replaceAll("\n", "");
        cleanString = cleanString.replaceAll("\'", " ");
        cleanString = cleanString.replaceAll(",", " ");
        
        return cleanString;
    }
    
    public static void storeDataIntoHashMap(String category) {
        
        if(categories.containsKey(category)) {
            categories.put(category, categories.get(category)+1);
        } else {
            categories.put(category, 1);
        }
    }
    
    public static void storeData() throws SQLException {

        BufferedReader br1 = null;
        BufferedReader br2 = null;
        JSONParser parser = new JSONParser();
        DBHandling db = new DBHandling();
        db.createTables();
        try {

            String sCurrentLine, sqlStmt;

            br1 = new BufferedReader(new FileReader(Configuration.businessFilePath));
            br2 = new BufferedReader(new FileReader(Configuration.checkinFilePath));

            System.out.println("Started filling table BUSINESS_LOCATION");
            while ((sCurrentLine = br1.readLine()) != null) {

                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String businessId = (String) jsonObject.get("business_id");
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
                    category.stream().forEach((categ) -> {
                        storeDataIntoHashMap(categ.toString());
                    });
                    sqlStmt = "INSERT INTO BUSINESS_LOCATION (id,latitude,longitude,business_name,stars,full_address,city) "
                            + "VALUES ('" + businessId + "'," + latitude + ", " + longitude + ", '" + businessName + "', " + stars + ", '" + businessAddress + "', '" + city + "');";
                    db.executeStmt(sqlStmt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully filled table BUSINESS_LOCATION");
            br2 = new BufferedReader(new FileReader(Configuration.checkinFilePath));
            System.out.println("Started filling table CHECKIN_INFO");
            while ((sCurrentLine = br2.readLine()) != null) {
                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;
                    String businessID = (String) jsonObject.get("business_id");
                    JSONObject checkinInfo = (JSONObject) jsonObject.get("checkin_info");
                    for (Object key : checkinInfo.keySet()) {
                        String count = (String) checkinInfo.get(key).toString();
                        sqlStmt = "INSERT INTO CHECKIN_INFO (business_id, checkin_time, checkin_count) "
                                + "VALUES ('" + businessID + "','" + key.toString() + "', " + count + ");";
                        db.executeStmt(sqlStmt);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully filled table CHECKIN_INFO");
            sqlStmt = "CREATE INDEX checkin_index ON CHECKIN_INFO (checkin_time)";
            db.executeStmt(sqlStmt);
            sqlStmt = "CREATE INDEX business_index ON BUSINESS_LOCATION (city)";
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
}
