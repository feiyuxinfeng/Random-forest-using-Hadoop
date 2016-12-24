/**
 * 
 */
package edu.iu.randomForest.io;

import org.apache.hadoop.conf.Configuration;

/**
 * @author summer
 *
 */

public class RandomForestIO {

	private static String trainingDatasetPath, testingDatasetPath ;
	private static Configuration config;
	
	public static void init(Configuration config){
		RandomForestIO.config = config;
	}

	/**
	 * 
	 * @param filepath - file path on the hdfs
	 * @param mode	 - 	true : train
	 * 					false: test
	 * @param headers -  will help to determine the reading strategy from the hdfs.
	 * @return
	 */
	public static DataFrame loadData(String filepath, boolean mode, boolean headers){
		
		if(mode == true)
			RandomForestIO.trainingDatasetPath = filepath;
		else
			RandomForestIO.testingDatasetPath = filepath;

		return new DataFrame(filepath, headers);
	}
	public static Configuration getConfig() {
		return config;
	}
	
}
