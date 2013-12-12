package cc.nlp.instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cc.nlp.parse.XMLMark;

/**
 * 主要包含一个静态方法，该方法将一个已被parse的数据文件夹下的文件按unit解析成Instance并返回InstanceList
 * 文件夹组织形式如下：
 * dir: test
 * 	dir: positive
 * 		file: file1
 * 		file: file2
 * 	dir: negative
 * 		file: file1
 * 		file: file2
 * @author cc
 *
 */
public class InstanceList {
	static final String UNIT_ID_HEAD = XMLMark.getUnitIdHead();
	static final String UNIT_ID_TAIL = XMLMark.getUnitIdTail();
	static final String SENTENCE_ID_HEAD = XMLMark.getSentenceIdHead();
	static final String SENTENCE_ID_TAIL = XMLMark.getSentenceIdTail();
	static final String ORIGINAL_SENTENCE_HEAD = XMLMark.getOriginalSentenceHead();
	static final String ORIGINAL_SENTENCE_TAIL = XMLMark.getOriginalSentenceTail();
	static final String WORDS_AND_TAGS_HEAD = XMLMark.getWordsAndTagsHead();
	static final String WORDS_AND_TAGS_TAIL = XMLMark.getWordsAndTagsTail();
	static final String CONSTITUTE_TREE_HEAD = XMLMark.getConstituteTreeHead();
	static final String CONSTITUTE_TREE_TAIL = XMLMark.getConstituteTreeTail();
	static final String TYPED_DEPENDENCIES_HEAD = XMLMark.getTypedDependenciesHead();
	static final String TYPED_DEPENDENCIES_TAIL = XMLMark.getTypedDependenciesTail();
	
	static Log log = LogFactory.getLog(InstanceList.class);
	
	private HashMap<String, Integer> label_2_id;
	private HashMap<String, Integer> record_count_by_label;
	private ArrayList<Instance> instanceList;
	private String parsed_data_dir_path;
	int label_count ;
	
	static public InstanceList constructInstanceList(String dir_path){
		File parsed_data_dir = new File(dir_path);
		
		if(!parsed_data_dir.exists()){
			System.err.println(dir_path + " does not exist!");
			
			return null;
		}
		
		if(!parsed_data_dir.isDirectory()){
			System.err.println(dir_path + " is not a directory");
			
			return null;
		}
		
		InstanceList instanceList = new InstanceList(dir_path);
		
		instanceList.processLabelsOfDir();
		
		return instanceList;
	}
	
	private InstanceList(String dir_path) {
		label_2_id = new HashMap<String, Integer>();
		record_count_by_label = new HashMap<String, Integer>();
		instanceList = new ArrayList<Instance>();
		label_count = 1;
		parsed_data_dir_path = dir_path;
	}
	
	public ArrayList<Instance> getInstanceList() {
		return instanceList;
	}
	
	public HashMap<String, Integer> getRecordCountByLabel() {
		return record_count_by_label;
	}
	
	public int size(){
		return instanceList.size();
	}
	
	/**
	 * label从1开始标
	 * @param label
	 * @return
	 */
	public String getLabelNameOf(int label){
		for (Entry<String, Integer> entry : label_2_id.entrySet()) {
			if(entry.getValue() == label) {
				return entry.getKey();
			}
		}
		
		return "default";
	}
	
	public int getLabelNumber(){
		return label_2_id.size();
	}
	
	private void processLabelsOfDir(){
		File dir = new File(parsed_data_dir_path);

		File[] labelFiles = dir.listFiles();
		for (File file : labelFiles) {
			if(!file.isDirectory()) continue;
			
			String label = file.getName().trim();
			
			processFilesOfLabel(file, label);
		}
	}
	
	private void processFilesOfLabel(File labelDir, String label){
		if(!label_2_id.containsKey(label)){
			label_2_id.put(label, label_count++);
			record_count_by_label.put(label, 0);
		}
		
		File[] files = labelDir.listFiles();
		
		for (File child_file : files) {
			processFile(child_file, label);
		}
	}
	
