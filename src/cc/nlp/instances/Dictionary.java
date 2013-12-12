package cc.nlp.instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Dictionary {
	private HashMap<String, Integer> str_2_id = new HashMap<String, Integer>();
	private HashMap<Integer, String> id_2_str = new HashMap<Integer, String>();
	
	public Dictionary(String src_path){
		File file = new File(src_path);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String line = reader.readLine();
			while(line != null){
				if(line.trim().equals("")) {
					line = reader.readLine();
					continue;
				}
				
				String[] strings = line.split("[ \t]+");
				
				str_2_id.put(strings[0], Integer.parseInt(strings[1]));
				id_2_str.put(Integer.parseInt(strings[1]), strings[0]);
				
				line = reader.readLine();
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getIDByFeature(String feature){
		if(!str_2_id.containsKey(feature)){
			return -1;
		}
		
		return str_2_id.get(feature);
	}
	
	public String getFeatureByID(int id){
		if(!id_2_str.containsKey(id))
			return "default";
		else return id_2_str.get(id);
	}
	
	public int size(){
		return str_2_id.size();
	}
}
