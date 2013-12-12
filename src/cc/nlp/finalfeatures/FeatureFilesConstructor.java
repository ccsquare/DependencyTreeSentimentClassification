package cc.nlp.finalfeatures;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cc.nlp.instances.DependencyRelation;
import cc.nlp.instances.DependencyRelationTable;
import cc.nlp.instances.Dictionary;
import cc.nlp.instances.Instance;
import cc.nlp.instances.InstanceData;
import cc.nlp.instances.InstanceList;
import cc.nlp.instances.SentenceData;

public class FeatureFilesConstructor {
	static Log log = LogFactory.getLog(FeatureFilesConstructor.class);
	
//	static String parsed_data_dir_path = "G:\\NLPJava\\GraphSentimentClassification\\test\\basic_dependencies\\rt-polaritydata.parsed";
//	static String dictionary_path = "G:\\NLPJava\\GraphSentimentClassification\\test\\basic_dependencies\\dictionary\\dictionary.txt";
//	static String relation_table_path = "G:\\NLPJava\\GraphSentimentClassification\\test\\basic_dependencies\\dictionary\\dependencyRelation.txt";
//	static String feature_dir_path = "G:\\NLPJava\\GraphSentimentClassification\\test\\basic_dependencies\\featureFiles";
	
	public static void main(String[] args) {
		if(args.length != 4) {
			System.err.println("arguments input wrongly!");
			
			return ;
		}
		String parsed_data_dir_path = args[0];
		String feature_dir_path = args[1];
		String dictionary_path = args[2];
		String relation_table_path = args[3];
		
		Runtime runtime = Runtime.getRuntime();
		System.out.println("before run total memory:\t" + runtime.totalMemory()/(1024 * 1024));
		long start = System.currentTimeMillis();

		FeatureFilesConstructor featureManager = constructFeatureManager(parsed_data_dir_path, feature_dir_path, dictionary_path, relation_table_path);
		
		featureManager.constructFeatureFiles();

		long end = System.currentTimeMillis();
		
		System.out.println("total cost is:\t" + (end-start) + "ms");
		System.out.println("after run total memory:\t" + runtime.totalMemory()/(1024 * 1024));
	}
	
	private HashMap<Integer, Integer> feature_occ_doc_num;
	private HashMap<String, HashMap<Integer, Integer>> graph_feature_occ_num;
	private InstanceList instanceList;
	private String dst_dir_path;
	private DependencyRelationTable dependencyRelationTable;
	private Dictionary dictionary;
	
	static FeatureFilesConstructor constructFeatureManager(String data_dir_path, String dst_dir_path, String dictionary_path, String relation_table_path){
		File dst_file = new File(dst_dir_path);
		if(!dst_file.exists()){
			dst_file.mkdir();
		}
		
		FeatureFilesConstructor featureManager = new FeatureFilesConstructor(data_dir_path, dst_dir_path, dictionary_path, relation_table_path);
		
		return featureManager;
	}
	
