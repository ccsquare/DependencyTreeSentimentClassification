package cc.nlp.instances;

import java.util.ArrayList;

/**
 * ÿ��instance�����ݲ����Ը������ʽ��֯��ÿ�����Ӧ�ڱ�parsed�����һ��UNIT
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
