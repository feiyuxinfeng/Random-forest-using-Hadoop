package edu.iu.randomForest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class Utils {

	private static int[][] recordsPerClass;
	private static BufferedWriter[] fileWriters; 
	
	private static void init(int numMapTasks, FileSystem fs, Path dataDir){
		try {
			if(fs.exists(dataDir))
				fs.delete(dataDir, true);
			if(!fs.mkdirs(dataDir))
				throw new IOException("mkdirs failed to create "+dataDir.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.err.println("In init. Created the directories.");
		Utils.fileWriters =  new BufferedWriter[numMapTasks];
		
		for(int i=0;i<numMapTasks;i++) {
			try {
				Utils.fileWriters[i] = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(dataDir, RFConstants.DATA_FILE_PREFIX+i), true)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.err.println("Created "+Utils.fileWriters.length+" BufferedWriter objects");
	}
	
	private static void writeTrainData(String trainDatasetPath, Path trainDataDir, int numMapTasks, FileSystem fs) throws IOException{
		BufferedReader br = null;
		String line = "";
		//String[] columns = null;
		int i = 0;
		
		Utils.recordsPerClass = new int[numMapTasks][2];
		System.err.println("INFO: Writing training data");
		try{
			br = new BufferedReader(new FileReader(trainDatasetPath));
			while((line = br.readLine()) != null) {
				if(i==0) {
					// first line
					// get the column headers
					//columns = line.split(",");
					init(numMapTasks, fs, trainDataDir);
					writeHeaders(line);
					
				}else{
					String[] values = line.split(",");
					String classLabel = values[values.length-1];
					int idx = Utils.getFileIndex(classLabel);
					
					writeData(line, idx);
					Utils.recordsPerClass[idx][Integer.parseInt(classLabel)]++;
				}
				i++;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		br.close();
		flushAndClose();
		
	}
	
	private static void writeTestData(String testDatasetPath, Path testDataDir, FileSystem fs){

		System.err.println("INFO: Writing testing data.");
//		try{
//			// Check data directory
//		    if (fs.exists(testDataDir)) {
//		    	fs.delete(testDataDir, true);
//		    }
//		    System.err.println("Test data path : "+testDatasetPath);
//		    Path testDataPath = new Path(testDatasetPath);
//		    System.err.println("Test path "+testDataPath.toString());
//		    System.err.println("Test data dir : "+testDataDir.toString());
//			fs.copyFromLocalFile(testDataPath, testDataDir);
//			
//			fs.rename(arg0, arg1)
//		}catch(Exception e){
//			e.printStackTrace();
//			
//		}
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			String line = null;
			
			try {
				if(fs.exists(testDataDir))
					fs.delete(testDataDir, true);
				if(!fs.mkdirs(testDataDir))
					throw new IOException("mkdirs failed to create "+testDataDir.toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			br = new BufferedReader(new FileReader(testDatasetPath));
			bw = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(testDataDir, RFConstants.TEST_DATA_FILE), true)));
			while((line = br.readLine()) != null) {
				bw.write(line);
				bw.newLine();
			}
			br.close();
			bw.flush();
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
				
	}
	
	public static void writeToHDFS(String trainDatasetPath, Path trainDataDir, String testDatasetPath, Path testDataDir,
			int numMapTasks, FileSystem fs) throws IOException{
		
		writeTrainData(trainDatasetPath, trainDataDir, numMapTasks, fs);
		writeTestData(testDatasetPath, testDataDir, fs);
		
	}

	private static void flushAndClose() {
		// TODO Auto-generated method stub
		for(int i=0;i<Utils.fileWriters.length;i++) {
			try {
				Utils.fileWriters[i].flush();
				Utils.fileWriters[i].close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	private static void writeHeaders(String line) {
		// TODO Auto-generated method stub
		
		System.err.println("Trying to write Headers");
		for(int i=0;i<Utils.fileWriters.length;i++) {
			writeData(line, i);
		}
	}

	private static void writeData(String data, int bwIdx){
		
		try {
			Utils.fileWriters[bwIdx].write(data);
			Utils.fileWriters[bwIdx].newLine();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static int getFileIndex(String classLabel) {
		int idx = 0;
		int classL = Integer.parseInt(classLabel);
		for(int i=1; i<Utils.recordsPerClass.length; i++) {
			if(Utils.recordsPerClass[idx][classL] > Utils.recordsPerClass[i][classL]) {
				idx = i;
			}
		}
		return idx;
	}


}
