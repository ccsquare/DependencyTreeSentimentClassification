package cc.nlp.instances;

import java.util.HashMap;
import java.util.Map.Entry;
/**
 * 一个用于分类的示例
 * 包含所需的信息和所属类标签
 * @author cc
 *
 */
public class Instance {
	private HashMap<String, Integer> label_2_id;
	InstanceData data;
	int label;
	
	public Instance(InstanceData data, int label, HashMap<String, Integer> label_2_id) {
		this.data = data;
		this.label = label;
		this.label_2_id = label_2_id;
	}
	
	public InstanceData getData(){
		return data;
	}
	
	public int getLabel(){
		return label;
	}
	
	public String getLabelName(){
		for (Entry<String, Integer> entry : label_2_id.entrySet()) {
			if(entry.getValue() == label) {
				return entry.getKey();
			}
		}
		
		return "default";
	}
}
