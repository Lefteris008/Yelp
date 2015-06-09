package yelp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author lefte_000
 */
public class GetData {

    public static String businessFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_business.json";
    public static String checkinFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_checkin.json";

    /**
     * @param args the command line arguments
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {

        BufferedReader br1 = null;
        BufferedReader br2 = null;
        JSONParser parser = new JSONParser();
        DBHandling db = new DBHandling();
        db.createTables();
        try {

            String sCurrentLine;

            br1 = new BufferedReader(new FileReader(businessFilePath));
            br2 = new BufferedReader(new FileReader(checkinFilePath));
            
            System.out.println("Started filling table BUSINESS_LOCATION");
            while ((sCurrentLine = br1.readLine()) != null) {

                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;

                    String businessId = (String) jsonObject.get("business_id");
//					System.out.println(businessId);

                    double latitude = (double) jsonObject.get("latitude");

                    double longitude = (double) jsonObject.get("longitude");

                    String businessName = (String) jsonObject.get("business_name");

                    double stars = (double) jsonObject.get("stars");

                    String sqlStatement1 = "INSERT INTO BUSINESS_LOCATION (id,latitude,longitude,business_name,stars,custom_category) "
                            + "VALUES ('" + businessId + "'," + latitude + ", " + longitude + ", " + businessName + ", " + stars + ", " + 0 + ");";
                    db.insert(sqlStatement1);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Successfully filled table BUSINESS_LOCATION");
            br2 = new BufferedReader(new FileReader(checkinFilePath));
            System.out.println("Started filling table CHECKIN_INFO");
            while ((sCurrentLine = br2.readLine()) != null) {
                Object obj;
                try {
                    obj = parser.parse(sCurrentLine);
                    JSONObject jsonObject = (JSONObject) obj;

                    String businessID = (String) jsonObject.get("business_id");
                    
                    JSONObject checkinInfo = (JSONObject) jsonObject.get("checkin_info");
                    
                    for(Object key : checkinInfo.keySet()){
                         String count = (String) checkinInfo.get(key).toString();
                         String hour = key.toString().substring(0, key.toString().indexOf("-"));
                         String day = key.toString().substring(key.toString().indexOf("-")+1);
                         String sqlStatement2 = "INSERT INTO CHECKIN_INFO (business_id,checkin_day,checkin_hour,checkin_count) "
                            + "VALUES ('" + businessID + "'," + day + ", " + hour + ", " + count +");";
                        db.insert(sqlStatement2);

                    }
                    
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Successfully filled table CHECKIN_INFO");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br1 != null) {
                    br1.close();
                    //br2.close();
                    db.closeDB();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
