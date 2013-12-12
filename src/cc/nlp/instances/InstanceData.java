package cc.nlp.instances;

import java.util.ArrayList;

/**
 * 每个instance的数据部分以该类的形式组织，每个类对应于被parsed结果的一个UNIT
 * @author cc
 *
 */
public class InstanceData {
	ArrayList<SentenceData> parsedSentences;
	
	public InstanceData(){
		parsedSentences = new ArrayList<SentenceData>();
	}
	
	public InstanceData(ArrayList<SentenceData> parsedInformations){
		this.parsedSentences = parsedInformations;
	}
	
	public int size(){
		return parsedSentences.size();
	}
	
	public ArrayList<SentenceData> getParsedInformations(){
		return parsedSentences;
	}
	
	public void addSentenceParsedData(SentenceData sentenceParsedData){
		parsedSentences.add(sentenceParsedData);
	}
}
