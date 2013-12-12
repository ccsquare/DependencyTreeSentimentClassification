package cc.nlp.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cc.nlp.parse.XMLMark;

/**
 * 该类是一个静态方法类，功能是从一个已经parse好的文件中(递归处理文件夹)中获取所有的依存关系
 * parse好的文件内容示例如下
 * 
 * <sentence_id = "1">
 * <original_sentence>
 * plot : two teen couples go to a church party , drink and then drive . 
 * </original_sentence>
 * <words_and_tags>
 * plot/NN :/: two/CD teen/JJ couples/NNS go/VB to/TO a/DT church/NN party/NN ,/, drink/VB and/CC then/RB drive/VB ./.
 * 
 * </words_and_tags>
 * 
 * <constitute_tree>
 * (ROOT (NP (NP (NN plot)) (: :) (S (NP (CD two) (JJ teen) (NNS couples)) (VP (VP (VB go) (PP (TO to) (NP (DT a) (NN church) (NN party)))) (, ,) (VP (VB drink)) (CC and) (VP (ADVP (RB then)) (VB drive)))) (. .)))
 * </constitute_tree>
 * 
 * <typed_dependencies>
 * root(ROOT-0, plot-1)
 * num(couples-5, two-3)
 * amod(couples-5, teen-4)
 * nsubj(go-6, couples-5)
 * nsubj(drink-12, couples-5)
 * nsubj(drive-15, couples-5)
 * dep(plot-1, go-6)
 * det(party-10, a-8)
 * nn(party-10, church-9)
 * prep_to(go-6, party-10)
 * dep(plot-1, drink-12)
 * conj_and(go-6, drink-12)
 * advmod(drive-15, then-14)
 * dep(plot-1, drive-15)
 * conj_and(go-6, drive-15)
 * 
 * </typed_dependencies>
 * </sentence_id>
 * 
 * @param 参数说明
 * @param args[0]:已被parsed的文件或文件夹完整路径
 * @param args[1]:目标文件完整路径
 * @author cc
 *
 */
public class DependencyRelationConstructor {
	static Log log = LogFactory.getLog(DependencyRelationConstructor.class);
	
	static final String TYPED_DEPENDENCIES_HEAD = XMLMark.getTypedDependenciesHead();
	static final String TYPED_DEPENDENCIES_TAIL = XMLMark.getTypedDependenciesTail();
	
	static HashSet<String> relationSet = new HashSet<String>();
	
	public static void main(String[] args) {
		String dataDir = args[0];
		String dstFile = args[1];
		
		File root = new File(dataDir);
		
		try {
			PrintWriter writer = new PrintWriter(new File(dstFile));
			
			try {
				processFile(root);
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				
				e.printStackTrace(new PrintWriter(sw));
				log.error(sw.toString());
			}
			
			TreeSet<String> sortedSet = new TreeSet<String>(relationSet);
			
			int i=1;
			for (String string : sortedSet) {
				writer.println(string + " " + (i++));
			}
			writer.println();
			writer.println();
			
			writer.close();
		} catch (FileNotFoundException e1) {
			StringWriter sw = new StringWriter();
			
			e1.printStackTrace(new PrintWriter(sw));
			log.error(sw.toString());
		}
	}
	
	static void processFile(File file) throws IOException{
		if(file.isDirectory()){
			File[] childs = file.listFiles();
			
			for (File file2 : childs) {
				processFile(file2);
			}
		} else {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			boolean flag = false;
			
			String line = reader.readLine();
			while(line != null){
				String current_line = line.trim();
				if(current_line.equals("")) {
					line = reader.readLine();
					continue;
				}
				if(current_line.equals(TYPED_DEPENDENCIES_HEAD)){
					flag = true;
				} else if(line.equals(TYPED_DEPENDENCIES_TAIL)){
					flag = false;
				} else {
					if(flag){
						Pattern p = Pattern.compile("([\\S]+?)\\(.*\\)");
						Matcher m = p.matcher(current_line);
						if(m.matches()){
							String relation = m.group(1);
							
//							Pattern inp = Pattern.compile("[_a-z]*[^_a-z]+[_a-z]*");
//							Matcher inm = inp.matcher(relation);
//							if(inm.matches()){
//								System.out.println(file.getAbsolutePath());
//								System.out.println("relation is " + relation);
//							}
							if(!relationSet.contains(relation)){
								relationSet.add(relation);
							}
						}
					}
				}
				
				line = reader.readLine();
			}
			
			reader.close();
		}
	}
}
