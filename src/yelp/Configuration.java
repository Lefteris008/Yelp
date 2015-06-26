package yelp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author  Paraskevas Eleftherios
 * @author  Tzanakas Alexandors
 * @version 2015.06.26_1853
 */
public class  Configuration {
    
    public String businessFilePath;
    public String checkinFilePath;
    public String categoriesFilePath;
    public String postgresConn;
    public String dbName;
    public String dbPassword;
    public String businessTableName;
    public String checkinTableName;
    
    /**
     * This method extracts the property values from 'config.properties' file
     * and stores them in the class public variables
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void getPropertyValues() throws FileNotFoundException, IOException {
        
        Properties prop = new Properties();
        String propFileName = "config.properties";
        
        InputStream inputStream = new FileInputStream(propFileName);
        prop.load(inputStream);
        
        businessFilePath = prop.getProperty("businessFP");
        checkinFilePath = prop.getProperty("checkinFP");
        categoriesFilePath = prop.getProperty("categoriesFP");
        postgresConn = prop.getProperty("postgresConn");
        dbName = prop.getProperty("dbName");
        dbPassword = prop.getProperty("dbPassword");
        businessTableName = prop.getProperty("businessTableName");
        checkinTableName = prop.getProperty("checkinTableName");
    }
    
}

