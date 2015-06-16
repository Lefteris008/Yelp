package yelp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author lefte_000
 */
public class TestCheckInClustering {
    public static void returnNumberOfBusinessesWithHighCheckInNumber() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(Configuration.checkinFilePath));
        String currentLine;
        JSONParser parser = new JSONParser();
        int sum, totalBusinesses = 0;
        while((currentLine = br.readLine()) != null) {
            Object obj;
            try {
                obj = parser.parse(currentLine);
                JSONObject jsonObject = (JSONObject) obj;
                JSONObject checkinInfo = (JSONObject) jsonObject.get("checkin_info");
                sum = 0;
                for (Object key : checkinInfo.keySet()) {
                    String count = (String) checkinInfo.get(key).toString();
                    sum += Integer.parseInt(count);
                }
                if(sum >= 10000) {
                    totalBusinesses++;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } 
        }
        System.out.println("Total number of businesses is "+totalBusinesses);
    }
}
