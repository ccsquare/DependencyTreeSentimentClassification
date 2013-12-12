package cc.nlp.filter_multfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cc.nlp.parse.SentenceParsedInformation;
import cc.nlp.parse.XMLMark;

/**
 * parse各个文件并得到parse结果后的后处理
 * 原本parse的结果可能是一个instance占一个文件，该类的功能是将各个文件的instance整合到一个文件中(属于同一个label的各个instance)，
 * 从而得到一个一条记录占据一个
 * <sentence_id = "1">
 * ...
 * </sentence_id>
 * 的文件
 * @author cc
 *
 */
public class InstanceFilesMerger {
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
	
	static Log log = LogFactory.getLog(InstanceFilesMerger.class);
	
//	static final String pos_dir_path = 
//			"G:\\NLPJava\\GraphSentimentClassification\\data.parsed\\txt_sentoken.parsed\\pos.parsed";
//	static final String pos_dst_dir_path =
//			"G:\\NLPJava\\GraphSentimentClassification\\data.parsed\\txt_sentoken.parsed\\pos";
	int unit_count;
	String dir_path;
	String dst_file_path;
	
	public static void main(String[] args) {
		InstanceFilesMerger convertTxtSentokenData = InstanceFilesMerger
															.ConvertTxtSentokenDataFactory(
																	args[0], 
																	args[1]);
		convertTxtSentokenData.convert();
	}
	
	static public InstanceFilesMerger ConvertTxtSentokenDataFactory(String src_dir, String dst_file_path){
		File src_dir_file = new File(src_dir);
		if(!src_dir_file.exists()) {
			System.err.println("src dir " + src_dir + "does not exist!");
			return null;
		}
		if(!src_dir_file.isDirectory()){
			System.err.println("src dir " + src_dir + " is not a directory!");
			return null;
		}
		
		File dst_file = new File(dst_file_path);
		if(!dst_file.exists()){
			try {
				dst_file.createNewFile();
			} catch (IOException e) {
				System.err.println("dst file " + dst_file.getAbsolutePath() + " create failed!");
				e.printStackTrace();
				
				return null;
			}
		}
		
		return new InstanceFilesMerger(src_dir, dst_file_path);
	}
	
	private InstanceFilesMerger(String src_dir, String dst_file_path){
		unit_count = 1;
		
		this.dir_path = src_dir;
		this.dst_file_path = dst_file_path;
	}

	public void convert(){
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(dst_file_path));
			
			File src_dir_file = new File(dir_path);
			
			File[] lists = src_dir_file.listFiles();
			
