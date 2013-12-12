package cc.nlp.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cc.nlp.parse.FilesParser.ParameterConfiguration.ArgumentState;
import cc.nlp.parse.FilesParser.ParameterConfiguration.ArgumentState.ArgumentVadidation;
import cc.nlp.parse.FilesParser.ParameterConfiguration.PathVadidation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * 对一个文件或者文件夹下的文件进行递归的parse
 * 在parse结束后，建立一个与原始文件夹平行的、名字后加.parsed的文件夹作为结果
 * 所parse的文件里每行一句话，最长句子需要预先计算出长度，然后设置为-maxLength参数
 * @author cc
 *
 */
public class FilesParser {
	static LexicalizedParser lp = null;
	
	static String[] options = new String[]{
		"-outputFormat","typedDependencies,wordsAndTags",
		"-maxLength","400"
		};
	static String defaultModelDir = "edu/stanford/nlp/models/lexparser/";
	static String defaultModel = "englishPCFG.ser.gz";

	static boolean isLexicalizedParserLoaded = false;
	
	boolean is_single_sent_single_line;
	String src_dir;
	String  dst_dir;
	
	static public FilesParser createFilesParser(String src, String dst, boolean is_single_sent_single_line){
		return new FilesParser(src, dst, is_single_sent_single_line);
	}
	
	private FilesParser(String src, String dst, boolean is_single_sent_single_line){
		this.is_single_sent_single_line = is_single_sent_single_line;
		this.src_dir = src;
		this.dst_dir = dst;
		
		long start = System.currentTimeMillis();
		
		if(!isLexicalizedParserLoaded) {
			System.out.println("loading model......");
		
			lp = LexicalizedParser.loadModel(defaultModelDir + defaultModel);
			lp.setOptionFlags(options);
		
			long point1 = System.currentTimeMillis();
		
			System.out.println("load model cost " + (point1 - start) + "ms");
			
			isLexicalizedParserLoaded = true;
		}
	}
	
	static class ParameterConfiguration{
		//待parse的文件或文件夹完整路径
		String to_be_parsed_path;
		//结果文件或文件夹的完整路径
		String parsed_path;
		//待parse的文件的一行是否保证是一句话
		boolean is_single_sent_single_line;
		
		public ParameterConfiguration(){
			to_be_parsed_path = "";
			parsed_path = "";
			is_single_sent_single_line = true;
		}
		
		enum PathVadidation{
			NOT_SRC_EXIST,
			NOT_TYPE_MATCHED,
			CREATE_DST_FAILED,
			VALID
		}
		
		static class ArgumentState{
			enum ArgumentVadidation{
				NO_ARGUMENT,
				UNABLE_IDENTIFY,
				ARGUMENT_INVALID,
				INVALID,
				ARGUMENT_EXCESSIVE,
				VALID
			}
			ArgumentVadidation argumentVadidation;
			String information;
			
			public ArgumentState(ArgumentVadidation argumentVadidation, String information){
				this.argumentVadidation = argumentVadidation;
				this.information = information;
			}
		}
		
