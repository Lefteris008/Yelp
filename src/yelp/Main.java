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
 * @version 2015.6.27.0002
 */
public class Main {
    
    /**
     * This method replaces the underscores of a given string with white space.
     * @param categories A string array with the categories
     * supplied by the external application
     * @return The string array with the underscores replaced 
     */
    private static String[] trimUnderscores(String[] categories) {
        int i = 0;
        for (String category : categories) {
            categories[i] = category.replaceAll("_", " ");
            i++;
        }
        return categories;
    }
    
    /**
     * The main method of the application. It gets the arguments
     * supplied by the external PHP application and calls the required
     * functions
     * @param args A string array containing the parameters supplied by the PHP application
     * @throws SQLException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParseException 
     */
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
        String jsonToPHP = "";
        if(args.length > 8) {
            if(choice == 1) {
                categories = args[8].split("\\+");
                categories = trimUnderscores(categories);
            }
        }
        
        Configuration conf = new Configuration();
        conf.getPropertyValues();
        
        if(choice == 0) {
            Clustering clus = new Clustering();
            clus.getParentClustersFromJSON(conf);
            GetData.storeData(clus, conf);
        } else if(choice==1){
            jsonToPHP = Query.makeQuery(hops, lat, lon, radius, checkInDay, checkInHour, interval, new ArrayList<>(Arrays.asList(categories)), conf);
            
            //The method found no results
            if(jsonToPHP == null) {
                jsonToPHP = "null";
            }
        } else {
            jsonToPHP = Query.makeQuery(hops, lat, lon, radius, checkInDay, checkInHour, interval, new ArrayList<>(), conf);
            
            //The method found no results
            if(jsonToPHP == null) {
                jsonToPHP = "null";
            }
        }
        System.out.println(jsonToPHP);
    }
}
