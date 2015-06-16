package yelp;

/**
 *
 * @author  Paraskevas Eleftherios (585)
 * @author  Pliakis Nikolaos (589)
 * @author  Tzanakas Alexandros (597)
 * @version 2015.06.16_1640
 */
public class  Configuration {
    
    public static String businessFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_business.json";
    public static String checkinFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_checkin.json";
    public static String categoriesFilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\categories.json";
    public static String categories2FilePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_categories.json";
    public static String postgresConn = "jdbc:postgresql://localhost:5432/postgres";
    public static String dbName = "postgres";
    public static String dbPassword = "password";
    public static String businessTableName = "BUSINESS_LOCATION";
    public static String checkinTableName = "CHECKIN_INFO";
    public static String userTableName = "USER_INFO";
}
