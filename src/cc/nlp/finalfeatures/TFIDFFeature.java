package cc.nlp.finalfeatures;

/**
 * 每个实例的每个word_and_tag为一个特征，它拥有一个tf_idf值
 * @author cc
 *
 */
public class TFIDFFeature {
	int instance_id;
	int feature_id;
	double tf_idf;
	
	public TFIDFFeature(int instance_id, int feature_id, double tf_idf) {
		this.instance_id = instance_id;
		this.feature_id = feature_id;
		this.tf_idf = tf_idf;
	}
	
	public String toString(){
		String result = "";
		
		result += instance_id + " ";
		result += feature_id + " ";
		result += tf_idf;
		
		return result;
	}
}
