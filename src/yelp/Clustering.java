package yelp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import jdk.nashorn.internal.runtime.ParserException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author  Paraskevas Eleftherios
 * @author  Tzanakas Alexandros
 * @version 2015.06.26_1938
 */

public class Clustering {
    
    private static final Set<String> parentClusters = new HashSet<>();
    
    public Clustering() {
        ///
    }
    
    /**
     * Gets the parent categories from the Yelp categories JSON.
     * @param conf The configuration object
     * @return True if the clusters are successfully extracted from the JSON, false otherwise
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException 
     */
    public boolean getParentClustersFromJSON(Configuration conf) throws FileNotFoundException, IOException, ParseException {
        BufferedReader br;
        JSONParser parser = new JSONParser();
        String sCurrentLine;
        br = new BufferedReader(new FileReader(conf.categoriesFilePath));
        while ((sCurrentLine = br.readLine()) != null) {
            Object obj;
            String parent, title;
            try {
                obj = parser.parse(sCurrentLine);
                JSONObject jsonObject = (JSONObject) obj;
                title = jsonObject.get("title").toString();
                parent = jsonObject.get("parents").toString();
                parent = parent.replaceAll("[^\\dA-Za-z ]", "");
                
                if(parent.equals("null")) {
                    title = splitCamelCase(title);
                    parentClusters.add(title); //The title is a parent category, store it
                }
            } catch(ParserException e) {
                return false;
            }
        }
        br.close();
        return true;
    }
    
    /**
     * This method tokenizes a camel case formed string. Each
     * token is a separate word.
     * @param s The string that is formed using the camel case
     * @return The string with white space
     */
    static String splitCamelCase(String s) {
        return s.replaceAll(
           String.format("%s|%s|%s",
              "(?<=[A-Z])(?=[A-Z][a-z])",
              "(?<=[^A-Z])(?=[A-Z])",
              "(?<=[A-Za-z])(?=[^A-Za-z])"
           ),
           " "
        );
     }
    
    /**
     * This method checks whether a category is parent
     * @param category The category to be checked
     * @return True if the category is parent, false otherwise
     */
    public boolean isParentCluster(String category) {
        return parentClusters.contains(category);
    }
    
    /**
     * Returns the total number of parent categories.
     * @return The total number of parent categories
     */
    public int returnTotalNumberOfParentClusters() {
        return parentClusters.size();
    }
    
    /**
     * Prints the parent categories.
     */
    public void printParentClusters() {
        parentClusters.stream().forEach((cat) -> {
            System.out.println(cat);
        });
    }
}