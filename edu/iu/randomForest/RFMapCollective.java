package edu.iu.randomForest;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import edu.iu.fileformat.MultiFileInputFormat;
import edu.iu.randomForest.io.DataFrame;
import edu.iu.randomForest.io.RandomForestIO;

public class RFMapCollective extends Configured implements Tool{

	
	public static void main(String args[]) throws Exception{
		int result = ToolRunner.run(new Configuration(), new RFMapCollective(), args);
		System.exit(result);
	}
	
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
//		if(!validArgs(args)){
//			return -1;
//		}
		
		
		String trainDatasetPath = args[0];
		String trainHdfsPath = args[1];
		String testDatasetPath = args[2];
		String testHdfsPath = args[3];
		String outputHdfsPath = args[4];
		int numMapTasks = Integer.parseInt(args[5]);
		int numOfTrees = Integer.parseInt(args[6]);
		int numAttributesToConsider = Integer.parseInt(args[7]);
		launch(trainDatasetPath, trainHdfsPath, testDatasetPath, testHdfsPath, outputHdfsPath,  numMapTasks, numOfTrees, numAttributesToConsider);
		return 0;
	}



	private void launch(String trainDatasetPath, String trainHdfsPath, String testDatasetPath, String testHdfsPath,
			String outputHdfsPath, int numMapTasks, int numOfTrees, int numAttributesToConsider) throws IOException {
		// TODO Auto-generated method stub
		Configuration configuration = getConf();
		
		Path trainDataDir = new Path(trainHdfsPath);
		Path testDataDir = new Path(testHdfsPath);
		Path outDir = new Path(outputHdfsPath);
		
		
		FileSystem fs = FileSystem.get(configuration);
		// writing the training dataset to the hdfs
		Utils.writeToHDFS(trainDatasetPath, trainDataDir, testDatasetPath, testDataDir, numMapTasks, fs);
		
		//RandomForestIO.init(configuration);
		//ataFrame d = RandomForestIO.loadData(trainHdfsPath, true, true);
		
		runRandomForest(configuration, trainDataDir, testDataDir, outDir, numMapTasks, numOfTrees, numAttributesToConsider);
	}

	
	
	private void runRandomForest(Configuration configuration, Path trainDataDir, Path testDataDir, Path outDir, int numMapTasks, int numOfTrees, int numAttributesToConsider) throws IOException {
		// TODO Auto-generated method stub
		
		System.err.println("Starting Job");
		boolean jobSuccess = true;
		int jobRetryCount = 0;
		
		// create a job configuration
		do {
			// ----------------------------------------------------------------------
			Job rfJob = configureRFJob(configuration, trainDataDir, testDataDir, outDir, numMapTasks, numOfTrees,numAttributesToConsider);
			System.err.println("Job CREATED for REDUCE");
			try {
				jobSuccess =rfJob.waitForCompletion(true);
			} catch (ClassNotFoundException | IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!jobSuccess) {
				System.out.println("Job failed. ");
				jobRetryCount++;
				if (jobRetryCount == 1) {
					break;
				}
			}else{
				break;
			}
		} while (true);
	}

	private Job configureRFJob(Configuration configuration, Path trainDataDir, Path testDataDir, Path outDir, int numMapTasks, int numOfTrees, int numAttributesToConsider) throws IOException {
		// TODO Auto-generated method stub
		
		Job job = Job.getInstance(configuration, "rf_job");
		Configuration jobConfig = job.getConfiguration();
		Path jobOutDir = new Path(outDir, "rf_out");
		FileSystem fs = FileSystem.get(configuration);
		if (fs.exists(jobOutDir)) {
			fs.delete(jobOutDir, true);
		}
		FileInputFormat.setInputPaths(job, trainDataDir);
		FileOutputFormat.setOutputPath(job, jobOutDir);
		Path testFile = new Path(testDataDir,RFConstants.TEST_DATA_FILE);

		
		job.setInputFormatClass(MultiFileInputFormat.class);
		job.setJarByClass(RFMapCollective.class);
		job.setMapperClass(RFMapper.class);
		org.apache.hadoop.mapred.JobConf jobConf = (JobConf) job.getConfiguration();
		jobConf.set("mapreduce.framework.name", "map-collective");
		jobConf.setNumMapTasks(numMapTasks);
		jobConf.setInt("mapreduce.job.max.split.locations", 10000);
		job.setNumReduceTasks(0);
		jobConfig.set(RFConstants.TEST_DATA_FILE,testFile.toString());
		jobConfig.setInt(RFConstants.NUMBER_OF_TREES, numOfTrees);
		jobConfig.setInt(RFConstants.NUMBER_ATTR_TO_CONSIDER,numAttributesToConsider);
		//jobConfig.setInt(KMeansConstants.NUM_CENTROIDS, numOfCentroids);
		//jobConfig.setInt(KMeansConstants.NUM_ITERATONS, numOfIterations);
		return job;
	}

	/**
	 * Arguments would be:
	 * dataset path (location of the file on the disk)
	 * hdfs dir path
	 * number of map tasks
	 * @param args
	 * @return
	 */
	private boolean validArgs(String[] args) {
		// TODO Auto-generated method stub
		if(args.length != 8) {
			System.err.println("Wrong usage!");
			return false;
		}
		
		return true;
	}

}
