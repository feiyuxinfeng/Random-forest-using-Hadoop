/**
 * 
 */
package edu.iu.randomForest;

/**
 * @author summer
 *
 */
import java.util.List;

public class TreePrediction {

	private double oobError;
	private List<Integer> predictions;
	public double getOobError() {
		return oobError;
	}
	public void setOobError(double oobError) {
		this.oobError = oobError;
	}
	public List<Integer> getPredictions() {
		return predictions;
	}
	public void setPredictions(List<Integer> predictions) {
		this.predictions = predictions;
	}
}

