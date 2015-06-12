package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
        BufferedReader br1 = null;
        BufferedReader br2 = null;
        JSONParser parser = new JSONParser();
        String sCurrentLine, sLine;
        br1 = new BufferedReader(new FileReader(Configuration.categoriesFilePath));
        while ((sCurrentLine = br1.readLine()) != null) {
            Object obj;
            String alias, currentParent, realParent;
            try {
                obj = parser.parse(sCurrentLine);
                JSONObject jsonObject = (JSONObject) obj;
                alias = (String) jsonObject.get("alias");
                currentParent = (String) jsonObject.get("parent");
                realParent = "null";
                if(currentParent.equals("null")) {
                    jsonFile.put(alias, alias); // Alias is a parent category itselft
                } else { //The alias is not a parent category
                    while(!currentParent.equals("null")) {
                        //Open the file again and iterate until you find it
                        br2 = new BufferedReader(new FileReader(Configuration.categoriesFilePath));
                        while ((sLine = br2.readLine()) != null) {
                            //Get next element
                            obj = parser.parse(sCurrentLine);
                            jsonObject = (JSONObject) obj;
                            
                            //Find the listing of the previous currentParent
                            if(jsonObject.get("alias").toString().equals(currentParent)) {
                                //Store its parent
                                realParent = (String) jsonObject.get("parent");
                                
                                //If it is a real parent, terminate the iteration
                                if(realParent.equals("null")) {
                                    realParent = currentParent;
                                    br2.close();
                                    break;
                                }
                            }
                        }
                        jsonFile.put(alias, realParent); // Store the parent category of the alias
                    }
                }
            } catch(ParserException e) {
                e.printStackTrace();
            }
        }
        br1.close();
    }
    
    public static void storeClusters() {
        
    }
    
    public static void findParentCategory() throws FileNotFoundException, IOException, ParseException {
        
        
        
    }
    
    public static void configureJSON() throws FileNotFoundException, IOException {
        BufferedReader br1 = new BufferedReader(new FileReader(Configuration.categoriesFilePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(Configuration.categories2FilePath));
        String sCurrentLine, newLine = "";
        while ((sCurrentLine = br1.readLine()) != null) {
            //Find the new JSON instance
            if(!sCurrentLine.equals("    },")) {
                newLine = newLine + sCurrentLine.replaceAll("\n", "");
            } else {
                newLine = newLine + "},\n";
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
