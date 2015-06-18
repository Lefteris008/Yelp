package yelp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import org.json.simple.parser.ParseException;

/**
 *
 * @author  Paraskevas Eleftherios (585)
 * @author  Pliakis Nikolaos (589)
 * @author  Tzanakas Alexandros (597)
 * @version 2015.6.16.1640
 */
public class Main {
    
    public static void main(String[] args) throws SQLException, IOException, FileNotFoundException, ParseException{
        
        int choice = Integer.parseInt(args[0]);
//        String businessId = args[1];
//        int checkInDay = Integer.parseInt(args[2]);
//        int checkInHour = Integer.parseInt(args[3]);
//        int hops;
//        String[] categories = null;
//        if(args.length > 4) {
//            hops = Integer.parseInt(args[4]);
//            if(choice == 2) {
//                categories = args[5].split("/");
//            }
//        } else {
//            hops = 3;
//        }
        Query.makeQuery(3, 36.1305306, -115.2072382, 4000, 2, 14, 1);
        if(choice == 0) {
//            Clustering clus = new Clustering();
//            clus.getParentClustersFromJSON();
//            GetData.storeData(clus);
        } else {
        //    GetData.getDataFromDBs(businessId, checkInDay, checkInHour, hops, choice, categories);
        }
    }
}
