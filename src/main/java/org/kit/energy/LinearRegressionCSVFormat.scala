package org.kit.energy

import java.io.FileNotFoundException

import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.Row
//import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.functions.udf
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 20.01.2017.
  */
@Component
class LinearRegressionCSVFormat extends Serializable{

  // Good to know:
  // ML: new, pipelines, dataframes, easier to construct ml pipeline
  // MLLib: Old, RDDs, More features

  // convert the Times to values via fc
  def timeConversion (str: String) : Int = {
    val timeStringValues = str.split(":")
    val timeIntValues = timeStringValues.map( str => str.toInt )
    val timeFinalValue = (timeIntValues.apply(0) * 60) + timeIntValues.apply(1)
    return timeFinalValue
  }

  // method to start and execute regression
  def startHere(dataPath:String, savePath:String): String = {

    // only for printing something to the console
    println("Here is where scala starts!")

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    // initialize spark context vars
    val conf = new SparkConf().setAppName("Simple Application")
      .setMaster("local")

    val sc = new SparkContext(conf)

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    var stringToReturn = "";

    try {

      // ************************
      // Working Nile csv example, csv format here: "","time","Nile"\n"1",10,400\n"2",32,4345\n....
      //https://spark.apache.org/docs/2.1.0/mllib-linear-methods.html#linear-least-squares-lasso-and-ridge-regression

      var nilCSV = spark.emptyDataFrame

      try {
        nilCSV = spark.read
          .format("com.databricks.spark.csv")
          .option("header", "true")
          .option("mode", "DROPMALFORMED")
          .load(dataPath)
      } catch {
        case ex: FileNotFoundException => {
          stringToReturn = "File not found"
          return stringToReturn
        }
      }

      val nilData = nilCSV.drop("_c0")

      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf( (i : String) => (Vectors.dense(i.toDouble) : org.apache.spark.ml.linalg.Vector) )
      //val toVector = udf[Vector, String](Vectors.dense(_.toDouble))

      val nilDataFormatted1 = nilData
          .withColumn("features", toVector(nilData("time")))
          .withColumn("label", toDouble(nilData("Nile")) )

      //val nilDataChanged = nilData.withColumnRenamed("Nile","features")

      val nilDataFormatted = nilDataFormatted1.drop("time","Nile")
      nilDataFormatted.show()

      val lrModelStart = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
      val lrModel = lrModelStart.fit(nilDataFormatted)


      println("parameters:")
      println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

      // Summarize the model over the training set and print out some metrics
      val trainingSummary = lrModel.summary
      println(s"numIterations: ${trainingSummary.totalIterations}")
      println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
      trainingSummary.residuals.show()
      println(s"MSE: ${trainingSummary.meanSquaredError}")
      println(s"r2: ${trainingSummary.r2}")

      stringToReturn = lrModel.coefficients.toString + " " + lrModel.intercept.toString

      return stringToReturn

      // working Nil example
      // ************************


    } finally {
      // do a clean stop on spark
      spark.stop()

    }

  }

}