		public ArgumentState resolveArguments(String[] arguments){
			if(arguments.length < 1) {
				return new ArgumentState(ArgumentVadidation.NO_ARGUMENT, "Please input to_be_parsed file path");
			}
			
			int i = 0;
			for(; i<arguments.length; i++){
				if(arguments[i].equals("-s")) break;
			}
			
			if(i >= arguments.length - 1 || arguments[i+1].startsWith("-"))
				return new ArgumentState(ArgumentVadidation.NO_ARGUMENT, "Please input to_be_parsed file path");
			
			to_be_parsed_path = arguments[i+1];
			
			int src_path_index = i;
			int j=0;
			for(; j<arguments.length; j++){
				if(j == src_path_index || j == src_path_index+1) continue;
				
				if(arguments[j].startsWith("-")){
					if(arguments[j].length() > 2 || (arguments[j].charAt(1) != 'd' && arguments[j].charAt(1) != 't')){
						return new ArgumentState(ArgumentVadidation.UNABLE_IDENTIFY, arguments[j] + " can not be recognize");
					}
					
					if(arguments[j].charAt(1) == 'd'){
						if(j == arguments.length - 1 || arguments[j+1].startsWith("-"))
							return new ArgumentState(ArgumentVadidation.NO_ARGUMENT, "Please input destination file path");
						
						j++;
						parsed_path = arguments[j];
					} else if(arguments[j].charAt(1) == 't'){
						if(j == arguments.length - 1 || arguments[j+1].startsWith("-"))
							return new ArgumentState(ArgumentVadidation.NO_ARGUMENT, "Please input whether single sentence single line or not");
						
						j++;
						
						if(arguments[j].equals("true")){
							is_single_sent_single_line = true;
						} else if(arguments[j].equals("false")){
							is_single_sent_single_line = false;
						} else {
							return new ArgumentState(ArgumentVadidation.ARGUMENT_INVALID, arguments[j] + " is invalid");	
						}
					}
				} else {
					return new ArgumentState(ArgumentVadidation.UNABLE_IDENTIFY, arguments[j] + " can not be recognized");
				}
			}
			
			if(j < arguments.length)
				return new ArgumentState(ArgumentVadidation.ARGUMENT_EXCESSIVE, "arguments are excessive");
			
			return new ArgumentState(ArgumentVadidation.VALID, "");
		}
		
		/**
		 * 验证用户输入的待parse文件或文件夹路径与存放结果的文件或文件夹路径是否合法
		 * @param src_path
		 * @param dst_path
		 * @return
		 */
		static public PathVadidation validateSrcAndDst(String src_path, String dst_path){
			File src_file = new File(src_path);
			if(!src_file.exists()) {
				return PathVadidation.NOT_SRC_EXIST;
			}
			
			if(dst_path.equals("")){
				if(src_file.isDirectory()) {
					//当用户未输入结果文件夹路径时，按规则创建文件夹
					File dst_file = new File(src_file.getParent() + File.separator + src_file.getName() + ".parsed");
					
					dst_file.mkdir();
				} else {
					//当用户未输入结果文件时，按规则创建文件
					File dst_file = new File(src_file.getParent() + File.separator + src_file.getName() + ".parsed");
					
					try {
						dst_file.createNewFile();
					} catch (IOException e) {
						//创建结果文件失败，返回失败标识
						return PathVadidation.CREATE_DST_FAILED;
					}
				}
				return PathVadidation.VALID;
			}
			
			if(src_file.isDirectory()){
				File dst_file = new File(dst_path);
				
				if(!dst_file.exists()){
					//当用户未输入结果文件夹路径时，按规则创建文件夹
					dst_file.mkdir();
					return PathVadidation.VALID;
				} else {
					if(!dst_file.isDirectory()){
						//当源路径为文件夹，结果路径为文件时，两者类型不匹配
						return PathVadidation.NOT_TYPE_MATCHED;
					}
				}
			} else {
				File dst_file = new File(dst_path);
				
				if(!dst_file.exists()){
					//当用户未输入结果文件时，按规则创建文件
					try {
						dst_file.createNewFile();
					} catch (IOException e) {
						return PathVadidation.CREATE_DST_FAILED;
					}
					
					return PathVadidation.VALID;
				} else {
					if(dst_file.isDirectory()){
						//当源路径为文件，结果路径为文件夹时，两者类型不匹配
						return PathVadidation.NOT_TYPE_MATCHED;
					}
				}
			}
			
			return PathVadidation.VALID;
		}
	
		
	}
	
