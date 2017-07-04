package org.kit.energy

import java.io.FileNotFoundException

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.{SparkContext, sql}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, concat_ws, udf}
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 02.02.2017.
  */
@Component
class CSVDataPreperator {

  //(dataPath: String, hasHead: Boolean, delimeter: String, labelIndex: String, featuresIndex: String, spark: SparkSession)
  def prepareDataset(csvFile:CSVFile, spark: SparkSession): sql.DataFrame = {

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    // read the csv properties
    val hasHead = csvFile.isHasHeader
    val delimeter = csvFile.getDelimeter
    val labelIndex = csvFile.getLabelColumnIndex
    val featuresIndex = csvFile.getFeatureColumnsIndexes
    val dataPath = csvFile.getDataPath

    println("print var inputs (head, del, label, features):")
    println(hasHead)
    println(delimeter)
    println(labelIndex)
    println(featuresIndex)

    var finalData = spark.emptyDataFrame
    val pastIndex = 2
    val horizont = 1

    try {
      // read the dataset
      val nilCSV = spark.read
        .format("com.databricks.spark.csv")
        .option("header", hasHead)
        .option("sep", delimeter)
        .option("mode", "DROPMALFORMED")
        .load(dataPath)

      val shift = 2

      println("input schema:")
      nilCSV.printSchema()

      println("input data:")
      nilCSV.show()

      // get all columns of the dataset (Array[String])
      val dataColumns = nilCSV.columns
      println("columns:")
      dataColumns.foreach(println)

      // split feature index string into array => Array[String] ("2,3" => ["2","3"])
      val featuresSplit = featuresIndex.split(",")
      println("splitted feature vector positions:")
      featuresSplit.foreach(println)

      // get column array with only the features in it
      val testArray = new Array[String](featuresSplit.length)
      var counter = 0

      // for each feature, fill the testArray with the names of the feature columns
      for (featureString <- featuresSplit) {
        testArray(counter) = dataColumns.apply(featureString.toInt)
        counter += 1
      }

      /*
      // **************** DOESN'T WORK ******************
      val selectedDF = nilCSV.select("Solar Irradiation")
      val selectedDFSchema = selectedDF.schema
      var filteredRDD = spark.emptyDataFrame.rdd
      var dadFrame = spark.emptyDataFrame
      var frameWithin = spark.emptyDataFrame
      var theLength = 0

      // generate multiple data series via the past-shift-value and the horizon
      // from 0 to pastShiftValue, generate a new data series which reaches from the value at (pastShiftValue - currentIndex) to (length - horizon - currentIndex) from the original data
      for (currentIndex <- 0 to pastIndex){
        println("This is run number " + currentIndex)
        println("--------------------------")
        // generate a new series, starting with the value at (pastIndex-current) from the original data
        filteredRDD = selectedDF.rdd.zipWithIndex().collect {case (r,i) if ( i >= (pastIndex-currentIndex) ) => r}
        println("Start at pastindex-current:")
        filteredRDD.foreach(r=>println(r.toString()))
        println()
        // then take all values from the gernerated series but leave all values after the index at (length-horizont-current) away
        theLength = filteredRDD.count().toInt
        frameWithin = spark.createDataFrame(filteredRDD,selectedDFSchema).limit(theLength-horizont-currentIndex)

        // because of the schema for adding new dfs to it?
        if(currentIndex == 0) {
            dadFrame = frameWithin
          }

        // what?
        var myString = frameWithin.columns.apply(0)

        println("first show the current dadFrame:")
        dadFrame.show()
        println("Then show the frame cutted which will be added to it")
        var frameWithinDadSecond = frameWithin.withColumnRenamed("Solar Irradiation",currentIndex.toString)
        frameWithinDadSecond.show()
        if(currentIndex==1){
          val blubi = 898
        }
        dadFrame = dadFrame.withColumn("blubi"+currentIndex.toString,frameWithinDadSecond(currentIndex.toString))
        dadFrame.show()
        val bla = 356
      }
      println("I'm so excited:")
      dadFrame.show()
      // **************** DOESN'T WORK ******************
      */


      println("features array:")
      testArray.foreach(println)

      // user defined functions
      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf((i: String) => (Vectors.dense(i.split(",").map(str => str.toDouble)): org.apache.spark.ml.linalg.Vector))

      // create a coloumn in which all features are inside, divided by ","
      val preData = nilCSV
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
