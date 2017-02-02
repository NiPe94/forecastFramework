package org.kit.energy

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql
import org.apache.spark.sql.{Column, SparkSession}
import org.apache.spark.sql.functions.{round, udf}
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
  /*def timeConversion (str: String) : Int = {

    val timeStringValues = str.split(":")
    val timeIntValues = timeStringValues.map( str => str.toInt )
    val timeFinalValue = (timeIntValues.apply(0) * 60) + timeIntValues.apply(1)
    return timeFinalValue
  }*/

  // perform Model application and save the resulting csv dataset on the given path
  def doApplication (lr: LinearRegressionModel, df: sql.DataFrame, savePathCSV:String) : Unit = {

    val transformedData = lr.transform(df)

    println("transformed Data schema:")
    transformedData.printSchema()

    println("transformed data:")
    transformedData.show()

    val savePathChanged = savePathCSV + "myData.csv"

    println("begin saving:")
    val selectedDataBefore = transformedData.withColumn("prediction2", round(new Column("prediction"),3))
    val selectedData = selectedDataBefore.select("label", "prediction2")
    selectedData
      .write
      .format("com.databricks.spark.csv")
      .option("header","false")
      .option("sep",";")
      .save(savePathChanged)

  }

  // method to start and execute the regression
  def start(dataPath:String, savePathModel:String, savePathCSV:String, performModeling:Boolean, performModelApplication:Boolean, hasHead:Boolean, delimeter:String, labelIndex:String, featuresIndex:String): String = {

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    println("print var inputs (head, del, label, features):")
    println(hasHead)
    println(delimeter)
    println(labelIndex)
    println(featuresIndex)

    // initialize spark context vars
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    var stringToReturn = "tescht weisch";

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

      // split feature index string into ints
      //val featuresSplit = featuresIndex.split(",")

      // user defined functions to convert the column data for the regression
      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf( (i : String) => (Vectors.dense(i.toDouble) : org.apache.spark.ml.linalg.Vector) )
      //val toArrayVector = udf( (j: Array[String]) => j.foreach( str => toVector(nilCSV(str)))  )





      // copy the columns and rename their header names, also convert their data
      val nilDataNotFormatted = nilCSV //nilData
        .withColumn("features", toVector(nilCSV(dataColumns.apply(featuresIndex.toInt - 1))))//nilData
        .withColumn("label", toDouble(nilCSV(dataColumns.apply(labelIndex.toInt - 1))) )//nilData

      val nilDataFormatted = nilDataNotFormatted.select("features","label")

      println("Not formatted:")
      nilDataNotFormatted.show()

      println("formatted:")
      nilDataFormatted.show()

      // if the user wants to start a modeling job
      if(performModeling) {
        // set regression parameter and start the regression
        val lrModelStart = new LinearRegression().setRegParam(0.0).setElasticNetParam(0.0).setFitIntercept(true)
        val lrModel = lrModelStart.fit(nilDataFormatted)

        // print parameter
        println("parameters:")
        println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

        // Summarize the model over the training set and print out some metrics
        val trainingSummary = lrModel.summary
        println(s"numIterations: ${trainingSummary.totalIterations}")
        println(s"MSE: ${trainingSummary.meanSquaredError}")
        println(s"r2: ${trainingSummary.r2}")

        // save the model
        lrModel.write.overwrite().save(savePathModel)

        if(performModelApplication) {
          // predict new data
          doApplication(lrModel,nilDataFormatted,savePathCSV)
        }

        // the model coefficients will be returned to server
        stringToReturn = lrModel.coefficients.toString + " " + lrModel.intercept.toString

      }

      // if the user wants to start a modelapplication job
      if(performModeling == false && performModelApplication) {
        // if no modeling was performed, load the user given model
        val lrModel = LinearRegressionModel.load(savePathModel)

        // predict new data
        doApplication(lrModel,nilDataFormatted,savePathCSV)

        // the model coefficients will be returned to server
        stringToReturn = lrModel.coefficients.toString + " " + lrModel.intercept.toString
      }


      return stringToReturn


    } catch {
      case e: Exception => return "error while calculating the algorithm"
    }
    finally {
      // do a clean stop on spark
      spark.stop()

    }

  }

}