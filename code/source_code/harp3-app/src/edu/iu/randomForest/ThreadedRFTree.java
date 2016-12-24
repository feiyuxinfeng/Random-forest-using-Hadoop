package edu.iu.randomForest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import edu.iu.randomForest.RFTree;
import edu.iu.randomForest.RandomForest;
import edu.iu.randomForest.TreePrediction;
import edu.iu.randomForest.io.DataFrame;

/**
 * 
 */

/**
 * @author summer
 *
 */
public class ThreadedRFTree implements Callable<TreePrediction> {

	DataFrame trainDataFrame, testDataFrame;
	int numAttributesToConsider;
	public ThreadedRFTree(DataFrame trainDataFrame, DataFrame testDataFrame, int numAttributesToConsider){
		this.trainDataFrame = trainDataFrame;
		this.testDataFrame = testDataFrame;
		this.numAttributesToConsider = numAttributesToConsider;
	}
	
	
	@Override
	public TreePrediction call() throws Exception {
		// TODO Auto-generated method stub
		
		TreePrediction treePrediction = new TreePrediction();
		List<int[]> trainData = new ArrayList<int[]>();
		List<int[]> testData = new ArrayList<int[]>();
		RandomForest.subsampleData(trainDataFrame.getValues(), trainData, testData);
		DataFrame subsample = new DataFrame(trainDataFrame.getAttributes(), trainData, trainDataFrame.isHeaders());
		
		System.err.println("RandomForest: subsample data.");
		/**
		 * To make this as multithreaded, encapsulate the following steps into one class that implements Runnable:
		 * 1. subsample - this can be done by this class and the sampled data can be passed as arguments to the run methods of the Thread;
		 * 2. Since every Thread would be responsible for building a random tree, the following LOC, should be put in the the run () method of that Thread.
		 */
		
		// this sub sample data would be used for building tree
		
		// number of tree would define the number of RFTrees objects to create
		RFTree rfTree = new RFTree();
		rfTree.constructTree(subsample);
		System.err.println("Generated Tree.");
		//rfTree.printTree(rfTree.getRoot());
		double errorRate = rfTree.determineOOBError(testData);
		System.err.println("OOB error: "+errorRate);
		//rfTree.predictClass(testDataFrame.getValues());

		List<Integer> predictions = rfTree.testTree(testDataFrame.getValues());
		treePrediction.setOobError(errorRate);
		treePrediction.setPredictions(predictions);
		System.err.println("RandomForest: Returning predictions.");
		return treePrediction;
		
	}

}
