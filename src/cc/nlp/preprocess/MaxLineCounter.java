package cc.nlp.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cc.nlp.instances.InstanceList;

/**
 * ����Ĺ����Ǽ���ĳ����parse�ļ�(��ݹ�ش����ļ���)�����(һ��һ��)�������ĵ�����
 * �Դ�parse�ļ����ı�Ҫ����ÿһ�仰(ÿһ��)�ĸ�ʽ����
 * simplistic , silly and tedious . 
 * @author cc
 *
 */
public class MaxLineCounter {
	static Log log = LogFactory.getLog(InstanceList.class);
	
	public static int count(String dirPath){
		File currentDir = new File(dirPath);
		int maxLen = Integer.MIN_VALUE;
		
		if(currentDir.isFile()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(currentDir));
				
				String line = reader.readLine();
				while(line != null){
					String[] words = line.trim().split("[ \t]+");
					
					if(words.length > maxLen) {
						maxLen = words.length;
						
						if(maxLen > 300){
							log.info(currentDir.getAbsolutePath() + "\n");
							log.info(line + "\n");
						}
					}
					
					line = reader.readLine();
				}
				
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
			
			return maxLen;
		} else {
			File[] files = currentDir.listFiles();
			for (File file : files) {
				int tmpLen = count(file.getAbsolutePath());
				
				if(tmpLen > maxLen) {
					maxLen = tmpLen;
				}
			}
			
			return maxLen;
		}
		
	}
	
	/**
	 * @param args[0] ��parse���ļ�(���ļ���)������·��
	 * @param args
	 */
	public static void main(String[] args) {
		String dirPath = args[0];
		
		System.out.println("The word number of the longest line is:\t" + count(dirPath));
	}

}
