package cc.nlp.instances;

import java.util.ArrayList;

/**
 * 每一句话在被parse后保留其4类信息
 * 示例：
 * simplistic , silly and tedious . 
 * 
 * 被parse后得到
 * <original_sentence>
 * simplistic , silly and tedious . 
 * </original_sentence>
 * 
 * <words_and_tags>
 * simplistic/JJ ,/, silly/JJ and/CC tedious/JJ ./.
 * </words_and_tags>
 * 
 * <constitute_tree>
 * (ROOT (FRAG (ADJP (ADJP (JJ simplistic)) (, ,) (ADJP (JJ silly)) (CC and) (ADJP (JJ tedious))) (. .)))
 * </constitute_tree>
 * 
 * <typed_dependencies>
 * root(ROOT-0, simplistic-1)
 * conj_and(simplistic-1, silly-3)
 * conj_and(simplistic-1, tedious-5)
 * </typed_dependencies>
 * @author cc
 *
 */
public class SentenceData {
	ArrayList<String> original_tokens;
	ArrayList<String> words_and_tags;
	String constitute_tree;
	ArrayList<DependencyRelation> dependency_tree;

	public SentenceData(ArrayList<String> original_tokens, ArrayList<String> words_and_tags, String constitute_tree, ArrayList<DependencyRelation> dependency_tree){
		this.original_tokens = original_tokens;
		this.words_and_tags = words_and_tags;
		this.constitute_tree = constitute_tree;
		this.dependency_tree = dependency_tree;
	}
	
	public ArrayList<String> getOriginal_tokens() {
		return original_tokens;
	}

	public void setOriginal_tokens(ArrayList<String> original_tokens) {
		this.original_tokens = original_tokens;
	}

	public ArrayList<String> getWords_and_tags() {
		return words_and_tags;
	}

	public void setWords_and_tags(ArrayList<String> words_and_tags) {
		this.words_and_tags = words_and_tags;
	}

	public String getConstitute_tree() {
		return constitute_tree;
	}

	public void setConstitute_tree(String constitute_tree) {
		this.constitute_tree = constitute_tree;
	}

	public ArrayList<DependencyRelation> getDependency_tree() {
		return dependency_tree;
	}

	public void setDependency_tree(ArrayList<DependencyRelation> dependency_tree) {
		this.dependency_tree = dependency_tree;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder("");
		
		result.append("original tokens:\n");
		for (String ot : original_tokens) {
			result.append(ot + " ");
		}
		result.append("\n");
		
		result.append("words and tags:\n");
		for (String wat : words_and_tags) {
			result.append(wat + " ");
		}
		result.append("\n");
		
		result.append("constitute tree:\n");
		result.append(constitute_tree);
		result.append("\n");
		
		result.append("typed dependencies:\n");
		for (DependencyRelation dRelation : dependency_tree) {
			result.append(dRelation.toString() + "\n");
		}
		result.append("\n");
		
		return result.toString();
	}
}
