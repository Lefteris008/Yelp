package yelp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Alex
 */
public class Main {
    
    public static void main(String[] args) throws SQLException, IOException, FileNotFoundException, ParseException{
        
        //GetData.storeData();
        //Clustering.transformJSONToHashMap();
        Clustering.configureJSON();
        
    }
}