	public void constructFeatureFiles(){
		System.out.println("caculating feature-occurrence documents's number......");
		caculateFeatureOccDocNum();
		
		System.out.println("count graph edge number......");
		countGraphEdgeNumber();
		
//		printFeatureOccDocNum();
//		printGraphEdgeNumber();
		
		try {
			getDstFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private FeatureFilesConstructor(String data_dir_path, String dst_dir_path, String dictionary_path, String relation_table_path) {
		this.dst_dir_path = dst_dir_path;
		feature_occ_doc_num = new HashMap<Integer, Integer>();
		graph_feature_occ_num = new HashMap<String, HashMap<Integer,Integer>>();
		
		System.out.println("loading dictionary.......");
		dictionary = new Dictionary(dictionary_path);
		for(int i=1; i<=dictionary.size(); i++){
			feature_occ_doc_num.put(i, 0);
		}
		System.out.println("dictionary's size is:\t" + dictionary.size());
		
		System.out.println("loading relation table......");
		dependencyRelationTable = new DependencyRelationTable(relation_table_path);
		System.out.println("relation table's size is:\t" + dependencyRelationTable.size());

		System.out.println("loading instances......");
		instanceList = InstanceList.constructInstanceList(data_dir_path);
		System.out.println("instancelist'size is:\t" + instanceList.size());
	}
	
	
	//for test
	void printFeatureOccDocNum(){
		for (Entry<Integer, Integer> entry : feature_occ_doc_num.entrySet()) {
			int feature = entry.getKey();
			int number = entry.getValue();
			
			if(number != 0){
				System.out.println(dictionary.getFeatureByID(feature)+"(" + feature + "):\t" + number);
			}
		}
	}
	
	public void getDstFiles() throws IOException{
		System.out.println("calculating tf-idf matrix......");
		
		ArrayList<TFIDFFeature> tFIDFMatrix = getTFIDFMatrix();
		
		String tf_idf_matrix_file_path = this.dst_dir_path + File.separator + "X";
		File tf_idf_matrix_file = new File(tf_idf_matrix_file_path);
		
		if(!tf_idf_matrix_file.exists()){
			tf_idf_matrix_file.createNewFile();
		}
		
		PrintWriter tf_idf_writer = new PrintWriter(tf_idf_matrix_file);
		
		for (TFIDFFeature tfidfFeature : tFIDFMatrix) {
			tf_idf_writer.println(tfidfFeature);
		}
		
		tf_idf_writer.close();
		
		System.out.println("calculating label weight matrix......");
		ArrayList<LabelFeature> labelWeightMatrix = getLabelWeightMatrix();
		
		String label_weight_file_path = this.dst_dir_path + File.separator + "Y";
		File label_weight_file = new File(label_weight_file_path);
		
		if(!label_weight_file.exists()){
			label_weight_file.createNewFile();
		}
		
		PrintWriter label_weight_writer = new PrintWriter(label_weight_file);
		
		for (LabelFeature labelWeight : labelWeightMatrix) {
			label_weight_writer.println(labelWeight);
		}
		
		label_weight_writer.close();
		
		
		System.out.println("calculating graph edge weight matrix......");
		ArrayList<GraphEdgeFeature> graphEdges = getGraphEdges();
		
		String graph_edge_file_path = this.dst_dir_path + File.separator + "G";
		File graph_edge_file = new File(graph_edge_file_path);
		
		if(!graph_edge_file.exists()){
			graph_edge_file.createNewFile();
		}
		
		PrintWriter graph_edge_writer = new PrintWriter(graph_edge_file);
		
		Collections.sort(graphEdges);
		for (GraphEdgeFeature graphEdge : graphEdges) {
			graph_edge_writer.println(graphEdge);
		}
		graph_edge_writer.println(dictionary.size() + " " + dictionary.size() + "0.0");
		
		graph_edge_writer.close();
	}
	
	/**
	 * 计算每个特征的tf-idf值，并将计算出的Matrix返回
	 * @return
	 */
	private ArrayList<TFIDFFeature> getTFIDFMatrix(){
		ArrayList<TFIDFFeature> tf_idf_features = new ArrayList<TFIDFFeature>();
		
		for (int instance_index=0; instance_index<instanceList.getInstanceList().size(); instance_index++) {
			Instance instance = instanceList.getInstanceList().get(instance_index);
			
			for (SentenceData sentenceParsedData : instance.getData().getParsedInformations()) {
				//Here involving the concrete feature selection
				ArrayList<String> instance_features = sentenceParsedData.getWords_and_tags();
				HashMap<String, Integer> occ_feature_num = new HashMap<String, Integer>();
				
				//计算每个句子中某个feature出现的次数
				for (String string : instance_features) {
					if(!occ_feature_num.containsKey(string)){
						occ_feature_num.put(string, 1);
					} else {
						occ_feature_num.put(string, occ_feature_num.get(string) + 1);
					}
				}
				
				for (Entry<String, Integer> entry : occ_feature_num.entrySet()) {
					String feature = entry.getKey();
					int number = entry.getValue();
					int feature_id = dictionary.getIDByFeature(feature);
					
					double tf = ((double)(number))/((double)(instance_features.size())); 
					double idf = Math.log(((double)(instanceList.getInstanceList().size()))/((double)(feature_occ_doc_num.get(feature_id))));
					
					double tf_idf = tf * idf;
					
					tf_idf_features.add(new TFIDFFeature(instance_index + 1, feature_id, tf_idf));
				}
			}
		}
		
		return tf_idf_features;
	}
	
	/**
	 * 获取每个instance的label并将得到的list返回
	 * @return
	 */
	private ArrayList<LabelFeature> getLabelWeightMatrix(){
		ArrayList<LabelFeature> labelWeightMatrix = new ArrayList<LabelFeature>();
		
		for (int instance_index=0; instance_index<instanceList.getInstanceList().size(); instance_index++) {
			Instance instance = instanceList.getInstanceList().get(instance_index);
			
			int label = instance.getLabel();
			
			labelWeightMatrix.add(new LabelFeature(instance_index + 1, label, 1));
		
		}
		
		return labelWeightMatrix;
	}
	
	/**
	 * 获取图中的边和其权值，并将得到的list返回
	 * @return
	 */
	private ArrayList<GraphEdgeFeature> getGraphEdges() {
		ArrayList<GraphEdgeFeature> graphEdges = new ArrayList<GraphEdgeFeature>();
		
		for (Entry<String, HashMap<Integer, Integer>> out_entry : graph_feature_occ_num.entrySet()) {
			String[] strings = out_entry.getKey().split("-");
			int r1 = Integer.parseInt(strings[0]);
			int r2 = Integer.parseInt(strings[1]);
			
			int total_count = 0;
			for (Integer integer : out_entry.getValue().values()) {
				total_count += integer;
			}
			
			
			double acc = 0.0;
			for (Entry<Integer, Integer> in_entry : out_entry.getValue().entrySet()) {
				double prob = ((double)(in_entry.getValue() + 1)) / ((double)(total_count + out_entry.getValue().size()));
				
				acc += -(prob * Math.log(prob));
			}
			
			graphEdges.add(new GraphEdgeFeature(r1, r2, acc));
		}
		
		return graphEdges;
	}
	
	//for test
	void printGraphEdgeNumber(){
		System.out.println("graph_edge_number is:\t" + graph_feature_occ_num.size());
		
		for (Entry<String, HashMap<Integer, Integer>> out_entry : graph_feature_occ_num.entrySet()) {
			String[] strings = out_entry.getKey().split("-");
			int r1 = Integer.parseInt(strings[0]);
			int r2 = Integer.parseInt(strings[1]);
			
			log.info("(" + (dictionary.getFeatureByID(r1) + "[" + r1 + "], " + dictionary.getFeatureByID(r2) + "[" + r2 + "]" + ")" ));
			
			for (Entry<Integer, Integer> in_entry : out_entry.getValue().entrySet()) {
				log.info("\t" + in_entry.getKey() + ":\t" + in_entry.getValue());
			}
		}
	}
	
	/**
	 * 计算图中的边在各个label中的出现次数
	 */
	private void countGraphEdgeNumber(){
		for (int instance_index=0; instance_index<instanceList.getInstanceList().size(); instance_index++) {
			Instance instance = instanceList.getInstanceList().get(instance_index);
			
			for (SentenceData sentenceParsedData : instance.getData().getParsedInformations()) {
				//Here involving the concrete feature selection
				ArrayList<String> words_and_tags = sentenceParsedData.getWords_and_tags();
				ArrayList<DependencyRelation> relations = sentenceParsedData.getDependency_tree();
				
				for (DependencyRelation relation : relations) {
					String relation_hash_str = GraphEdgeFeature.getGraphEdgeExpress(relation, words_and_tags, this.dictionary);
					
					//Here involving root relation
					if(relation_hash_str == null) continue;
					
					if(!graph_feature_occ_num.containsKey(relation_hash_str)){
						//当某个图边还没在哈希表中时，先在哈希表中增加该边，并对该边的各个label的出现次数都设置为0
						HashMap<Integer, Integer> label_number = new HashMap<Integer, Integer>();
						for(int i=1; i<=instanceList.getLabelNumber(); i++){
							label_number.put(i, 0);
						}
						graph_feature_occ_num.put(relation_hash_str, label_number);
					}
					
					int label = instance.getLabel();
					
					int old_num = graph_feature_occ_num.get(relation_hash_str).get(label);
					
					graph_feature_occ_num.get(relation_hash_str).put(label, old_num+1);
				}
			}
		}
	}
	
	/**
	 * 计算每个特征出现过的文档数
	 */
	private void caculateFeatureOccDocNum(){
		for (Instance instance : instanceList.getInstanceList()) {
			InstanceData instanceData = instance.getData();
			
			for(SentenceData sentenceParsedData: instanceData.getParsedInformations()){
				//Here involving the concrete feature selection
				ArrayList<String> instance_features = sentenceParsedData.getWords_and_tags();
						
				HashSet<String> just_occ_without_frequency = new HashSet<String>();
				//不考虑每个句子中某个feature出现多次
				for (String word_and_tag : instance_features) {
					just_occ_without_frequency.add(word_and_tag);
				}
				for (String word_and_tag : just_occ_without_frequency) {
					if(dictionary.getIDByFeature(word_and_tag) == -1){
						System.out.println(word_and_tag + " not found");
						
						for (String string : instance_features) {
							System.out.println(string);
						}
					}
					
					int current_feature_id = dictionary.getIDByFeature(word_and_tag);
					feature_occ_doc_num.put(current_feature_id, feature_occ_doc_num.get(current_feature_id)+1);
				}
			}
		}
	}
}