			for (File file : lists) {
				processSingleFile(file, writer);
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void processSingleFile(File file, PrintWriter writer){
		String unit_information = assembleParsedInformation(getDataFromSingleFile(file), unit_count++);
		
		writer.println(unit_information);
	}
	
	/**
	 * 将一个文件中的句子全部合并起来并组成一个unit_id
	 * @param sent_parsed_infor_list
	 * @param unit_id
	 * @return
	 */
	private String assembleParsedInformation(ArrayList<SentenceParsedInformation> sent_parsed_infor_list, int unit_id){
		StringBuilder resultBuilder = new StringBuilder("");
		
		resultBuilder.append(XMLMark.getUnitIdHead(unit_id) + "\n\n");
		
		int sentence_id = 1;
		
		for (SentenceParsedInformation sentenceParsedInformation : sent_parsed_infor_list) {
			resultBuilder.append(XMLMark.getSentenceIdHead(sentence_id++) + "\n");
			
			//合并原始句子信息
			resultBuilder.append(XMLMark.getOriginalSentenceHead() + "\n");
			for (String original_token : sentenceParsedInformation.getOriginal_tokens()) {
				resultBuilder.append(original_token + " ");
			}
			resultBuilder.deleteCharAt(resultBuilder.length()-1);
			resultBuilder.append("\n");
			resultBuilder.append(XMLMark.getOriginalSentenceTail() + "\n\n");
			
			//合并词与词性信息
			resultBuilder.append(XMLMark.getWordsAndTagsHead() + "\n");
			for (String word_and_tag : sentenceParsedInformation.getWords_and_tags()) {
				resultBuilder.append(word_and_tag + " ");
			}
			resultBuilder.deleteCharAt(resultBuilder.length() - 1);
			resultBuilder.append("\n");
			resultBuilder.append(XMLMark.getWordsAndTagsTail() + "\n\n");
			
			//合并句法树信息
			resultBuilder.append(XMLMark.getConstituteTreeHead() + "\n");
			resultBuilder.append(sentenceParsedInformation.getConstitute_tree());
			resultBuilder.append("\n");
			resultBuilder.append(XMLMark.getConstituteTreeTail() + "\n\n");
			
			//合并依存树信息
			resultBuilder.append(XMLMark.getTypedDependenciesHead() + "\n");
			for (String typed_edge : sentenceParsedInformation.getDependency_tree()) {
					resultBuilder.append(typed_edge + "\n");
			}
			resultBuilder.append(XMLMark.getTypedDependenciesTail() + "\n\n");
			
			resultBuilder.append(XMLMark.getSentenceIdTail() + "\n\n");
		}

		resultBuilder.append(XMLMark.getUnitIdTail() + "\n");
		
		return resultBuilder.toString();
	}
	
	private ArrayList<SentenceParsedInformation> getDataFromSingleFile(File file){
		ArrayList<SentenceParsedInformation> sent_parsed_infor_list = new ArrayList<SentenceParsedInformation>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			while(hasNextData(reader)){
				ArrayList<SentenceParsedInformation> new_parsed_sents = getNextData(reader, file.getAbsolutePath());

				for (SentenceParsedInformation sentenceParsedInformation : new_parsed_sents) {
					sent_parsed_infor_list.add(sentenceParsedInformation);
				}
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println(file.getAbsolutePath() + " process failed!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(file.getAbsolutePath() + " reader close failed!");
			e.printStackTrace();
		}
		
		return sent_parsed_infor_list;
	}
	
	/**
	 * 将一个文件中的units合并成一个unit
	 * @param instanceDataList
	 * @return
	 */
//	private SentenceParsedInformation mergeSentenceParsedInformations(ArrayList<SentenceParsedInformation> sent_parsed_infor_list){
//		
//		ArrayList<SentenceParsedInformation>
//		
//		for (InstanceData new_instance_data : instanceDataList) {
//			for (SentenceData new_sentence_parsed_data : new_instance_data.getParsedInformations()) {
//				instance_data.addSentenceParsedData(new_sentence_parsed_data);
//			}
//		}
//		
//		return instance_data;
//	}
	
	/*
	 * 判断某个文件中是否还有下一条数据记录
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private boolean hasNextData(BufferedReader reader) throws IOException{
		reader.mark(100);
		
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
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private ArrayList<SentenceParsedInformation> getNextData(BufferedReader reader, String current_file) throws IOException{
		ArrayList<String> original_tokens = new ArrayList<String>();
		ArrayList<String> words_and_tags = new ArrayList<String>();
		String constitute_tree = "";
		ArrayList<String> dependency_tree = new ArrayList<String>();
		
		boolean unit_flag = false;
		boolean sentence_flag = false;
		boolean original_tokens_flag = false;
		boolean words_and_tags_flag = false;
		boolean constitute_tree_flag = false;
		boolean dependency_tree_flag = false;
		
		ArrayList<SentenceParsedInformation> sent_parsed_infor_list = new ArrayList<SentenceParsedInformation>();
		
		@SuppressWarnings("unused")
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
					dependency_tree = new ArrayList<String>();
					
					continue;
				} else if(current_line.equals(SENTENCE_ID_TAIL)) {
					sentence_flag = false;
					
					SentenceParsedInformation sentenceParsedInformation = 
							new SentenceParsedInformation(original_tokens, words_and_tags, constitute_tree, dependency_tree);
					
					sent_parsed_infor_list.add(sentenceParsedInformation);
					
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
							dependency_tree.add(dependency_line);
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
		
		return sent_parsed_infor_list;
	}
	
}
