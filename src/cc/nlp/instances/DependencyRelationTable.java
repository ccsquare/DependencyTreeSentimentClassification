package cc.nlp.instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

public class DependencyRelationTable {
	HashMap<String, Integer> str_to_relation = new HashMap<String, Integer>();
	HashMap<Integer, String> relation_to_str = new HashMap<Integer, String>();
	
	public DependencyRelationTable(String src_path){
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
				
				str_to_relation.put(strings[0], Integer.parseInt(strings[1]));
				relation_to_str.put(Integer.parseInt(strings[1]), strings[0]);
				
				line = reader.readLine();
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void print_relation_hashMap(){
		TreeSet<String> treeSet = new TreeSet<String>(str_to_relation.keySet());
		
		int i=1;
		for (String string : treeSet) {
			System.out.println((i++) + ": " + string);
		}
	}
	
	public int getRelationIDByString(String relationName){
		if(!str_to_relation.containsKey(relationName)){
			return -1;
		}
		
		return str_to_relation.get(relationName);
	}
	
	public String getRelationName(int id){
		if(!relation_to_str.containsKey(id))
			return "default";
		else return relation_to_str.get(id);
	}
	
	public int size(){
		return str_to_relation.size();
	}
}
