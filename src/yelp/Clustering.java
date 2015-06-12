package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
 * @version 2015.06.15_2104
 * 
 */

public class Clustering {
    
    public static HashMap<String,String> categories = new HashMap<>();
    public static HashMap<String,String> jsonFile = new HashMap<>();
    
    public Clustering() {
        
    }
    
    public static void transformJSONToHashMap() throws FileNotFoundException, IOException, ParseException {
        BufferedReader br;
        JSONParser parser = new JSONParser();
        String sCurrentLine;
        br = new BufferedReader(new FileReader(Configuration.categories2FilePath));
        while ((sCurrentLine = br.readLine()) != null) {
            Object obj;
            String alias, parent;
            try {
                obj = parser.parse(sCurrentLine);
                JSONObject jsonObject = (JSONObject) obj;
                alias = jsonObject.get("alias").toString();
                parent = jsonObject.get("parents").toString();
                parent = parent.replaceAll("[^\\dA-Za-z ]", "");
                jsonFile.put(alias, parent);
            } catch(ParserException e) {
                e.printStackTrace();
            }
        }
        br.close();
    }
    
    public static void storeClusters() {
        
    }
    
    public static void findParentCategory() throws FileNotFoundException, IOException, ParseException {
        String alias, currentParent, actualParent = "null";
        Set<String> keys = jsonFile.keySet();
        for(String key : keys) {
            
            //For every single key
            alias = key; //Get its alias
            currentParent = jsonFile.get(key); //Get its parent
            try {
            while(!currentParent.equals("null")) { //While this key is not a root parent
                actualParent = currentParent;
                currentParent = jsonFile.get(currentParent); //Iterate until you find it
                
            }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            categories.put(alias, actualParent);
        }
        System.out.println("");
    }
    
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
    
    
}
