package org.kit.energy

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, concat_ws, udf}
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 02.02.2017.
  */
@Component
class DataPreperator {

  def prepareDataset(dataPath: String, hasHead: Boolean, delimeter: String, labelIndex: String, featuresIndex: String, spark: SparkSession): sql.DataFrame = {

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    println("print var inputs (head, del, label, features):")
    println(hasHead)
    println(delimeter)
    println(labelIndex)
    println(featuresIndex)

    var finalData = spark.emptyDataFrame

    val pastIndex = 3

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

      nilCSV.

      /*
      // was wenn testArray aus PV|PV|Solar
      println("This is the part which will be thrown away:")
      var partSet = nilCSV.select("Solar Irradiation").limit(3)
      partSet.show()

      println("This is the data without the thrown part:")
      var newDF = spark.emptyDataFrame
      for(a <- 1 to 2){
        if(a == 1){
          newDF = nilCSV.select("Solar Irradiation").except(partSet)
        }
        else {
          newDF = newDF.except(partSet)
        }
        println(a)
        println("----------------------")
        newDF.show()
        println("----------------------")
        partSet = newDF.limit(3)
      }*/


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
