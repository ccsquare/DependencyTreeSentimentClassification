package cc.nlp.datapropress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class DataPreprocesserAclIMDB {
	public static void main(String[] args) {
		String root = "G:\\NLPJava\\GraphSentimentClassification\\data\\aclIMDB";
		String dst_dir = "G:\\NLPJava\\GraphSentimentClassification\\data\\aclIMDB_linesSplitted";
		
		File root_file = new File(root);
		File dst_file = new File(dst_dir);
		if(!dst_file.exists()) dst_file.mkdir();
		
		try {
			processDir(root_file, dst_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	static private void processDir(File src_dir, File dst_dir) throws IOException{
		File[] childs = src_dir.listFiles();
		
		for (File file : childs) {
			if(file.isDirectory()) {
				System.out.println("processing directory: " + file.getAbsolutePath() + "......");
				File child_dst_dir = new File(dst_dir + File.separator + file.getName());
				if(!child_dst_dir.exists()) child_dst_dir.mkdir();
				
				processDir(file, child_dst_dir);
			}
			else {
				File child_dst_file = new File(dst_dir + File.separator + file.getName());
				if(!child_dst_file.exists()) child_dst_file.createNewFile();
			
				processFile(file, child_dst_file);
			}
		}
	}
	
	static private void processFile(File src_file, File dst_file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(src_file));
		PrintWriter writer = new PrintWriter(dst_file);

		String line = reader.readLine();
		while(line != null){
			line = line.trim().replaceAll("<br /><br />", ". ");
			line = line.replaceAll("<br />", " ");
			
			ArrayList<String> lines = splitLineIntoSentences(line);
			
			for (String string : lines) {
				writer.println(string);
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		writer.close();
	}
	
	/**
	 * 当一行含有多个句子时利用stanford parser对这行进行句子切分，并将切分后的句子列表返回
	 * @param line
	 * @return
	 */
	static private ArrayList<String> splitLineIntoSentences(String line){
		ArrayList<String> result = new ArrayList<String>();
		
		String paragraph = line.trim();
		Reader reader = new StringReader(paragraph);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);

		List<String> sentenceList = new LinkedList<String>();
		Iterator<List<HasWord>> it = dp.iterator();
		while (it.hasNext()) {
		   StringBuilder sentenceSb = new StringBuilder();
		   List<HasWord> sentence = it.next();
		   for (HasWord token : sentence) {
		      sentenceSb.append(token + " ");
		   }
		   sentenceSb.deleteCharAt(sentenceSb.length() - 1);
		   
		   sentenceList.add(sentenceSb.toString());
		}

		for(String sentence:sentenceList) {
		   result.add(sentence);
		}
		
		return result;
	}
}