	/**
	 * 从一个文件中抽取instances
	 * @param file
	 * @param label
	 */
	private void processFile(File file, String label){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			while(hasNextData(reader)){
				InstanceData newData;
				
				newData = getNextData(reader, file.getAbsolutePath());
					
				record_count_by_label.put(label, record_count_by_label.get(label) + 1);
				instanceList.add(new Instance(newData, label_2_id.get(label), label_2_id));
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println(file.getAbsolutePath() + " process failed!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(file.getAbsolutePath() + " reader close failed!");
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断某个文件中是否还有下一条数据记录
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private boolean hasNextData(BufferedReader reader) throws IOException{
		reader.mark(1000);
		
		String line = reader.readLine();
		while(line != null){
			if(line.trim().equals("")) {
				line = reader.readLine();
				continue;
			}
			
			break;
		}
		if(line != null){
			reader.reset();
			return true;
		}
		
		reader.reset();
		
		return false;
	}
	
	/**
	 * 获取下一个unit的数据
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private InstanceData getNextData(BufferedReader reader, String current_file) throws IOException{
		ArrayList<String> original_tokens = new ArrayList<String>();
		ArrayList<String> words_and_tags = new ArrayList<String>();
		String constitute_tree = "";
		ArrayList<DependencyRelation> dependency_tree = new ArrayList<DependencyRelation>();
		
		boolean unit_flag = false;
		boolean sentence_flag = false;
		boolean original_tokens_flag = false;
		boolean words_and_tags_flag = false;
		boolean constitute_tree_flag = false;
		boolean dependency_tree_flag = false;
		
		InstanceData instanceData = new InstanceData();
		
		String unit_id = "";
		
		String line = "";

		ArrayList<String> middle_dependency_edges = new ArrayList<String>();
		
		do {
			line = reader.readLine().trim();
			String current_line = line.trim();
			
			if(current_line.matches(UNIT_ID_HEAD)) {
				unit_id = current_line;
				unit_flag = true;
				continue;
			}
			
			if(current_line.equals("")) continue;
			
			if(unit_flag){
				if(current_line.matches(SENTENCE_ID_HEAD)){
					sentence_flag = true;
					
					original_tokens = new ArrayList<String>();
					words_and_tags = new ArrayList<String>();
					constitute_tree = "";
					dependency_tree = new ArrayList<DependencyRelation>();
					
					continue;
				} else if(current_line.equals(SENTENCE_ID_TAIL)) {
					sentence_flag = false;
					
					SentenceData sentenceParsedData = 
							new SentenceData(original_tokens, words_and_tags, constitute_tree, dependency_tree);
					
					instanceData.addSentenceParsedData(sentenceParsedData);
					
					continue;
				} 
				
				if(sentence_flag) {
					if(current_line.equals(ORIGINAL_SENTENCE_HEAD)){
						original_tokens_flag = true;
						continue;
					} else if(current_line.equals(ORIGINAL_SENTENCE_TAIL)){
						original_tokens_flag = false;
						continue;
					} else if(current_line.equals(WORDS_AND_TAGS_HEAD)){
						words_and_tags_flag = true;
						continue;
					} else if(current_line.equals(WORDS_AND_TAGS_TAIL)){
						words_and_tags_flag = false;
						continue;
					} else if(current_line.equals(CONSTITUTE_TREE_HEAD)){
						constitute_tree_flag = true;
						continue;
					} else if(current_line.equals(CONSTITUTE_TREE_TAIL)){
						constitute_tree_flag = false;
						continue;
					} else if(current_line.equals(TYPED_DEPENDENCIES_HEAD)){
						middle_dependency_edges = new ArrayList<String>();
						dependency_tree_flag = true;
						continue;
					} else if(current_line.equals(TYPED_DEPENDENCIES_TAIL)){
						for (String dependency_line : middle_dependency_edges) {
							try {
								DependencyRelation dependencyRelation = DependencyRelation.convertLine2DependencyRelation(dependency_line);
								if(dependencyRelation != null)
									dependency_tree.add(dependencyRelation);
							} catch (NumberFormatException e) {
								String information = "";
								information += current_file + "\n";
								information += unit_id + "\n";
								information += current_line + "\n";
								
								String[] strings = new String[words_and_tags.size()];
								words_and_tags.toArray(strings);
								
								information += Arrays.toString(strings);

								log.info(information);
							} catch (NotDependencyStringMatchedException e) {
								log.info(e.getMessage() + "\n" + current_file + "\n" + unit_id + "\n" + current_line + "\n");
							}
						}
						
						dependency_tree_flag = false;
						continue;
					}
					
					if(original_tokens_flag){
						original_tokens = new ArrayList<String>();
						for (String token : current_line.split("[ \t]")) {
							original_tokens.add(token);
						}
					}
					if(words_and_tags_flag){
						words_and_tags = new ArrayList<String>();
						for (String word_and_tag : current_line.split("[ \t]")) {
							words_and_tags.add(word_and_tag);
						}
					}
					if(constitute_tree_flag){
						constitute_tree = current_line;
					}
					if(dependency_tree_flag){
						middle_dependency_edges.add(current_line);
					}
				}
			}
		} while(!line.trim().equals(UNIT_ID_TAIL));
		
		return instanceData;
	}
}
