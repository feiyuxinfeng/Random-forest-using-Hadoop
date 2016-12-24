/**
 * 
 */
package edu.iu.randomForest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.CollectiveMapper;
import org.apache.hadoop.mapreduce.Mapper;

import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.Table;
import edu.iu.harp.resource.DoubleArray;
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
	
	protected void mapCollective( KeyValReader reader, Context context) throws IOException, InterruptedException  {
		List<String> dataFiles = new ArrayList<String>();
	    while (reader.nextKeyValue()) {
	    	String key = reader.getCurrentKey();
	    	String value = reader.getCurrentValue();
	    	LOG.info("Key: " + key + ", Value: " + value);
	    	
	    	System.err.println("File : "+value);
	    	
	    	dataFiles.add(value);
	    }
	    Configuration conf = context.getConfiguration();
	    try {
			constructRF(dataFiles, conf, context);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void broadcastPredictionTable( Table<DoubleArray> predictionTable) throws IOException{  
		 //broadcast predictions
		  boolean isSuccess = false;
		  try {
			  isSuccess = broadcast("main", "broadcast-predictions", predictionTable, this.getMasterID(),false);
		  } catch (Exception e) {
		      LOG.error("Fail to bcast.", e);
		  }
		  if (!isSuccess) {
		      throw new IOException("Fail to bcast");
		  }
	}
	
	private void constructRF(List<String> dataFiles, Configuration conf,
			Mapper<String, String, Object, Object>.Context context) throws IOException, InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		// initialize RandomForestIO
		RandomForestIO.init(conf);
		System.err.println("RandomForest init call complete: "+this.getSelfID());
		
		DataFrame[] dataFrame = new DataFrame[dataFiles.size()];
		int i=0;
		for(String filePath : dataFiles) {
			// ask the user for presence of headers
			dataFrame[i++] = RandomForestIO.loadData(filePath, true, this.headers);
		}
		//TreePrediction[] predictions = new TreePrediction[dataFiles.size()];
		List<TreePrediction> predictions = new ArrayList<TreePrediction>();
		DataFrame testDataFrame = RandomForestIO.loadData(this.testFile, false, this.headers);
		i=0;
		
		Table<DoubleArray> predictionTable = new Table<>(0, new CombinePredictions());
		System.err.println("Broadcasting prediction table!!!");
		broadcastPredictionTable(predictionTable);
		for(DataFrame trainDataFrame : dataFrame){
			System.err.println("Calling run RF!!!");
			predictions.addAll(RandomForest.runRF(trainDataFrame, testDataFrame, this.numOfTrees, this.numAttributesToConsider));
		}
		for(TreePrediction prediction: predictions){
			populatePredictions(predictionTable, testDataFrame, prediction);

		}
		reduce("main", "reduce", predictionTable, this.getMasterID());
		
		if(this.isMaster()){
			System.err.println("RandomForest: Output confusion matrix.");
			outputConfusionMatrix(testDataFrame, predictionTable, context);
		}
		
	}
	


	private int[][] accountThePredictions(DataFrame testDataFrame, Table<DoubleArray> predictionTable) {
		int trueP=0, trueN=0, falseP=0, falseN=0;
		// iterate on the testDataFrame
		for(int i=0; i<testDataFrame.size();i++){
			double[] values =  predictionTable.getPartition(i).get().get();
			//System.err.println("Predicted value "+values[values.length-2]);
			if(values[values.length-2] < 0.0){
				// predicted is class -1 (negative)
				int[] retVal = null;
				if((retVal = testDataFrame.getRecord(i, "CLASS"))!=null){
					//System.err.println("Actual class "+retVal[0]);
					if(retVal[0] == -1){
						// checking if the actual class label was negative class label
						// if yes, then this account to true negative count
						trueN++;
					}else{
						// else this indicates that the predicted label was negative
						// while the actual label is positive
						// hence it accounts for false negative
						falseN++;
					}
				}
				
			}else{
				// predicted is class 1
				int[] retVal = null;
				if((retVal = testDataFrame.getRecord(i, "CLASS"))!=null){
					//System.err.println("Actual class "+retVal[0]);
					if(retVal[0] == 1){
						// checking if the actual class label was positive class label
						// if yes, then this account to true positive count
						trueP++;
					}else{
						// else this indicates that the predicted label was positive
						// while the actual label is negative
						// hence it accounts for false positive
						falseP++;
					}
				}
			}
			
		}
		
		return ConfusionMatrix.getConfusionMatrix(trueP, trueN, falseP, falseN);
		
	}
	
	private void outputConfusionMatrix(DataFrame testDataFrame, Table<DoubleArray> predictionTable, Context context) {
		// TODO Auto-generated method stub
		int[][] confMatrix = accountThePredictions(testDataFrame, predictionTable);
		float accuracy = (confMatrix[0][0] + confMatrix[1][1]) / (confMatrix[0][0] + confMatrix[0][1] + confMatrix[1][0] + confMatrix[1][1]);
		accuracy *= 100;
		String output="\t Predicted ";
		output += "\n Actual (Yes) | TP: "+ confMatrix[0][0] +" | FN "+confMatrix[0][1]+"| \n";
		output += "       (No)  | FP: "+ confMatrix[1][0] +" | TN "+confMatrix[1][1]+"| \n";
		output += "Accuracy: "+accuracy+"%";
		try {
			context.write(null, new Text(output));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void populatePredictions(Table<DoubleArray> predictionTable, DataFrame testDataFrame, TreePrediction treePrediction) {
		// TODO Auto-generated method stub
		List<Integer> predictionsFromTree = treePrediction.getPredictions();
		double oobError = treePrediction.getOobError();
				
		int vectorSize = testDataFrame.attributeSize()+2;	// +2 for accommodating the predicted label and the oob error
		
		for(int i=0; i<testDataFrame.size();i++) {
			double[] partial = new double[vectorSize];
			int j=0;
			for(; j < vectorSize-2; j++){
				partial[j] =  (double) (testDataFrame.getRecord(i, j)[0]);  
			}
			partial[j++] = (double) predictionsFromTree.get(i);
			partial[j++] = oobError;
			if(predictionTable.getPartition(i) == null) {
				Partition<DoubleArray> tmpPartition = new Partition<DoubleArray>(i, new DoubleArray(partial, 0, vectorSize));
				predictionTable.addPartition(tmpPartition);
			}else{
				// combine predictions from other threads
				double[] partial2 = predictionTable.getPartition(i).get().get();
				if(vectorSize == partial2.length){
					double val1 = partial[vectorSize-2] * (1-partial[vectorSize-1]);
					double val2 = partial2[vectorSize-2] * (1-partial2[vectorSize-1]);
					partial2[vectorSize-2] = val1 + val2;
					partial2[vectorSize-1] = 0.0;
				}
			}
		}
	}
	
	    
	
	
}
