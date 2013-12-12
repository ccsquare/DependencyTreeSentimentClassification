package cc.nlp.finalfeatures;

import java.util.ArrayList;

import cc.nlp.instances.DependencyRelation;
import cc.nlp.instances.Dictionary;

public class GraphEdgeFeature implements Comparable<GraphEdgeFeature>{
	int src_feature_id;
	int dst_feature_id;
	double weight;
	
	public GraphEdgeFeature(int src_feature_id, int dst_feature_id, double weight) {
		this.src_feature_id = src_feature_id;
		this.dst_feature_id = dst_feature_id;
		this.weight = weight;
	}
	
	/**
	 * 根据在parsed中表示的依存关系与words_and_tags列表抽取所需要的图边表示
	 * 示例:
	 * root(ROOT-0, simplistic-1)
	 * 由于其边有一个节点是虚拟的根节点，故该边被忽略
	 * conj(simplistic-1, silly-3)
	 * 表示成
	 * "dictionary[simplistic]-dictionary[silly]"
	 * @param relation
	 * @param words_and_tags
	 * @return
	 */
	static String getGraphEdgeExpress(DependencyRelation relation, ArrayList<String> words_and_tags, Dictionary dictionary){
		//表示该依存关系与虚拟的根节点相关
		if(relation.getSrcIndex() == 0 || relation.getDstIndex() == 0) return null;
				
		StringBuilder stringBuilder = new StringBuilder("");
				
		int feature_id1 = dictionary.getIDByFeature(words_and_tags.get(relation.getSrcIndex() - 1));
		int feature_id2 = dictionary.getIDByFeature(words_and_tags.get(relation.getDstIndex() - 1));
				
		stringBuilder.append(feature_id1 + "-" + feature_id2);
				
		return stringBuilder.toString();
	}
	
	public String toString(){
		String result = "";
				
		result += src_feature_id + " ";
		result += dst_feature_id + " ";
		result += weight;
		
		return result;
	}

	@Override
	public int compareTo(GraphEdgeFeature o) {
		if(this.src_feature_id < o.src_feature_id) return -1;
		else if(this.src_feature_id > o.src_feature_id) return 1;
		else if(this.dst_feature_id < o.dst_feature_id) return -1;
		else if(this.dst_feature_id > o.dst_feature_id) return 1;
		
		return 0;
	}
}
