package cc.nlp.datapropress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class DataPreprocesserTwentyNewsGroups {
	public static void main(String[] args) {
		String src_dir = args[0];
		
		File src_file = new File(src_dir);
		if(!src_file.exists() || !src_file.isDirectory()){
			System.err.println("source diretory input wrongly!");
			
			return ;
		}
		
		DataPreprocesserTwentyNewsGroups dataPreprocesser = new DataPreprocesserTwentyNewsGroups(src_dir);
		
//		dataPreprocesser.removeRedundantSidesAndLines();
		dataPreprocesser.joinLines();
	}

	static final String MAIL_REGEX = "(\\w)+([$\\.-]\\w+)*@(\\w)+(([$\\.-]\\w+)+)";
	static final String MAIL_REPLACEMENT = "abc@gmail.com";
	static final String PHONE_NUMBER = "";
	static final String PHONE_REPLACEMENT = "";
	
	static final String[] UNTOKENIZABLE = {"","","","",""};
	static final String UNTOKENIZABLE_REPLACEMENT = "";
	
	static final String REDUNDANT_LINE_HEAD_REGEX = "[: *=<>%#-\\/\t|~]";
	static final String REDUNDANT_LINE_TAIL_REGEX = "[ *=<>#\t-\\/|~]";
	static final String NOT_REDUNDANT_LINE_HEAD_REGEX = "[^: *=<>%#-\\/\t|~]";
	static final String REDUNDANT_LINE_REGEX 
							= "(?:[^a-zA-Z0-9]+)"
							+ "|(?:[ \t]*from[ \t]*:.*)"
							+ "|(?:[ \t]*date[ \t]*:.*)"
							+ "|(?:.+[\\S][ \t]+[|]+[ \t]+[\\S].+)"
							+ "|(?:[ \t]*re[ \t]*:.*)"
							+ "|(?:.*(?:(?:writes)|(?:wrote)|(?:write)):[\\s]*)"
							+ "|(?:in[\\s]+article.*)";
	
	private String root_dir;
	private String src_dir;
	private String dst_dir;
	
	public DataPreprocesserTwentyNewsGroups(String src_dir){
		File src_dir_file = new File(src_dir);
		root_dir = src_dir_file.getParent();
		
		this.src_dir = src_dir_file.getName();
	}
	
	/**
	 * µ±Ò»ÐÐº¬ÓÐ¶à¸ö¾ä×ÓÊ±ÀûÓÃstanford parser¶ÔÕâÐÐ½øÐÐ¾ä×ÓÇÐ·Ö£¬²¢½«ÇÐ·ÖºóµÄ¾ä×ÓÁÐ±í·µ»Ø
	 * @param line
	 * @return
	 */
	private ArrayList<String> splitLineIntoSentences(String line){
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
	
	/**
	 * µÝ¹é½«Ò»¸öÎÄ¼þ¼ÐÏÂµÄÁ¬ÐøµÄÐÐºÏ²¢ÆðÀ´
	 */
	public void joinLines(){
		dst_dir = root_dir + File.separator + src_dir + "_linesJoined";
		
		File src_file = new File(root_dir + File.separator + src_dir);
		File dst_file = new File(dst_dir);
		if(!dst_file.exists()){
			dst_file.mkdir();
		}
		
		joinLines(src_file, dst_file);
	}
	
	/**
	 * joingLines()·½·¨µÄµÝ¹é°æ±¾
	 * @param src_file
	 * @param dst_dir
	 */
	private void joinLines(File src_file, File dst_dir){
		if(src_file.isDirectory()){
			File[] childs = src_file.listFiles();
			
			for (File child_file : childs) {
				if(child_file.isDirectory()) {
					File new_dst_dir = new File(dst_dir + File.separator + child_file.getName());
					if(!new_dst_dir.exists()) new_dst_dir.mkdir();
					
					joinLines(child_file, new_dst_dir);
				} else {
					joinLines(child_file, dst_dir);
				}
			}
		} else {
			File dst_file = new File(dst_dir + File.separator + src_file.getName());
			
			if(!dst_file.exists()) {
				try {
					dst_file.createNewFile();
				} catch (IOException e) {
					System.err.println(dst_dir + File.separator + src_file.getName() + " create failed!");
					e.printStackTrace();
				}
			}
			
			try {
				joinSingleFile(src_file, dst_file);
			} catch (IOException e) {
				System.err.println(src_file.getAbsolutePath() + " process failed!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ¶ÔÓÚÒ»¸öÌØ¶¨µÄÎÄ¼þ£¬½«ÆäÖÐµÄÁ¬ÐøµÄÐÐºÏ²¢ÆðÀ´
	 * @param src_file
	 * @param dst_file
	 * @throws IOException
	 */
	private void joinSingleFile(File src_file, File dst_file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(src_file));
		PrintWriter writer = new PrintWriter(dst_file);
		
		while(hasNextData(reader)){
			String new_line = getNextData(reader);
			
			if(new_line.contains("\n")) {
				int first_wrap = new_line.indexOf('\n');
				int last_wrap = new_line.lastIndexOf('\n');
				
				ArrayList<String> previous = splitLineIntoSentences(new_line.substring(0, first_wrap));
				for (String string : previous) {
					writer.println(string);
				}
				writer.println(new_line.substring(first_wrap, last_wrap + 1));
				ArrayList<String> follow = splitLineIntoSentences(new_line.substring(last_wrap));
				for (String string : follow) {
					writer.println(string);
				}
				
				continue;
			}
			
			ArrayList<String> new_lines = splitLineIntoSentences(new_line);
			for (String line : new_lines) {
				writer.println(line);
			}
			
			writer.println();
		}
		
		reader.close();
		writer.close();
	}
	
	/**
	 * ÅÐ¶ÏÄ³¸öÎÄ¼þÖÐÊÇ·ñ»¹ÓÐÏÂÒ»ÌõÊý¾Ý¼ÇÂ¼
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private boolean hasNextData(BufferedReader reader) throws IOException{
		reader.mark(100000);
		
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
	 * »ñÈ¡ÏÂÒ»¸öunitµÄÊý¾Ý
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private String getNextData(BufferedReader reader) throws IOException{
		StringBuilder result = new StringBuilder();
		
		String line = reader.readLine();
		while(line != null && !line.trim().equals("")){
			if(isCurrentLineTable(line, reader)){
				result.append("\n" + line + "\n");
			} else {
				result.append(line + " ");
			}
			
			line = reader.readLine();
		}
		
		return result.toString();
	}
	
	private boolean isCurrentLineTable(String current_line, BufferedReader reader) throws IOException{
		reader.mark(10000);
		
		
		
		reader.reset();
		
		return false;
	}
	
	/**
	 * µÝ¹éÉ¾³ýÎÄ¼þ¼ÐÏÂÎÄ¼þµÄÈßÓàÐÐºÍÈßÓàÐÐÊ×ÓëÐÐÎ²×Ö·û
	 */
	public void removeRedundantSidesAndLines(){
		dst_dir = root_dir + File.separator + src_dir + "_redundantSidesAndLinesRemoved";
		
		File src_file = new File(root_dir + File.separator + src_dir);
		File dst_file = new File(dst_dir);
		if(!dst_file.exists()){
			dst_file.mkdir();
		}
		
		removeRedundantSidesAndLines(src_file, dst_file);
	}
	
	/**
	 * removeRedundantSidesAndLines()·½·¨µÄµÝ¹é°æ±¾
	 * @param src_file
	 * @param dst_dir
	 */
	private void removeRedundantSidesAndLines(File src_file, File dst_dir){
		if(src_file.isDirectory()){
			File[] childs = src_file.listFiles();
			
			for (File child_file : childs) {
				if(child_file.isDirectory()) {
					File new_dst_dir = new File(dst_dir + File.separator + child_file.getName());
					if(!new_dst_dir.exists()) new_dst_dir.mkdir();
					removeRedundantSidesAndLines(child_file, new_dst_dir);
				} else {
					removeRedundantSidesAndLines(child_file, dst_dir);
				}
			}
		} else {
			File dst_file = new File(dst_dir + File.separator + src_file.getName());
			
			if(!dst_file.exists()) {
				try {
					dst_file.createNewFile();
				} catch (IOException e) {
					System.err.println(dst_dir + File.separator + src_file.getName() + " create failed!");
					e.printStackTrace();
				}
			}
			
			try {
				removeRedundantInformation(src_file, dst_file);
			} catch (IOException e) {
				System.err.println(src_file.getAbsolutePath() + " process failed!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ¶ÔÓÚÒ»¸öÌØ¶¨µÄÎÄ¼þ½øÐÐÈßÓàÐÐºÍÈßÓàÐÐÊ×ÓëÐÐÎ²µÄÉ¾³ý
	 * @param src_file
	 * @param dst_file
	 * @throws IOException
	 */
	private void removeRedundantInformation(File src_file, File dst_file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(src_file));
		PrintWriter writer = new PrintWriter(dst_file);
		
		String line = reader.readLine();
		while(line != null){
			if(line.trim().equals("")) writer.println();
			else {
				writer.println(processRedundantLine(line));
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		writer.close();
	}
	
	/**
	 * ÅÐ¶ÏÄ³Ò»ÐÐµÄÄÚÈÝÊÇ·ñÎªÈßÓàÐÅÏ¢£¬ÆäÍ·»òÎ²ÊÇ·ñÓÐÈßÓà×Ö·û
	 * Èç¹ûÊÇÈßÓàÐÅÏ¢£º
	 * 		·µ»Ø¿Õ´®
	 * Èç¹ûÓÐÈßÓà×Ö·û£º
	 * 		·µ»ØÉ¾³ýÈßÓà×Ö·ûºóµÄÄÚÈÝ
	 * @param line
	 * @return
	 */
	private String processRedundantLine(String line){
		if(line.toLowerCase().trim().matches(REDUNDANT_LINE_REGEX)) return "";
		line.replaceAll("[KkMm][SsWw]>", "");
		
		String new_line = replaceLine(line);
		
		if(new_line.toLowerCase().matches(REDUNDANT_LINE_REGEX)) return "";
		
		return new_line;
	}

	/**
	 * °´Ä³Ð©Ä£Ê½Ìæ»»ÎÄ±¾ÖÐµÄÄÚÈÝ
	 * @param line
	 * @return
	 */
	private String replaceLine(String line) {
		int first_n_r = 0;
		int last_n_r = line.length()-1;

		for(int i=0; i<line.length(); i++){
			if(line.substring(i, i+1).matches(NOT_REDUNDANT_LINE_HEAD_REGEX)) {
				first_n_r = i;
				break;
			}
		}
		
		if(first_n_r == line.length()) return "";
		
		for(int i=last_n_r; i>=0; i--){
			if(!line.substring(i, i+1).matches(REDUNDANT_LINE_TAIL_REGEX)){
				last_n_r = i;
				break;
			}
		}
		
		if(last_n_r < 0) return "";
		
		String new_line = line.substring(first_n_r, last_n_r+1);
		
		new_line = new_line.replaceAll(MAIL_REGEX, MAIL_REPLACEMENT);
		for (String str : UNTOKENIZABLE) {
			new_line = new_line.replace(str, UNTOKENIZABLE_REPLACEMENT);
		}
		
		
		return new_line;
	}
	
	/**
	 * »ñÈ¡Ò»ÐÐ¿ªÊ¼µÄÎÞÓÃ×Ö·û
	 * @return
	 */
	public Set<Character> getLineHeadNotLetters(){
		File src_dir_file = new File(src_dir);
		
		return getLineHeadNotLetters(src_dir_file);
	}
	
	/**
	 * getLineHeadNotLetters()·½·¨µÄµÝ¹é°æ±¾
	 * @param file
	 * @return
	 */
	private Set<Character> getLineHeadNotLetters(File file){
		Set<Character> characters = new HashSet<Character>();
		if(file.isDirectory()){
			File[] childs = file.listFiles();
			
			for (File child_file : childs) {
				characters.addAll(getLineHeadNotLetters(child_file));
			}
		} else {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				try {
					line = reader.readLine();
					while(line != null){
						for(int i=0; i<line.length(); i++){
							if(Character.isLetterOrDigit(line.charAt(i))) break;
							
							if(line.charAt(i) == '<'){
								System.out.println(file.getAbsolutePath());
								System.out.println(line);
								
							}
							characters.add(line.charAt(i));
						}
						
						line = reader.readLine();
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
		}
		
		return characters;
	}
}
