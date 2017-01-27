package org.kit.energy

import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.SparkSession
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 20.01.2017.
  */
@Component
class LinearRegressionCSVFormat extends Serializable{

  // Good to know:
  // ML: new, pipelines, dataframes, easier to construct ml pipeline
  // MLLib: Old, RDDs, More features

  // convert the Times to Integer values via this function
  def timeConversion (str: String) : Int = {
    val timeStringValues = str.split(":")
    val timeIntValues = timeStringValues.map( str => str.toInt )
    val timeFinalValue = (timeIntValues.apply(0) * 60) + timeIntValues.apply(1)
    return timeFinalValue
  }

  // method to start and execute the regression
  def startHere(dataPath:String, savePath:String): String = {

    // only for printing something to the console
    println("Here is where scala starts!")

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    // initialize spark context vars
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    var stringToReturn = "";

    try {

      // read the dataset
      val nilCSV = spark.read
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .option("mode", "DROPMALFORMED")
        .load(dataPath)

      // drop the unimportant column
      val nilData = nilCSV.drop("_c0")

      // user defined functions to convert the column data for the regression
      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf( (i : String) => (Vectors.dense(i.toDouble) : org.apache.spark.ml.linalg.Vector) )

      // copy the columns and rename their header names, also convert their data
      val nilDataFormatted1 = nilData
          .withColumn("features", toVector(nilData("time")))
          .withColumn("label", toDouble(nilData("Nile")) )

      // drop the old columns
      val nilDataFormatted = nilDataFormatted1.drop("time","Nile")
      nilDataFormatted.show()

      // set regression parameter and start the regression
      val lrModelStart = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
      val lrModel = lrModelStart.fit(nilDataFormatted)

      // print parameter
      println("parameters:")
      println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

      // Summarize the model over the training set and print out some metrics
      val trainingSummary = lrModel.summary
      println(s"numIterations: ${trainingSummary.totalIterations}")
      println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
      trainingSummary.residuals.show()
      println(s"MSE: ${trainingSummary.meanSquaredError}")
      println(s"r2: ${trainingSummary.r2}")

      // save and load the model
      lrModel.write.overwrite().save(savePath)
      val bla = LinearRegressionModel.load(savePath)
      println("Loaded params:")
      println(s"Coefficients: ${bla.coefficients} Intercept: ${bla.intercept}")

      // return the model parameters
      stringToReturn = lrModel.coefficients.toString + " " + lrModel.intercept.toString
      return stringToReturn


    } finally {
      // do a clean stop on spark
      spark.stop()

    }

  }

}
