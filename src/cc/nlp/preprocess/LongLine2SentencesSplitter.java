package cc.nlp.preprocess;

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

public class LongLine2SentencesSplitter {

	public static void main(String[] args) {
		if(args.length < 1){
			System.err.println("Please input src directory");
			return ;
		}
		
		File src_file = new File(args[0]);
		if(!src_file.exists()){
			System.err.println("Src directory does not exist!");
			return ;
		}
		try {
			processDir(src_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static private void processDir(File dir) throws IOException{
		File[] childs = dir.listFiles();
		
		for (File file : childs) {
			if(file.isDirectory()) {
				System.out.println("processing directory: " + file.getAbsolutePath() + "......");
				processDir(file);
			}
			else {
				processFile(file);
			}
		}
	}
	
	static private void processFile(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		PrintWriter writer = new PrintWriter(new File("tmp"));

		String line = reader.readLine();
		while(line != null){
			writer.println(line);
			
			line = reader.readLine();
		}
		
		reader.close();
		writer.close();
		
		reader = new BufferedReader(new FileReader(new File("tmp")));
		writer = new PrintWriter(file);
		
		line = reader.readLine();
		while(line != null){
			ArrayList<String> lines = splitLineIntoSentences(line);
			
			for (String string : lines) {
				writer.println(string);
			}
			
			line = reader.readLine();
		}
		reader.close();
		writer.close();
		
		File tmp = new File("tmp");
		tmp.delete();
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
