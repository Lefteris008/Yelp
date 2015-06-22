package yelp;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;

/**
 *
 * @author  Paraskevas Eleftherios
 * @version 2015.06.23_0051
 */
public class JSON {
    
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
                businessName = b.get(1);
                businessLat = b.get(2);
                businessLon = b.get(3);
                businessAddress = "N/A";
                businessStars = b.get(4);
                businessCity = "N/A";
                businessCategory = "N/A";
                
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
