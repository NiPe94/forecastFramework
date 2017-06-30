package org.kit.energy

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
      val selectedDF = nilCSV.select("Solar Irradiation")
      val selectedDFSchema = selectedDF.schema
      var filteredRDD = spark.emptyDataFrame.rdd
      var dadFrame = spark.emptyDataFrame
      var frameWithin = spark.emptyDataFrame
      var theLength = 0

      for (bla <- 0 to pastIndex){
        println("This is run number " + bla)
        println("--------------------------")
        // starte bei past Index - bla
        filteredRDD = selectedDF.rdd.zipWithIndex().collect {case (r,i) if ( i >= (pastIndex-bla) ) => r}
        // gehe bis count - horizont - bla
        theLength = filteredRDD.count().toInt
        frameWithin = spark.createDataFrame(filteredRDD,selectedDFSchema).limit(theLength-horizont-bla)

        if(bla == 0) {
            dadFrame = frameWithin
          }

        var myString = frameWithin.columns.apply(0)

        println("Now it burns:")
        println("first show the current dadFrame:")
        dadFrame.show()
        println("Then show the frame within which will be added")
        frameWithin.show()
        dadFrame = dadFrame.withColumn(bla.toString,frameWithin("Solar Irradiation"))
        dadFrame.show()
      }
      println("I'm so excited:")
      dadFrame.show()
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

    return finalData

  }
}
