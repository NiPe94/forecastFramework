package org.kit.energy

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.springframework.stereotype.Component
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf

/**
  * Created by qa5147 on 16.01.2017.
  */
@Component
class TestClass extends Serializable{

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
  def start(dataPath:String, savePath:String, performModeling:Boolean, performModelApplication:Boolean): String = {

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

      var lrModel = new LinearRegressionModel

      // if the user wants to start a modeling job
      if(performModeling) {
        // set regression parameter and start the regression
        val lrModelStart = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
        lrModel = lrModelStart.fit(nilDataFormatted)

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

        // save the model
        lrModel.write.overwrite().save(savePath)

      }

      // if the user wants to start a modelapplication job
      if(performModelApplication) {
        // if no modeling was performed, load the user given model
        if(performModeling == false) {
            lrModel = LinearRegressionModel.load(savePath)
          }

        // predict new data
        val transformedData = lrModel.transform(nilDataFormatted)

        val savePathChanged = savePath + "myData.csv"

        transformedData.write
          .format("com.databricks.spark.csv")
          .option("header","true")
          .save(savePathChanged)
      }

      // the model coefficients will be returned to server
      stringToReturn = lrModel.coefficients.toString + " " + lrModel.intercept.toString

      return stringToReturn


    } finally {
      // do a clean stop on spark
      spark.stop()

    }

  }

}
