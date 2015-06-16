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
 * @author  Paraskevas Eleftherios (585)
 * @author  Pliakis Nikolaos (589)
 * @author  Tzanakas Alexandros (597)
 * @version 2015.06.16_1640
 */

public class Clustering {
    
    private static Set<String> parentClusters = new HashSet<>();
    
    public Clustering() {
        ///
    }
    
    public static boolean getParentClustersFromJSON() throws FileNotFoundException, IOException, ParseException {
        BufferedReader br;
        JSONParser parser = new JSONParser();
        String sCurrentLine;
        br = new BufferedReader(new FileReader(Configuration.categories2FilePath));
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
                e.printStackTrace();
                return false;
            }
        }
        br.close();
        return true;
    }
    
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
    
    public static boolean isParentCluster(String category) {
        return parentClusters.contains(category);
    }
    
    public static int returnTotalNumberOfParentClusters() {
        return parentClusters.size();
    }
    
    public static void printParentClusters() {
        parentClusters.stream().forEach((cat) -> {
            System.out.println(cat);
        });
    }
    
    /*
    public static void configureJSON() throws FileNotFoundException, IOException {
        BufferedReader br1 = new BufferedReader(new FileReader(Configuration.categoriesFilePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(Configuration.categories2FilePath));
        String sCurrentLine, newLine = "";
        while ((sCurrentLine = br1.readLine()) != null) {
            if(sCurrentLine.equals("[")) {
                continue;
            }
            //Find the new JSON instance
            if(!sCurrentLine.equals("    },")) {
                newLine = newLine + sCurrentLine.replaceAll("\n", "");
            } else {
                newLine = newLine + "}\n";
                newLine = newLine.replaceAll(" ", "");
                //Store it into the new file
                bw.write(newLine);
                newLine = "";
            }
        }
        br1.close();
        bw.close();
    }
    */
}
