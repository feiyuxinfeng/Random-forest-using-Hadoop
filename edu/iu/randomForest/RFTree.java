/**
 * 
 */
package edu.iu.randomForest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.iu.randomForest.RTNode;
import edu.iu.randomForest.RandomForest;
import edu.iu.randomForest.io.DataFrame;
/**
 * @author summer
 *
 */
public class RFTree {
	private int leafClassThreshold=5;
	private List<Integer> attributes;
	private int numberofAttributes;
	private RTNode root;
	
	int getHeight(RTNode node) {
		 if (node == null) return -1;
	 		return 1 + Math.max(getHeight(node.getLeftChild()), getHeight(node.getRightChild()));
	}

	public void constructTree(DataFrame data){
		RTNode root = null;
		if(!data.getValues().isEmpty()){
			root = new RTNode();
			root.setData(data);
			root.setLeafNode(false);
			this.numberofAttributes = data.attributeSize();
			generateNode(root);
			//this.attributes = data.getAttributes();
			//this.numberofAttributes = attributes.size();
			
			this.root = root;
			System.err.println("Height of the tree is "+getHeight(this.root));
		}
		
	}
	
	private void generateNode(RTNode node){
		//check if the node can be assigned as leaf
		//if not, for each attribute do:
			//sort the data by the values of that attribute.
			//check for the rows where the class label changes
			//for each position where class label changes, check if the split at that point is best split.
			//assign the node with the attribute and attribute value to split on.
		//call the same function recursively
		double minEntropy = Double.MAX_VALUE;
		if(!node.isLeafNode()){
			if(checkIfLeaf(node)){
				node.setClassLabel(getClassLabel(node));
				node.setLeafNode(true);
				return;
			}
			
			this.attributes = subsampleAttributes();
			
			for(int attribute : attributes){
				getDataSortedByAttribute(node.getData().getValues(), attribute);
				List<Integer> classLabelChangeIndexes = getClassLabelChangeRows(node.getData().getValues());
				for(int labelChangeRow: classLabelChangeIndexes){
					double currentEntropy =checkSplit(node, labelChangeRow, minEntropy, attribute);
					System.err.println("[Node Info: "+"Root(isleaf) - "+node.isLeafNode()+", Left (isleaf) - "+node.getLeftChild().isLeafNode()+", Right (isleaf) - "+node.getRightChild().isLeafNode()) ;
					if(currentEntropy < minEntropy)
						minEntropy = currentEntropy;
				}
			}
			//attributes.remove(node.getSplitValue());
			//check if left child can be a leaf node
			RTNode leftChild = node.getLeftChild();
			if(leftChild.getData().getValues().size() < leafClassThreshold){
				int classLabel = getMajorityClass(leftChild.getData().getValues());
				leftChild.setClassLabel(classLabel);
				leftChild.setLeafNode(true);
			}else{
				leftChild.setLeafNode(false);
				generateNode(node.getLeftChild());
			}
			
			//check if right child can be a leaf node
			RTNode rightChild = node.getRightChild();
			if(rightChild.getData().getValues().size() < leafClassThreshold){
				int classLabel = getMajorityClass(rightChild.getData().getValues());
				rightChild.setClassLabel(classLabel);
				rightChild.setLeafNode(true);
			}else{
				rightChild.setLeafNode(false);
				generateNode(node.getRightChild());
			}
			
			
		}
		
	}
	
	private int getMajorityClass(List<int[]> data) {
		int class1Count = 0;
		int class0Count = 0;
		for(int i=0; i<data.size(); i++){
			if(data.get(i)[numberofAttributes-1]==0){
				class0Count++;
			}else{
				class1Count++;
			}
		}
		return class0Count > class1Count ? 0 : 1;
	}

	private double checkSplit(RTNode node, int labelChangeRow, double minEntropy, int attribute) {
		List<int[]> leftNodeData = getLeftNodeData(node.getData().getValues(), labelChangeRow);
		List<int[]> rightNodeData = getRightNodeData(node.getData().getValues(), labelChangeRow);
		double leftNodeEntropy = getEntropy(leftNodeData);
		double rightNodeEntropy = getEntropy(rightNodeData);
		double totalEntropy = (leftNodeData.size() * leftNodeEntropy + rightNodeData.size()*rightNodeEntropy)/node.getData().size();
		if(totalEntropy < minEntropy){
			node.setSplitValue(node.getData().getValues().get(labelChangeRow)[attribute]);
			node.setSplitAttributeIndex(attribute);
			RTNode leftNode = new RTNode();
			RTNode rightNode = new RTNode();
			if(leftNodeData.isEmpty() || rightNodeData.isEmpty()){
				System.err.println("Left node or right node data is empty!!!");
			}
			leftNode.setData(new DataFrame(node.getData().getAttributes(), leftNodeData, node.getData().isHeaders()));
			rightNode.setData(new DataFrame(node.getData().getAttributes(), rightNodeData, node.getData().isHeaders()));
			node.setLeftChild(leftNode);
			node.setRightChild(rightNode);
		}else{
			totalEntropy = minEntropy;
		}
		return totalEntropy;
	}
	
