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
	 * ������parsed�б�ʾ�������ϵ��words_and_tags�б��ȡ����Ҫ��ͼ�߱�ʾ
	 * ʾ��:
	 * root(ROOT-0, simplistic-1)
	 * ���������һ���ڵ�������ĸ��ڵ㣬�ʸñ߱�����
	 * conj(simplistic-1, silly-3)
	 * ��ʾ��
	 * "dictionary[simplistic]-dictionary[silly]"
	 * @param relation
	 * @param words_and_tags
	 * @return
	 */
	static String getGraphEdgeExpress(DependencyRelation relation, ArrayList<String> words_and_tags, Dictionary dictionary){
		//��ʾ�������ϵ������ĸ��ڵ����
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
