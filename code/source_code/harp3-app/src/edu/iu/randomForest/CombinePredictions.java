/**
 * 
 */
package edu.iu.randomForest;

import edu.iu.harp.partition.PartitionCombiner;
import edu.iu.harp.partition.PartitionStatus;
import edu.iu.harp.resource.DoubleArray;

/**
 * @author summer
 *
 */
public class CombinePredictions extends PartitionCombiner<DoubleArray> {

	@Override
	public PartitionStatus combine(DoubleArray predcition1, DoubleArray prediction2) {
		// TODO Auto-generated method stub
		
		double[] predictArr1 = predcition1.get();
		int size1 = predictArr1.length;
		double[] predictArr2 = prediction2.get();
		int size2 = predictArr2.length;
		if(size1!=size2){
			return PartitionStatus.COMBINE_FAILED;
		}
		
		for(int i=0;i<size1;i++) {
			double val1 = predictArr1[size1-2] * (1-predictArr1[size1-1]);
			double val2 = predictArr2[size2-2] * (1-predictArr2[size2-1]);
			//System.err.println("Val1, val2 = "+ val1 +"  "+ val2);
			predictArr1[size1-2] = val1 + val2;
			predictArr1[size1-1] = 0.0;
		}
		return PartitionStatus.COMBINED;
	}

}
