package org.example;

import io.delta.tables.DeltaTable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.functions;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public final static String READPATH = "file:///Users/I077063/Documents/GitHub/parquet-delta-java/sample-data/currencycodes.snappy.parquet";
    public final static String WRITEPATH = "file:///Users/I077063/Documents/GitHub/parquet-delta-java/sample-data/delta-data";

    public static void main(String[] args) {
        //Create Spark Session
        SparkSession spark = SparkSession
                .builder()
                .master("local[*]")
                .appName("parquet_to_delta")
                .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
                .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
                .getOrCreate();

        StructType schema = getSchema();

        // Read the parquet file
        Dataset<Row> data = readParquet(spark, schema, READPATH);

        // Write the parquet contents into delta table
        writeDelta(spark, data, WRITEPATH);

        // Read the delta table and print the entries
        printDelta(spark, WRITEPATH);
    }

    private static Dataset<Row> readParquet(SparkSession spark, StructType schema, String readPath){
        System.out.println("****************Reading parquet file.....****************");
        Dataset<Row> parquetData = spark.read().schema(schema).parquet(readPath);
        System.out.println("****************Parquet file read complete!****************");
        return parquetData;
    }

    private static void writeDelta(SparkSession spark, Dataset<Row> data, String writePath){
        System.out.println("****************Writing data into delta table.....****************");
        data.write().format("delta")
                .mode("overwrite")
                .save(writePath);
        System.out.println("****************Delta write complete!****************");
    }

    private static void printDelta(SparkSession spark, String deltaPath){
        boolean deltaExists = DeltaTable.isDeltaTable(spark, deltaPath);
        if (deltaExists) {
            System.out.println("****************Printing data from delta table.....****************");
            DeltaTable deltaTable = DeltaTable.forPath(spark, deltaPath);
            Dataset<Row> deltaDF = deltaTable.toDF();
            deltaDF.withColumn("description", functions.expr("filter(name, x -> x.lang = 'en-US')"))
                .drop("name")
                .show(10, false);
        }
    }

    private static StructType getSchema(){
        List<StructField> nameStruct = new ArrayList<>();
        nameStruct.add(DataTypes.createStructField("lang", DataTypes.StringType, true));
        nameStruct.add(DataTypes.createStructField("content", DataTypes.StringType, true));
        ArrayType nameArray = DataTypes.createArrayType(DataTypes.createStructType(nameStruct), true);

        List<StructField> currencyStruct = new ArrayList<>();
        currencyStruct.add(DataTypes.createStructField("internalUUID", DataTypes.StringType, true));
        currencyStruct.add(DataTypes.createStructField("code", DataTypes.StringType, true));
        currencyStruct.add(DataTypes.createStructField("name", nameArray, true));

        return DataTypes.createStructType(currencyStruct);
    }
}