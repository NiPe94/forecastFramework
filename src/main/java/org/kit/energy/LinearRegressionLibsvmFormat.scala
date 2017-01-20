package org.kit.energy

import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 19.01.2017.
  */
@Component
class LinearRegressionLibsvmFormat extends Serializable{

  def timeConversion (str: String) : Int = {
    val timeStringValues = str.split(":")
    val timeIntValues = timeStringValues.map( str => str.toInt )
    val timeFinalValue = (timeIntValues.apply(0) * 60) + timeIntValues.apply(1)
    return timeFinalValue
  }

  def startHere(): Unit = {

    // Initialze context vars and settings
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    val conf = new SparkConf().setAppName("Simple Application")
      .setMaster("local")
    val sc = new SparkContext(conf)

    // Initialize Spark context
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    import spark.implicits._

    // Load training data
    val training = spark.read.format("libsvm")
      .load("scale.txt")

    val lr = new LinearRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)

    // Fit the model
    val lrModel = lr.fit(training)

    // Print the coefficients and intercept for linear regression
    println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

    // Summarize the model over the training set and print out some metrics
    val trainingSummary = lrModel.summary
    println(s"numIterations: ${trainingSummary.totalIterations}")
    println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
    trainingSummary.residuals.show()
    println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
    println(s"r2: ${trainingSummary.r2}")

    sc.stop()


  }
}
