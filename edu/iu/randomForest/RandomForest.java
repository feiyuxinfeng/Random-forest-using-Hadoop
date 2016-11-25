package edu.iu.randomForest;

import java.util.ArrayList;
import java.util.List;

import edu.iu.randomForest.io.DataFrame;
import edu.iu.randomForest.RFTree;

public class RandomForest {
	public static int attributeSize;
	public static int attributesToConsider;
	public static int numberOfTrees;
	public static int dataSize;
	
	public static void  runRF(DataFrame trainDataFrame, DataFrame testDataFrame, int numOfTrees, int numAttributesToConsider) {
		RandomForest.attributeSize = trainDataFrame.attributeSize()-1;		// doesn't count the class label 
		RandomForest.attributesToConsider = numAttributesToConsider;
		RandomForest.numberOfTrees = numOfTrees;
		RandomForest.dataSize = trainDataFrame.size();
		TreePrediction treePrediction = new TreePrediction();
		List<int[]> trainData = new ArrayList<int[]>();
		List<int[]> testData = new ArrayList<int[]>();
		
		subsampleData(trainDataFrame.getValues(), trainData, testData);
		
		DataFrame subsample = new DataFrame(trainDataFrame.getAttributes(), trainData, trainDataFrame.isHeaders());
		// this sub sample data would be used for building tree
		
		// number of tree would define the number of RFTrees objects to create
		RFTree rfTree = new RFTree();
		rfTree.constructTree(subsample);
		System.err.println("Generated Tree.");
		double errorRate = rfTree.determineOOBError(testData);
		System.err.println("OOB error: "+errorRate);
		List<Integer> predictions = rfTree.testTree(testDataFrame.getValues());
		treePrediction.setOobError(errorRate);
		treePrediction.setPredictions(predictions);
		return treePrediction;
	}
	
	
	/**
	 * Generates bootstrap sample from the data.
	 * @param dataValues
	 * @param trainData
	 * @param testData
	 */
	private static void subsampleData(List<int[]> dataValues,List<int[]> trainData,List<int[]> testData){
		int N = RandomForest.dataSize;
		ArrayList<Integer> recordsToSample = new ArrayList<Integer>(N); 
		int[] selected = new int[N];
		for (int i=0;i<N;i++){
			int idx = (int)Math.floor(Math.random()*N);
			recordsToSample.add(idx);
			trainData.add((dataValues.get(idx)).clone());
			selected[idx] = 1;
		}
		for (int i=0;i<N;i++)
			if (selected[i]==0)
				testData.add((dataValues.get(i)).clone());
	}

	
}
