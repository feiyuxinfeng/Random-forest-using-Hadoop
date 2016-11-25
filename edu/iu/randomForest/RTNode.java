/**
 * 
 */
package edu.iu.randomForest;
import edu.iu.randomForest.io.DataFrame;
/**
 * @author summer
 *
 */

public class RTNode {

	private DataFrame data;
	//private List<int[]> data;
	private RTNode leftChild;
	private RTNode rightChild;
	private int splitAttributeIndex;
	private int splitValue;
	private boolean isLeafNode;
	private int classLabel;
	
	/*public List<int[]> getData() {
		return data;
	}
	public void setData(List<int[]> data) {
		this.data = data;
	}*/
	public DataFrame getData() {
		return data;
	}
	public void setData(DataFrame data) {
		this.data = data;
	}
	public RTNode getLeftChild() {
		return leftChild;
	}
	public void setLeftChild(RTNode leftChild) {
		this.leftChild = leftChild;
	}
	public RTNode getRightChild() {
		return rightChild;
	}
	public void setRightChild(RTNode rightChild) {
		this.rightChild = rightChild;
	}
	public int getSplitAttributeIndex() {
		return splitAttributeIndex;
	}
	public void setSplitAttributeIndex(int splitAttributeIndex) {
		this.splitAttributeIndex = splitAttributeIndex;
	}
	public int getSplitValue() {
		return splitValue;
	}
	public void setSplitValue(int splitValue) {
		this.splitValue = splitValue;
	}
	public boolean isLeafNode() {
		return isLeafNode;
	}
	public void setLeafNode(boolean isLeafNode) {
		this.isLeafNode = isLeafNode;
	}
	public int getClassLabel() {
		return classLabel;
	}
	public void setClassLabel(int classLabel) {
		this.classLabel = classLabel;
	}
	
}
