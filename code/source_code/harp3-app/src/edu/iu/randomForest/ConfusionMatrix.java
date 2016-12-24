/**
 * 
 */
package edu.iu.randomForest;

/**
 * @author summer
 *
 */
public class ConfusionMatrix {
	private static int truePositive, trueNegative, falsePositive, falseNegative;

	/*public ConfusionMatrix(int truePositive, int trueNegative, int falsePositive, int falseNegative) {
		super();
		ConfusionMatrix.truePositive = truePositive;
		ConfusionMatrix.trueNegative = trueNegative;
		ConfusionMatrix.falsePositive = falsePositive;
		ConfusionMatrix.falseNegative = falseNegative;
	}*/
	
	public static int[][] getConfusionMatrix(int truePositive, int trueNegative, int falsePositive, int falseNegative){
		int[][] confMatrix = new int[2][2];
		confMatrix[0][0] = truePositive;
		confMatrix[0][1] = falseNegative;
		confMatrix[1][1] = trueNegative;
		confMatrix[1][0] = falsePositive;
		return confMatrix;
	}
}
