package cc.nlp.finalfeatures;

/**
 * ÿ��ʵ����ÿ��word_and_tagΪһ����������ӵ��һ��tf_idfֵ
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