	/**
	 * @param args[0] 待parse的文件(或文件夹)的完整路径
	 * @param args[1] 文件的一行是否保证是一句话(true|false)
	 * @param args[2] parse的结果存放的路径(当无时，表示结果将存放于与parse源文件相同的目录下)
	 * @param args
	 */
	public static void main(String[] args) {
		ParameterConfiguration parameterConfiguration = new ParameterConfiguration();
		
		ArgumentState argumentState = parameterConfiguration.resolveArguments(args);
		
		if(argumentState.argumentVadidation != ArgumentVadidation.VALID){
			System.err.println(argumentState.information);
			
			return ;
		}
		
		boolean is_single_sent_single_line = parameterConfiguration.is_single_sent_single_line;
		String src_path = parameterConfiguration.to_be_parsed_path;
		String dst_path = parameterConfiguration.parsed_path;
		
		PathVadidation pathVadidation = ParameterConfiguration.validateSrcAndDst(src_path, dst_path);
		
		switch (pathVadidation) {
		case NOT_SRC_EXIST:
			System.err.println("src file does not exist!");
			return ;
		case NOT_TYPE_MATCHED:
			System.err.println("src file and dst file does not match!");
			return ;
		case CREATE_DST_FAILED:
			System.err.println("create dst file failed!");
			return ;
		default:
			break;
		}
		
		if(dst_path.equals("")){
			File src_file = new File(src_path);
			dst_path = src_file.getParent() + File.separator + src_file.getName() + ".parsed";
		}
		
		long start = System.currentTimeMillis();
		
		FilesParser filesParser = FilesParser.createFilesParser(src_path, dst_path, is_single_sent_single_line);
		
		filesParser.parse();
		
		long end = System.currentTimeMillis();
		
		System.out.println("parse " + args[0] + " cost " + (end - start) + "ms");
	}
	
	
	public void parse(){
		File file = new File(src_dir);
	
		if(file.isDirectory()){
			parseDir(file, dst_dir);
		} else {
			File dst_file = new File(dst_dir);
			
			try {
				parseSingleFile(file, dst_file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void parseDir(File file, String current_dir){
		System.out.println("parsing " + file.getAbsolutePath() + " ......");
		int count = 1;
		
		File[] child_files = file.listFiles();
		File new_dir = new File(current_dir);
		if(!new_dir.exists()){
			new_dir.mkdir();
		}
		for (File file2 : child_files) {
			if(file2.isDirectory())
				parseDir(file2, new_dir.getAbsolutePath() + File.separator + file2.getName() + ".parsed");
			else {
				System.out.print("Parsing file " + (count++) + "\r");
				parseFile(file2, current_dir);
			}
		}
	}
	
	public void parseFile(File src_file, String dst_dir){
		String dst_file_name = src_file.getName();
		File dst_file = new File(dst_dir + File.separator + dst_file_name + ".parsed");
		
		if(!dst_file.exists()){
			try {
				dst_file.createNewFile();
			} catch (IOException e) {
				System.out.println("file " + dst_file.getAbsolutePath() + " create failed!");
				e.printStackTrace();
			}
			try {
				parseSingleFile(src_file, dst_file);
			} catch (IOException e) {
				System.out.println(src_file.getAbsolutePath() + " parse failed!");
				e.printStackTrace();
			}
		} else {
			try {
				parseSingleFile(src_file, dst_file);
			} catch (IOException e) {
				System.out.println(src_file.getAbsolutePath() + " parse failed!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 对单独的一个文件进行parse
	 * @param srcFile
	 * @param dstFile
	 * @throws IOException
	 */
	public void parseSingleFile(File srcFile, File dstFile) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(srcFile));
		PrintWriter writer = new PrintWriter(dstFile);
		
		int unit_id = 1;
		String line = reader.readLine();
		while(line != null){
			if(!line.trim().equals("")){ 
				String result = parseSingleLine(line, is_single_sent_single_line, unit_id++);
				
				writer.println(result);
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		writer.close();
	}
	
	/**
	 * 对文件中的一个非空行进行parse，parse后的结果以字符串形式返回
	 * 根据一行是否只有单独的一行采用不同的parse策略
	 * @param line
	 * @param is_single_line_single_sentence
	 * @param unit_id
	 * @return
	 */
	public String parseSingleLine(String line, Boolean is_single_sent_single_line, int unit_id){
		String result = "";
		
		ArrayList<SentenceParsedInformation> parsedInformations = new ArrayList<SentenceParsedInformation>();
		if(is_single_sent_single_line){
			SentenceParsedInformation parsedInformation = parseSingleSent(line);
			parsedInformations.add(parsedInformation);
		} else {
			ArrayList<String> sentences = splitLineIntoSentences(line);
			
			for (String sentence : sentences) {
				parsedInformations.add(parseSingleSent(sentence));
			}
		}
		result = assembleParsedInformation(parsedInformations, unit_id);
		
		return result.toString();
	}

	/**
	 * 当一行含有多个句子时利用stanford parser对这行进行句子切分，并将切分后的句子列表返回
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
	 * 在一行包含多个句子时，对每个句子parse后都会得到其相应的封装在一个SentenceParsedInformation中的信息，
	 * 该方法将一行对应的多个句子的SentenceParsedInformation合并起来，形成一个Unit，并也字符串的形式返回
	 * @param parsedInformations
	 * @param unit_id
	 * @return
	 */
 	private String assembleParsedInformation(ArrayList<SentenceParsedInformation> parsedInformations, int unit_id){
		StringBuilder resultBuilder = new StringBuilder("");
		
		resultBuilder.append(XMLMark.getUnitIdHead(unit_id) + "\n\n");
		
		int sentence_id = 1;
		
		for (SentenceParsedInformation sentenceParsedInformation : parsedInformations) {
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
	
	/**
	 * 对一句话进行parse,得到这句话原始的句子、词和词性标注、句法树、依存树封装而成的一个结构
	 * @param sentence
	 * @return
	 */
	private SentenceParsedInformation parseSingleSent(String sentence){
		ArrayList<String> original_tokens = new ArrayList<String>();
		ArrayList<String> words_and_tags = new ArrayList<String>();
		String constitute_tree;
		ArrayList<String> dependency_tree = new ArrayList<String>();
		
		Tree parse;

		TokenizerFactory<CoreLabel> tokenizerFactory = 
				PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords =
				tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
		parse = lp.apply(rawWords);
		
		for (CoreLabel coreLabel : rawWords) {
			original_tokens.add(coreLabel.toString());
		}
		
		StringWriter watWriter = new StringWriter();
		PrintWriter watPrintWriter = new PrintWriter(watWriter);
		TreePrint tp = new TreePrint("wordsAndTags");
		tp.printTree(parse, watPrintWriter);
		for (String word_and_tag : watWriter.toString().trim().split("[ \t]+")) {
			words_and_tags.add(word_and_tag);
		}
		watPrintWriter.close();

		StringWriter con_tree_writer = new StringWriter();
		PrintWriter con_tree_printWriter = new PrintWriter(con_tree_writer);
	    tp = new TreePrint("oneline");
	    tp.printTree(parse, con_tree_printWriter);
	    constitute_tree = con_tree_writer.toString().trim();
	    con_tree_printWriter.close();
	    
//	    StringWriter typed_tree_writer = new StringWriter();
//	    PrintWriter typed_tree_printWriter = new PrintWriter(typed_tree_writer);
//	    tp = new TreePrint("typedDependencies");
//	    tp.printTree(parse, typed_tree_printWriter);
//	    for (String typed_edge : typed_tree_writer.toString().split("[\\n]+")) {
//			dependency_tree.add(typed_edge);
//		}
	    
	    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    Collection<TypedDependency> tdc = gs.allTypedDependencies();
	    for (TypedDependency typedDependency : tdc) {
	    	dependency_tree.add(typedDependency.toString());
		}
	    return new SentenceParsedInformation(original_tokens, words_and_tags, constitute_tree, dependency_tree);
	}
}
