package yelp;

/**
 *
 * @author Alex
 */
public class  Configuration {
    
    public static String businessFilePath = "C:\\Users\\Alex\\Documents\\NetBeansProjects\\yelp\\yelp_academic_dataset_business.json";
    public static String checkinFilePath = "C:\\Users\\Alex\\Documents\\NetBeansProjects\\yelp\\yelp_academic_dataset_checkin.json";
//    public static String businessFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_business.json";
//    public static String checkinFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_checkin.json";
    public static String categories2FilePath = "C:\\Users\\Alex\\Documents\\NetBeansProjects\\yelp\\categories.json";
    public static String postgresConn = "jdbc:postgresql://localhost:5432/postgres";
    public static String dbName = "postgres";
    public static String dbPassword = "admin";
    public static String businessTableName = "BUSINESS_LOCATION";
    public static String checkinTableName = "CHECKIN_INFO";
    public static String userTableName = "USER_INFO";
    public static String dataParentFolder = "/data/";
    public static String resultJSONFile = "results.json";
    
}

