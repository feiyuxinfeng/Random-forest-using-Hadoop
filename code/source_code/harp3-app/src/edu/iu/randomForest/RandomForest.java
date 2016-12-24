package edu.iu.randomForest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import edu.iu.randomForest.ThreadedRFTree;

import edu.iu.randomForest.io.DataFrame;

public class RandomForest {
	public static int attributeSize;
	public static int attributesToConsider;
	public static int numberOfTrees;
	public static int dataSize;
	
	public static List<TreePrediction>  runRF(DataFrame trainDataFrame, DataFrame testDataFrame, int numOfTrees, int numAttributesToConsider) throws InterruptedException, ExecutionException {
		RandomForest.attributeSize = trainDataFrame.attributeSize()-1;		// doesn't count the class label 
		RandomForest.attributesToConsider = numAttributesToConsider;
		RandomForest.numberOfTrees = numOfTrees;
		RandomForest.dataSize = trainDataFrame.size();
		List<TreePrediction> treePrediction = new ArrayList<TreePrediction>();
		List<int[]> trainData = new ArrayList<int[]>();
		List<int[]> testData = new ArrayList<int[]>();
		Set<Future<TreePrediction>> set = new HashSet<Future<TreePrediction>>();
		
		System.err.println("RandomForest runRF() called.");
		ExecutorService executor = Executors.newFixedThreadPool(RandomForest.numberOfTrees);
		for(int j=0; j<RandomForest.numberOfTrees; j++) {
			Callable<TreePrediction> callable = new ThreadedRFTree(trainDataFrame, testDataFrame, RandomForest.attributesToConsider);
			Future<TreePrediction> future = executor.submit(callable);
			set.add(future);
		}
		
		executor.shutdown();
		System.err.println("RandomForest: Executor shutdowm. Waiting for Termination.");
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		for (Future<TreePrediction> future : set) {
		    treePrediction.add(future.get()); 
		}
		return treePrediction;
		
		
		/*subsampleData(trainDataFrame.getValues(), trainData, testData);
		DataFrame subsample = new DataFrame(trainDataFrame.getAttributes(), trainData, trainDataFrame.isHeaders());
		*//**
		 * To make this as multithreaded, encapsulate the following steps into one class that implements Runnable:
		 * 1. subsample - this can be done by this class and the sampled data can be passed as arguments to the run methods of the Thread;
		 * 2. Since every Thread would be responsible for building a random tree, the following LOC, should be put in the the run () method of that Thread.
		 *//*
		
		// this sub sample data would be used for building tree
		
		// number of tree would define the number of RFTrees objects to create
		RFTree rfTree = new RFTree();
		rfTree.constructTree(subsample);
		System.err.println("Generated Tree.");
		rfTree.printTree(rfTree.getRoot());
		double errorRate = rfTree.determineOOBError(testData);
		System.err.println("OOB error: "+errorRate);
		//rfTree.predictClass(testDataFrame.getValues());

		List<Integer> predictions = rfTree.testTree(testDataFrame.getValues());
		treePrediction.setOobError(errorRate);
		treePrediction.setPredictions(predictions);
		return treePrediction;*/
		
		
		
	}
	
	
	/**
	 * Generates bootstrap sample from the data.
	 * @param dataValues
	 * @param trainData
	 * @param testData
	 */
	public static void subsampleData(List<int[]> dataValues,List<int[]> trainData,List<int[]> testData){
		
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
