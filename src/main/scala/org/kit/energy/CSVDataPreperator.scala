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
    val labelIndex = csvFile.getLabelColumnIndex
    val featuresIndex = csvFile.getFeatureColumnsIndexes
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

      println("input schema:")
      inputData.printSchema()

      println("input data:")
      inputData.show()

      // get all columns of the dataset (Array[String])
      val dataColumns = inputData.columns
      println("columns:")
      dataColumns.foreach(println)

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

      print("printing the testArray: ")
      testArray.foreach(x => println(x))

      val testColumn = dataColumns.apply(0)

      /*
      // **************** DOESN'T WORK YET******************
      val selectedDF = inputData.select(testColumn) //"Solar Irradiation"
      val selectedDFSchema = selectedDF.schema
      var filteredRDD = spark.emptyDataFrame.rdd
      var dadFrame = spark.emptyDataFrame
      var frameWithin = spark.emptyDataFrame
      var theLength = 0

      // generate multiple data series via the past-shift-value and the horizon
      // from 0 to pastShiftValue, generate a new data series which reaches from the value at (pastShiftValue - currentIndex) to (length - horizon - currentIndex) from the original data
      for (currentIndex <- 0 to pastIndex){

        // generate a new series, starting with the value at (pastIndex-current) from the original data
        filteredRDD = selectedDF.rdd.zipWithIndex().collect {case (r,i) if ( i >= (pastIndex-currentIndex) ) => r}

        // then take all values from the gernerated series but leave all values after the index at (length-horizont-current) away
        theLength = filteredRDD.count().toInt
        frameWithin = spark.createDataFrame(filteredRDD,selectedDFSchema).limit(theLength-horizont-currentIndex)
        println("The current frame within: ")
        frameWithin.show()

        // because of the schema for adding new dfs to it?
        if(currentIndex == 0) {
            dadFrame = frameWithin
          }

        val what = frameWithin.apply("PV Power")
        val nume = "PV Power"+currentIndex.toString()

        println("before")
        dadFrame.show()

        if(currentIndex>0){
          val blo = 355
        }

        if(currentIndex==0){
          val fhu = 89
        }

        //dadFrame = dadFrame.withColumn(nume,col(nume))
        dadFrame = (dadFrame
          .join(frameWithin))

        println("after")
        dadFrame.show()

        if(currentIndex>0){
          val blo = 355
        }
      }

      // **************** DOESN'T WORK YET******************
      */



      println("features array:")
      testArray.foreach(println)

      // user defined functions
      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf((i: String) => (Vectors.dense(i.split(",").map(str => str.toDouble)): org.apache.spark.ml.linalg.Vector))

      // create a coloumn in which all features are inside, divided by ","
      val preData = inputData
        .withColumn("test", concat_ws(",", testArray.map(str => col(str)): _*))

      println("preData:")
      preData.show()

      finalData = preData
        .withColumn("features", toVector(preData("test")))
        .withColumn("label", toDouble(preData(dataColumns.apply(labelIndex.toInt))))
        .select("features", "label")

      println("final Data: ")
      finalData.show()

    }
    catch{
      case e: FileNotFoundException => println("File not found")
    }

    return finalData

  }
}
