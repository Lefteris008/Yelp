package yelp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author lefte_000
 */
public class GetData {
	
	public static String filePath = "C:\\Users\\lefte_000\\Downloads\\yelp\\yelp_academic_dataset_business.json";
	/**
	 * @param args
	 *            the command line arguments
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {

		BufferedReader br = null;
		JSONParser parser = new JSONParser();
		DBHandling db = new DBHandling();
		db.createTable();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filePath));

			while ((sCurrentLine = br.readLine()) != null) {

				Object obj;
				try {
					obj = parser.parse(sCurrentLine);
					JSONObject jsonObject = (JSONObject) obj;

					String businessId = (String) jsonObject.get("business_id");
//					System.out.println(businessId);

					double latitude = (double) jsonObject.get("latitude");
					
					double longitude = (double) jsonObject.get("longitude");
					
					String sqlStatement = "INSERT INTO BUSINESS_LOCATION (id,latitude,longitude) "
							+ "VALUES ('"+businessId+"',"+latitude+", "+ longitude+"  );";
					
					db.insert(sqlStatement);

//					for (Object key : hours.keySet()) {
//						System.out.println(key + " " + hours.get(key));
//
//					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
