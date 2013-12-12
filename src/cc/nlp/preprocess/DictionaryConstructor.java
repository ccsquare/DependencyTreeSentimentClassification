package cc.nlp.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import cc.nlp.parse.XMLMark;

/**
 * 静态函数类，用于构建词典
 * 输入文本是已经parse好的文件(或包含这类文件的文件夹)，
 * 得到的词典的形式示例如下：
 * abstract/JJ	471
 * absurd/JJ	472
 * absurd/UH	473
 * absurdist/JJ	474
 * absurdist/NN	475
 * absurdities/NNS	476
 * absurdity/NN	477
 * absurdly/JJ	478
 * 
 * @param 参数说明
 * @param args[0]:已被parsed的文件或文件夹完整路径
 * @param args[1]:目标文件完整路径,如果缺省则构建的词典文件名为dictionary.txt，处于输入的根目录下
 * @author cc
 * @author cc
 *
 */
public class DictionaryConstructor {
	static final String WORDS_AND_TAGS_HEAD = XMLMark.getWordsAndTagsHead();
	static final String WORDS_AND_TAGS_TAIL = XMLMark.getWordsAndTagsTail();
	
	private String src_dir;
	private String dic_path;
	private Set<String> dictionary;
	
	public static void main(String[] args) {
		if(args.length < 1){
			System.err.println("Please input argument");
			
			return ;
		}
		
		String src_dir = args[0];
		String dic_path = "";
		
		File src_file = new File(src_dir);
		
		if(args.length == 1){
			if(!src_file.exists()){
				System.err.println(src_dir + " does not exist!");
				
				return ;
			} else if(!src_file.isDirectory()){
				System.err.println(src_dir + " is not a directory!");
				
				return ;
			}
			
			dic_path = src_file.getAbsolutePath() + File.separator + "dictionary.txt";
		} else {
			dic_path = args[1];
			
			File dic_file = new File(dic_path);
			
			if(dic_file.exists()){
				if(dic_file.isDirectory()){
					dic_path = dic_file.getAbsolutePath() + File.separator + "dictionary.txt";
				}
			} else {
				dic_file.mkdir();
				dic_path = dic_file.getAbsolutePath() + File.separator + "dictionary.txt";
			}
		}
		
		File dic_file = new File(dic_path);
		if(!dic_file.exists()){
			try {
				dic_file.createNewFile();
			} catch (IOException e) {
				System.err.println("create dictionary failed!");
				
				return ;
			}
		}
		
		Runtime runtime = Runtime.getRuntime();
		System.out.println("before run total memory:\t" + runtime.totalMemory()/(1024 * 1024));
		long start = System.currentTimeMillis();
		
		getDictionaryConstructor(src_dir, dic_path).constructDictionary();
		
		long end = System.currentTimeMillis();
		
		System.out.println("total cost is:\t" + (end-start) + "ms");
		System.out.println("after run total memory:\t" + runtime.totalMemory()/(1024 * 1024));
	}
	
	static public DictionaryConstructor getDictionaryConstructor(String src_dir, String dic_path){
		return new DictionaryConstructor(src_dir, dic_path);
	}
	
	private DictionaryConstructor(String src_dir, String dic_path){
		this.src_dir = src_dir;
		this.dic_path = dic_path;
		dictionary = new HashSet<String>();
	}
	
	void constructDictionary(){
		File src_file = new File(this.src_dir);
		File dic_file = new File(this.dic_path);
		
		try {
			processFile(src_file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		TreeSet<String> treeSet = new TreeSet<String>(this.dictionary);
		
		try {
			PrintWriter writer = new PrintWriter(dic_file);
			
			int i = 1;
			for (String string : treeSet) {
				writer.println(string + "\t" + (i++));
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("file " + dic_file + " can not be found!");
			e.printStackTrace();
			return ;
		}

	}
	
	void processFile(File file) throws IOException{
		if(file.isDirectory()){
			File[] childs = file.listFiles();
			
			for(File file2: childs){
				processFile(file2);
			}
		} else {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			boolean flag = false;
			
			String line = reader.readLine().trim();
			
			while(line != null){
				String current_line = line;
				
				if(current_line.equals("")){
					line = reader.readLine();
					continue;
				}
				
				if(current_line.equals(WORDS_AND_TAGS_HEAD)){
					flag = true;
				} else if(current_line.endsWith(WORDS_AND_TAGS_TAIL)){
					flag = false;
				} else {
					if(flag){
						String[] words_and_tags = line.split("[ ]");
					
						for (String word_and_tag : words_and_tags) {
							if(!dictionary.contains(word_and_tag)) {
								dictionary.add(word_and_tag);
							}
						}
					}
				}
				
				line = reader.readLine();
			}
			
			reader.close();
		}
		
	}
}
