package cc.nlp.finalfeatures;

/**
 * һ��ʵ����һ��label����ĳЩ�㷨�£�һ��ʵ���������Ӧ��Ȩ��
 * @author cc
 *
 */
public class LabelFeature {
	int instance_id;
	int label;
	double weight;
	
	public LabelFeature(int instance_id, int label, double weight){
		this.instance_id = instance_id;
		this.label = label;
		this.weight = weight;
	}
	
	public String toString() {
		String result = "";
		
		result += instance_id + " ";
		result += label + " ";
		result += (int)weight;
		
		return result;
	}
}
