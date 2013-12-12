package cc.nlp.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在一个文件parse结束后，将会得到一个类似XML格式文件的parse结果文件，该类维护XML标记
 * @author cc
 *
 */
public class XMLMark {
	private static final String UNIT_ID_HEAD = "<unit_id = \"[0-9]+\">";
	private static final String UNIT_ID_TAIL = "</unit_id>";
	private static final String SENTENCE_ID_HEAD = "<sentence_id = \"[0-9]+\">";
	private static final String SENTENCE_ID_TAIL = "</sentence_id>";
	private static final String ORIGINAL_SENTENCE_HEAD = "<original_sentence>";
	private static final String ORIGINAL_SENTENCE_TAIL = "</original_sentence>";
	private static final String WORDS_AND_TAGS_HEAD = "<words_and_tags>";
	private static final String WORDS_AND_TAGS_TAIL = "</words_and_tags>";
	private static final String CONSTITUTE_TREE_HEAD = "<constitute_tree>";
	private static final String CONSTITUTE_TREE_TAIL = "</constitute_tree>";
	private static final String TYPED_DEPENDENCIES_HEAD = "<typed_dependencies>";
	private static final String TYPED_DEPENDENCIES_TAIL = "</typed_dependencies>";
	
	
	public static int getUnitIDByHead(String unit_id_head){
		Pattern pattern = Pattern.compile("<unit_id = \"([0-9]+)\">");
		Matcher matcher = pattern.matcher(unit_id_head);
		
		if(matcher.find()){
			return Integer.parseInt(matcher.group(1));
		}
		
		return -1;
	}
	
	public static int getSentenceIDByHead(String sentence_id_head){
		Pattern pattern = Pattern.compile("<sentence_id = \"([0-9]+)\">");
		Matcher matcher = pattern.matcher(sentence_id_head);
		
		if(matcher.find()){
			return Integer.parseInt(matcher.group(1));
		}
		
		return -1;
	}
	
	public static String getUnitIdHead(int unit_id) {
		String result = "<unit_id = \"" + unit_id + "\">";
		
		return result;
	}

	public static String getUnitIdHead() {
		return UNIT_ID_HEAD;
	}
	
	public static String getUnitIdTail() {
		return UNIT_ID_TAIL;
	}
	
	public static String getSentenceIdHead(int sentence_id){
		String result = "<sentence_id = \"" + sentence_id + "\">";
		
		return result;
	}
	
	public static String getSentenceIdHead() {
		return SENTENCE_ID_HEAD;
	}

	public static String getSentenceIdTail() {
		return SENTENCE_ID_TAIL;
	}

	public static String getOriginalSentenceHead() {
		return ORIGINAL_SENTENCE_HEAD;
	}

	public static String getOriginalSentenceTail() {
		return ORIGINAL_SENTENCE_TAIL;
	}

	public static String getWordsAndTagsHead() {
		return WORDS_AND_TAGS_HEAD;
	}

	public static String getWordsAndTagsTail() {
		return WORDS_AND_TAGS_TAIL;
	}

	public static String getConstituteTreeHead() {
		return CONSTITUTE_TREE_HEAD;
	}

	public static String getConstituteTreeTail() {
		return CONSTITUTE_TREE_TAIL;
	}

	public static String getTypedDependenciesHead() {
		return TYPED_DEPENDENCIES_HEAD;
	}

	public static String getTypedDependenciesTail() {
		return TYPED_DEPENDENCIES_TAIL;
	}
}
