package org.kit.energy

import java.io.FileNotFoundException

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.{SparkContext, sql}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{col, concat_ws, udf}
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 02.02.2017.
  */
@Component
class CSVDataPreperator extends DataPreperator{

  //(dataPath: String, hasHead: Boolean, delimeter: String, labelIndex: String, featuresIndex: String, spark: SparkSession)
  def prepareDataset(input: InputFile, spark: SparkSession): sql.DataFrame = {

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    val csvFile =  input.asInstanceOf[CSVFile]

    // read the csv properties
    val hasHead = csvFile.isHasHeader
    val delimeter = csvFile.getDelimeter
    val labelIndex = csvFile.getIndices
    val featuresIndex = csvFile.getIndices
    val dataPath = csvFile.getDataPath

    var finalData = spark.emptyDataFrame
    val pastIndex = 2
    val horizont = 1

    try {

      // read the dataset
      val inputData = spark.read
        .format("com.databricks.spark.csv")
        .option("header", hasHead)
        .option("sep", delimeter)
        .option("mode", "DROPMALFORMED")
        .load(dataPath)

      val shift = 2

      // get all columns of the dataset (Array[String])
      val dataColumns = inputData.columns

      // split feature index string into array => Array[String] ("2,3" => ["2","3"])
      val featuresSplit = featuresIndex.split(",")

      // get column array with only the features in it
      val testArray = new Array[String](featuresSplit.length)
      var counter = 0

      // for each feature, fill the testArray with the names of the feature columns
      for (featureString <- featuresSplit) {
        testArray(counter) = dataColumns.apply(featureString.toInt)
        counter += 1
      }

      val testColumn = dataColumns.apply(0)

      // user defined functions
      val toDouble = udf[Double, String](_.replace(",",".").toDouble)
      //val toVector = udf((i: String) => (Vectors.dense(i.split(",").map(str => str.toDouble)): org.apache.spark.ml.linalg.Vector))
      val toVector = udf((i: String) => (Vectors.dense(i.split(",").map(str => str.replace(",",".").toDouble)): org.apache.spark.ml.linalg.Vector))

      // create a coloumn in which all features are inside, divided by ","
      val preData = inputData
        .withColumn("test", concat_ws(",", testArray.map(str => col(str)): _*))

      if(input.getDataPurpose.equals("label")){
        finalData = preData.withColumn("data", toDouble(preData(dataColumns.apply(labelIndex.toInt)))).select("data")
      }
      else{
        finalData = preData.withColumn("data", toVector(preData("test"))).select("data")
      }

    }
    catch{
      case e: FileNotFoundException => println("File not found")
    }

    return finalData

  }
}
