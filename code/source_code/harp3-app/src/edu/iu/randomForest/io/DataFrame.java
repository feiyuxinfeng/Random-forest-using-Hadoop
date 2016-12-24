/**
 * 
 */
package edu.iu.randomForest.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.iu.randomForest.RandomForest;

/**
 * @author summer
 *
 */
public class DataFrame{
	
	private String[] attributes;
	// dneed of a hashmap for mapping the attributed names to indices?
	private List<int[]> values;
	private int[] attributes_idx;
	private boolean headers;
	
	//TODO remove this later
	private int dataSize;
	
	
	public DataFrame(String pathToDataset, boolean headers) {
		super();
		this.headers = headers;
		this.values = new ArrayList<int[]>();
		System.err.println("Reading data now !!! ");
		readData(pathToDataset);
		
	}
	
	public DataFrame(String[] attributes, List<int[]> values, boolean headers) {
		this.attributes = attributes;
		this.values = values;
		this.headers = headers;
		this.attributes_idx = new int[attributes.length];
		for(int i=0;i<this.attributes.length;i++){
			this.attributes_idx[i] = i;
		}
	}
	
	public DataFrame(DataFrame dataFrame){
		this.attributes = dataFrame.attributes;
		this.values = dataFrame.values;
		this.headers = dataFrame.headers;
		this.attributes_idx = new int[attributes.length];
		for(int i=0;i<this.attributes.length;i++){
			this.attributes_idx[i] = i;
		}
	}
	
	
	
	
	
	/**
	 * Key assumptions:
	 * The file should be strictly comma separated.
	 * And the last column of the file should be CLASS label.
	 * All the values are integers.
	 * @param pathToDataset
	 */
	private void readData(String pathToDataset) {
		// TODO Auto-generated method stub
		System.err.println("Path : "+pathToDataset);
		Path cPath = new Path(pathToDataset);
		FileSystem fs = null;
		try {
			fs = FileSystem.get(RandomForestIO.getConfig());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FSDataInputStream in = null;
		try {
			in = fs.open(cPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader( new InputStreamReader(in));
		System.err.println("BufferedReader is ready.");
		String line = null;
		int i=0;
		try {
			while((line = br.readLine()) != null) {
				//System.err.println("Line: "+line);
				if(line.trim().length()==0)
					break;
				String[] data = line.split(",");
				if(i==0) {
					this.attributes_idx = new int[data.length];
					// column headers
					if(this.headers){
						this.attributes = data;
						
						//System.err.println("Headers present");
						//System.err.println("Read the columns for file "+cPath.toString());
					}else{
						//System.err.println("Headers not present");
						this.attributes = new String[data.length];
						int j=0;
						for(;j<attributes.length-1;j++){
							this.attributes[j] = "a_"+j;
						}
						this.attributes[j] = "CLASS";
					}
					for(int l=0;l<this.attributes.length;l++){
						this.attributes_idx[l] = l;
					}
//					for(int l=0;l<this.attributes_idx.length;l++){
//						// fill up the attribute indices
//						this.attributes_idx[l] = 0;
//					}
				}else{
					// values from the dataset
					// create and array of integers. 
					// assign values in them and then add it to the values ArrayList.
					int[] vals = new int[data.length];
					for(int j=0;j<data.length-1;j++){
						vals[j] = Integer.parseInt(data[j]);
					}
					if(data[data.length-1].equals("0")){
						// replacing the class label 0 by -1
						//System.err.println("Class label changed to -1");
						vals[data.length-1] = -1;
					}else{
						//System.err.println("Class label changed to 1");
						vals[data.length-1] = 1;
					}
					// finally add it to the ArrayList values variable
					this.values.add(vals);
				}
				i++;
			}
			System.err.println("Data written!");
		} catch (NumberFormatException | IOException | NullPointerException e) {
			// TODO Auto-generated catch block
			System.err.println("ERROR while reading.");
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException | NullPointerException e) {
			// TODO Auto-generated catch block
			System.err.println("ERROR while closing br.");
			e.printStackTrace();
		}
		System.err.println("Done with reading.");
	}
	
	/**
	 * Returns the size of the data
	 * @return
	 */
	public int size(){
		return this.values.size();
	}
	
	/**
	 * Returns the size of the attributes.
	 * @return
	 */
	public int attributeSize(){
		return this.attributes.length;
	}
	
	/**
	 * Returns the values for the specified index parameter.
	 * @param idx - the index of the record to be retrieved. 
	 * 				0 <= idx < size of the dataframe
	 * @return
	 */
	public int[] getRecord(int idx) {
		if(idx < 0 || idx >= this.values.size()){
			// index out of range, so return null.
			return null;
		}
		return this.values.get(idx);
	}
	
	/**
	 * Returns the index of the attribute string.
	 * @param attribute
	 * @return
	 */
	private int getAttributedIndexMapping(String attribute) {
		int attr_idx =-1, i = 0;
		for(String attr : this.attributes){
			if(attr.equals(attribute)){
				attr_idx = i;
				break;
			}
			i++;
		}
		return attr_idx;
	}
	
	public int[] getRecord(int idx, int attr_idx) {
		if(idx < 0 || idx >= this.values.size()){
			// index out of range, so return null.
			return null;
		}
		if(attr_idx < 0 || attr_idx >= this.attributes.length){
			// attribute index out of range
			return null;
		}
		int[] val = {this.getRecord(idx)[attr_idx]};
		return val;
		
		
	}
	
	/**
	 * Returns the value at the specified row index (idx) and
	 * for the specified attribute name
	 * @param idx - row of interest
	 * @param attribute - attibute name of interest
	 * @return
	 */
	public int[] getRecord(int idx, String attribute){
		if(idx < 0 || idx >= this.values.size()){
			// index out of range, so return null.
			return null;
		}
		int attr_idx = -1;
		if((attr_idx = this.getAttributedIndexMapping(attribute))==-1){
			return null;
		}
		int[] val = {this.getRecord(idx)[attr_idx]};
		return val;
	}

	
	public String[] getAttributes() {
		return attributes;
	}

//	public void setAttributes(String[] attributes) {
//		this.attributes = attributes;
//	}

	public List<int[]> getValues() {
		return values;
	}

//	public void setValues(ArrayList<int[]> values) {
//		this.values = values;
//	}
	/**
	 * Changed the getter to return the ArrayList of attribute indices.
	 * @return
	 */
	public List<Integer> getAttributes_idx() {
		List<Integer> attr_idx = new ArrayList<Integer>();
		for(int ele : this.attributes_idx)
			attr_idx.add(ele);
		return attr_idx;
	}

//	public void setAttributes_idx(int[] attributes_idx) {
//		this.attributes_idx = attributes_idx;
//	}

	public boolean isHeaders() {
		return headers;
	}

//	public void setHeaders(boolean headers) {
//		this.headers = headers;
//	}
}
