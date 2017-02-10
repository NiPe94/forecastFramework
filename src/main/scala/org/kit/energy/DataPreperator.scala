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

  def prepareDataset(dataPath: String, hasHead: Boolean, delimeter: String, labelIndex: String, featuresIndex: String): sql.DataFrame = {

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    // initialize spark context vars
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    println("print var inputs (head, del, label, features):")
    println(hasHead)
    println(delimeter)
    println(labelIndex)
    println(featuresIndex)

    var finalData = spark.emptyDataFrame

    try {

      // read the dataset
      val nilCSV = spark.read
        .format("com.databricks.spark.csv")
        .option("header", hasHead)
        .option("sep", delimeter)
        .option("mode", "DROPMALFORMED")
        .load(dataPath)

      println("input schema:")
      nilCSV.printSchema()

      println("input data:")
      nilCSV.show()

      // get all columns of the dataset (Array[String])
      val dataColumns = nilCSV.columns
      println("columns:")
      dataColumns.foreach(println)

      // split feature index string into ints => Array[String]
      val featuresSplit = featuresIndex.split(",")
      println("splitted feature vector:")
      featuresSplit.foreach(println)

      // get column array with only the features in it
      val testArray = new Array[String](featuresSplit.length)
      var counter = 0

      for (featureString <- featuresSplit) {
        testArray(counter) = dataColumns.apply(featureString.toInt - 1)
        counter += 1
      }

      println("test array:")
      testArray.foreach(println)

      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf((i: String) => (Vectors.dense(i.split(",").map(str => str.toDouble)): org.apache.spark.ml.linalg.Vector))

      val preData = nilCSV
        .withColumn("test", concat_ws(",", testArray.map(str => col(str)): _*))

      finalData = preData
        .withColumn("features", toVector(preData("test")))
        .withColumn("label", toDouble(preData(dataColumns.apply(labelIndex.toInt - 1))))
        .select("features", "label")

    }

    return finalData

  }
}
