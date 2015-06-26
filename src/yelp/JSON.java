package yelp;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;

/**
 *
 * @author  Paraskevas Eleftherios
 * @author  Tzanakas Alexandros
 * @version 2015.06.26_1851
 */
public class JSON {
    
    /**
     * This method creates a JSON and stores it in a string
     * @param finalResults The final results extracted from the SQL queries
     * @return A string with final JSON
     */
    public static String createJSONResultString(HashMap finalResults) {
        
        // Key are the number of hop and value is an ArrayList with all businesses in the specific hop
        ArrayList<ArrayList<String>> resultsOfHop;
        String businessId, businessName, businessLat, businessLon, businessAddress, businessStars, businessCity, businessCategory;
        JSONObject fileObj, hopObj = null, businessObj;
        fileObj = new JSONObject();
        int hopCounter = 0, businessCounter;
        for (Object c : finalResults.keySet()) {
            hopCounter++;
            resultsOfHop = (ArrayList<ArrayList<String>>) finalResults.get(c);
            hopObj = new JSONObject();
            businessCounter = 0;
            for (ArrayList<String> b : resultsOfHop) {
                businessCounter++;
                businessId = b.get(0);
                businessLon = b.get(1);
                businessLat = b.get(2);
                businessName = b.get(3);
                businessStars = b.get(4);
                businessAddress = b.get(5);
                businessCity = b.get(6);
                businessCategory = b.get(7);
                
                businessObj = new JSONObject();
                businessObj.put("id", businessId);
                businessObj.put("name", businessName);
                businessObj.put("full_address", businessAddress);
                businessObj.put("stars", businessStars);
                businessObj.put("longitude", businessLon);
                businessObj.put("latitude", businessLat);
                businessObj.put("city", businessCity);
                businessObj.put("category", businessCategory);
                
                hopObj.put("business" + businessCounter, businessObj);  
            }
            fileObj.put("hop" + hopCounter, hopObj);
            
        }
        return fileObj.toJSONString();
    }
    
}
