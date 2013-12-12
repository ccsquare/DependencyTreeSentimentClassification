package cc.nlp.instances;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyRelation {
	String relation;
	int src_index;
	int dst_index;
	
	public DependencyRelation(String relation, int src_index, int dst_index) {
		this.relation = relation;
		this.src_index = src_index;
		this.dst_index = dst_index;
	}
	
	public String getRelation() {
		return relation;
	}


	public int getSrcIndex() {
		return src_index;
	}

	public int getDstIndex() {
		return dst_index;
	}


	/**
	 * 将一行为root(ROOT-0, 's-2)的文本转换成一条DependencyRelation记录
	 * @param current_line
	 * @return
	 * @throws NumberFormatException
	 * @throws NotDependencyStringMatchedException 
	 */
	static public DependencyRelation convertLine2DependencyRelation(String current_line) throws NumberFormatException, NotDependencyStringMatchedException{
		Pattern pattern = Pattern.compile("([^(]+)\\(.+-([0-9]+)[^0-9,]*,[ ]*.+-([0-9]+)[^0-9,)]*\\)");
		
		Matcher matcher = pattern.matcher(current_line);
		
		if(matcher.find()){
			String relation = matcher.group(1);
			String id1 = matcher.group(2);
			String id2 = matcher.group(3);
			
			int feature_id1 = Integer.parseInt(id1);
			int feature_id2 = Integer.parseInt(id2);
		
			DependencyRelation dependencyRelation = new DependencyRelation(relation, feature_id1, feature_id2);
			
			return dependencyRelation;
		} else {
			throw new NotDependencyStringMatchedException(current_line + " can not match depdendency edge");
		}
	}
	
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder("");
		stringBuilder.append(relation);
		stringBuilder.append("(");
		stringBuilder.append(src_index + ", " + dst_index);
		stringBuilder.append(")");
		
		return stringBuilder.toString();
	}
}
