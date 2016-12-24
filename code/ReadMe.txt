# STEPS FOR INSTALLATION

# 1. Pre-requisite: 
Hadoop 2.6.0 and Harp 3.0 are installed on your machine.
Make sure you've apache ant 1.9.7 and jdk 1.8.0_91

# 2. Setting environment variables:
export HARP3_PROJECT_HOME=<path_to_Harp_dir>
export JAVA_HOME=<path_to_jdk1.8.0_91>
export ANT_HOME=<path_to_apache_ant1.9.7>
export HADOOP_HOME=<path_to_Hadoop_dir>
source $HADOOP_HOME/etc/hadoop/hadoop-env.sh
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export PATH=$HADOOP_HOME/bin:$ANT_HOME/bin:$JAVA_HOME/bin:$SCALA_HOME/bin:$PATH
export HADOOP_MAPRED_HOME=$HADOOP_HOME
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_HDFS_HOME=$HADOOP_HOME
export HADOOP_YARN_HOME=$HADOOP_HOME
export YARN_HOME=$HADOOP_HOME
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_HOME/lib/native

#3. Compile
cd $HARP3_PROJECT_HOME/harp3-app
ant
cp build/harp-app-hadoop-2.6.0.jar $HADOOP_HOME

#STEPS TO RUN

#1
cd $HADOOP_HOME

#2
hadoop jar harp3-app-hadoop-2.6.0.jar edu.iu.randomForest.RFMapCollective <training_dir> <train_hdfs_dir> <testfile> <testfile_hdfs> <outputdir_hdfs> <number of map tasks> <number of attributes> <number of trees> <header>

options:
<training_dir>  - local file directory path for training dataset which can contain one or more training files
<train_hdfs_dir> - directory on hdfs for saving training data
<testfile> - local file path of the testfile
<testfile_hdfs> - directory on hdfs for saving testfile
<outputdir_hdfs> - directory on hdfs for output
<number of map tasks> - number of map task to execute
<number of attributes> - number of attributes to consider out of the overall attributes
<number of trees> - number of trees in the random forest
<header> - true if dataset has headers (column names)

#EXAMPLE
[askarand@j-067 hadoop-2.6.0]$ hadoop jar harp3-app-hadoop-2.6.0.jar edu.iu.randomForest.RFMapCollective /N/u/askarand/dataset/Dataset/train/ /randomForestTraining /N/u/askarand/dataset/Dataset/test/test.csv /randomForestTesting /randomForestOutput_10 4 5 2 true

#RESULTS
Total Harp RandomForest Execution Time: 34214

[askarand@j-067 hadoop-2.6.0]$ hdfs dfs -cat /randomForestOutput_10/rf_out/part-m-00001
16/11/30 11:30:33 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
	 Predicted 
 Actual (Yes) | TP: 4168 | FN 649| 
       (No)  | FP: 0 | TN 10178|	   
Accuracy: 95.671890%