	private List<int[]> getRightNodeData(List<int[]> data, int labelChangeRow) {
		List<int[]> rightNodeData = new ArrayList<int[]>();
		for(int i=labelChangeRow; i<data.size(); i++){
			rightNodeData.add(data.get(i));
		}
		return rightNodeData;
	}

	private List<int[]> getLeftNodeData(List<int[]> data, int labelChangeRow) {
		List<int[]> leftNodeData = new ArrayList<int[]>();
		for(int i=0; i<labelChangeRow; i++){
			leftNodeData.add(data.get(i));
		}
		return leftNodeData;
	}

	private double getEntropy(List<int[]> data){
		double logofTwo = Math.log(2);
		double numberofClass1Records =0.0;
		double numberofClass0Records = 0.0;
		double entropy = 0.0;
		for(int i =0; i<data.size(); i++){
			if(data.get(i)[numberofAttributes-1] == 0)
				numberofClass0Records++;
			else
				numberofClass1Records++;
		}
		
		if(numberofClass0Records == 0 || numberofClass1Records == 0)
			return entropy;
		else{
			entropy = numberofClass0Records*Math.log(numberofClass0Records)/logofTwo + numberofClass1Records*Math.log(numberofClass1Records)/logofTwo;
			entropy = entropy *-1;
			return entropy;
		}
	}

	private List<Integer> getClassLabelChangeRows(List<int[]> data) {
		// TODO Auto-generated method stub
		List<Integer> classLabelChangeIndexes = new ArrayList<Integer>();
		int prevClassLabel = data.get(0)[numberofAttributes-1];
		for(int i=1; i<data.size(); i++){
			if(prevClassLabel != data.get(i)[numberofAttributes-1]){
				prevClassLabel = data.get(i)[numberofAttributes-1];
				classLabelChangeIndexes.add(i);
			}
		}
		
		return classLabelChangeIndexes;
	}

	private void getDataSortedByAttribute(List<int[]> data, final int attribute) {
		Collections.sort(data, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				// TODO Auto-generated method stub
				return o1[attribute] - o2[attribute];
			}
		});
	}

	private boolean checkIfLeaf(RTNode node){
		List<int[]> data = node.getData().getValues();
		Integer classLabel = data.get(0)[numberofAttributes-1];
		for(int i=1; i<data.size(); i++){
			if(data.get(i)[numberofAttributes-1] != classLabel){
				return false;
			}
		}
		return true;
	}
	
	private int getClassLabel(RTNode node){
		return node.getData().getValues().get(0)[numberofAttributes-1];
	}
	
	public List<Integer> testTree(List<int[]> testData){
		List<Integer> predictions = new ArrayList<Integer>();
		for(int[] sample : testData){
			predictions.add(predictLabel(sample));
		}
		return predictions;
	}
	
	public int predictLabel(int[] testData){
		RTNode currentNode = this.root;
		if(currentNode == null){
			System.err.println("Root is null");
			return -1;
		}
		System.err.println("Root value: "+this.root.isLeafNode()+"Root split value="+this.root.getSplitValue());
		while(true){
			try{

				if(currentNode == null){
					break;
				}

				if(currentNode.isLeafNode()){
					System.err.println("current node is leaf, class label : "+currentNode.getClassLabel());
					return currentNode.getClassLabel();
				}
				else if(testData[currentNode.getSplitAttributeIndex()] < currentNode.getSplitValue()){
					if(currentNode.getLeftChild() == null){
						System.err.println("Left child of this node is null");
						System.err.println("Node is leaf: "+currentNode.isLeafNode()+"Node split value: "+currentNode.getSplitValue());
						break;
					}else
						currentNode = currentNode.getLeftChild();
				}else{
					if(currentNode.getRightChild() == null){
						System.err.println("Right child of this node is null");
						System.err.println("Node is leaf: "+currentNode.isLeafNode()+"Node split value: "+currentNode.getSplitValue());
						break;
					}else
						currentNode = currentNode.getRightChild();
				}
			}
			catch(NullPointerException np){
				System.err.println("Current node might be null.");
				np.printStackTrace();
			}
		}
		return -1;
	}
	
	public double determineOOBError(List<int[]> testData){
		
		double correct = 0.0;
		double error= 0.0;
		for(int[] sample : testData){
			int predictedLabel = predictLabel(sample);
			if(sample[sample.length-1] == predictedLabel){
				correct++;
			}
		}
		error = 1 - correct/testData.size();
		return error;
	}
	
	public void getVariableImportance(){
		
	}
	
	private List<Integer> subsampleAttributes() {
		
		int attributeSize = RandomForest.attributeSize;
		int attributesToConsider = RandomForest.attributesToConsider;
		int[] selected = new int[attributeSize];
		
		for(int k=0;k<attributesToConsider;) {
			// int a=(int)Math.floor(Math.random()*RandomForest.M);
			int attr_idx = (int)Math.floor(Math.random()*attributeSize);
			if(selected[attr_idx] == 0){
				selected[attr_idx] = 1;
				k++;
			}
		}
		
		List<Integer> selectedAttributes=new ArrayList<Integer>(attributesToConsider);
		
		for (int i=0;i<attributeSize;i++)
			if (selected[i]==1)
				selectedAttributes.add(i);
		return selectedAttributes;
		
	}
	
}
