# parquet-delta-java
 
Clone the file to your local repository<br>
`git-clone https://github.com/kartikc13/parquet-delta-java.git`

From the terminal (within root of the project) execute<br>
`gradle clean build`<br>
This should create a folder under the project root as `/build/dependent-libs/` where all the dependent libraries (.jar) for the project exists.

Finally, run the spark submit command (ensure to start the Spark master prior to this)<br>
`$SPARK_HOME/bin/spark-submit --jars ``echo build/dependant-libs/* | tr ' ' ','`` ./build/libs/parquet-delta-java-1.0-SNAPSHOT.jar`<br>
**NOTE:** This works only in Linux systems<br>
$SPARK_HOME --> The location of your spark installation until `/libexec` 
