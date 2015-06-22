package yelp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.simple.parser.ParseException;

/**
 *
 * @author  Paraskevas Eleftherios
 * @author  Tzanakas Alexandros
 * @version 2015.6.23.0113
 */
public class Main {
    
    private static String[] trimUnderscores(String[] categories) {
        int i = 0;
        for (String category : categories) {
            categories[i] = category.replaceAll("_", " ");
            i++;
        }
        return categories;
    }
    
    public static void main(String[] args) throws SQLException, IOException, FileNotFoundException, ParseException{
        
        int choice = Integer.parseInt(args[0]);
        double lat = Double.parseDouble(args[1]);
        double lon = Double.parseDouble(args[2]);
        int radius = Integer.parseInt(args[3]);
        int checkInDay = Integer.parseInt(args[4]);
        int checkInHour = Integer.parseInt(args[5]);
        int interval = Integer.parseInt(args[6]);
        int hops = Integer.parseInt(args[7]);
        String[] categories = null;
        if(args.length > 8) {
            if(choice == 1) {
                categories = args[8].split(">");
                categories = trimUnderscores(categories);
            }
        }
        if(choice == 0) {
            Clustering clus = new Clustering();
            clus.getParentClustersFromJSON();
            GetData.storeData(clus);
        } else if(choice==1){
            Query.makeQuery(hops, lat, lon, radius, checkInDay, checkInHour, interval, new ArrayList<>(Arrays.asList(categories)));
        } else {
            Query.makeQuery(hops, lat, lon, radius, checkInDay, checkInHour, interval, new ArrayList<>());
        }
    }
}
