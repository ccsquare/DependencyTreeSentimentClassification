package cc.nlp.finalfeatures;

/**
 * 一个实例有一个label，在某些算法下，一个实例还有其对应的权重
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
