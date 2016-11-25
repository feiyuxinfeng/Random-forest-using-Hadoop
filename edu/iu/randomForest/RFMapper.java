/**
 * 
 */
package edu.iu.randomForest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.CollectiveMapper;
import org.apache.hadoop.mapreduce.Mapper;

import edu.iu.randomForest.io.DataFrame;
import edu.iu.randomForest.io.RandomForestIO;

/**
 * @author summer
 *
 */
public class RFMapper extends CollectiveMapper<String, String, Object, Object> {
	
	private int numOfTrees, numAttributesToConsider;
	private String testFile;
	private boolean headers;
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		//initialization
		Configuration configuration = context.getConfiguration();

		this.testFile = configuration.get(RFConstants.TEST_DATA_FILE);
		this.numOfTrees = configuration.getInt(RFConstants.NUMBER_OF_TREES, 2);
		this.numAttributesToConsider = configuration.getInt(RFConstants.NUMBER_ATTR_TO_CONSIDER, 2);
		this.headers = configuration.getBoolean(RFConstants.HAS_HEADERS, true);
	}
	
	protected void mapCollective( KeyValReader reader, Context context) throws IOException, InterruptedException {
		List<String> dataFiles = new ArrayList<String>();
	    while (reader.nextKeyValue()) {
	    	String key = reader.getCurrentKey();
	    	String value = reader.getCurrentValue();
	    	LOG.info("Key: " + key + ", Value: " + value);
	    	
	    	System.err.println("File : "+value);
	    	
	    	dataFiles.add(value);
	    }
	    Configuration conf = context.getConfiguration();
	    constructRF(dataFiles, conf, context);
	}

	private void constructRF(List<String> dataFiles, Configuration conf,
			Mapper<String, String, Object, Object>.Context context) {
		// TODO Auto-generated method stub
		// initialize RandomForestIO
		RandomForestIO.init(conf);
		
		DataFrame[] dataFrame = new DataFrame[dataFiles.size()];
		int i=0;
		for(String filePath : dataFiles) {
			// ask the user for presence of headers
			dataFrame[i++] = RandomForestIO.loadData(filePath, true, this.headers);
		}
		
		DataFrame testDataFrame = RandomForestIO.loadData(this.testFile, false, this.headers);
		for(DataFrame trainDataFrame : dataFrame)
			RandomForest.runRF(trainDataFrame, testDataFrame, this.numOfTrees, this.numAttributesToConsider);
	}
	
	    
	
	
}
