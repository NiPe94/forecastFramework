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

      /*
      // read data and show part of Data
      val data = spark.read
        .format("com.databricks.spark.csv")
        .option("mode","DROPMALFORMED")
        .option("header","false")
        .load("PV2015.csv")
      println("TryData2:")
      data.show()

      // Transform Dataframe to Dataset[Labeled Point]#rdd
      val dataParsed = data.select("_c0")
        .map(r => LabeledPoint( timeConversion(r(0).toString.split(";").apply(1)).toDouble , Vectors.dense(r(0).toString.split(";").apply(2).toDouble) ))
        .rdd
        .cache()
      println("Print data after parsing:")
      dataParsed.take(5).foreach(println)

      // set training parameter and train data
      println("Begin training:")
      val numIts = 100
      val steps = 0.0000001
      val model = LinearRegressionWithSGD.train(dataParsed, numIts, steps)

      println("print model weights:")
      println(model.weights)

      // Evaluate model on training examples and compute training error
      val valuesAndPreds = dataParsed.map { point =>
        val prediction = model.predict(point.features)
        (point.label, prediction)
      }
      val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2) }.mean()
      println("training Mean Squared Error = " + MSE)

      /* save and load the evaluated model
      model.save(sc,"DADModel")
      val sameOldThing = LinearRegressionModel.load("DADModel")
      */
      */

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
      println("nilData without c0")
      nilData.printSchema()
      nilData.show()

      val test = Vectors.dense(1.0)
      println(test)

      val toDouble = udf[Double, String](_.toDouble)
      val toVector = udf( (i : String) => (Vectors.dense(i.toDouble) : org.apache.spark.ml.linalg.Vector) )
      //val toVector = udf[Vector, String](Vectors.dense(_.toDouble))

      val nilDataFormatted = nilData
          .withColumn("label", toDouble(nilData("time")))
          .withColumn("features", toVector(nilData("Nile")) )

      println("nilDataFormatted")
      nilDataFormatted.printSchema()
      nilDataFormatted.show()

      val nilDataChanged = nilData.withColumnRenamed("Nile","features")

      nilDataChanged.show()

      import spark.implicits._

      //nilDataChanged.foreach(r => Row(r(0).toString.toDouble, Vectors.dense(1.0, r(1).toString.toDouble)))

      //val nilDataChangedToRow = nilDataChanged.map( r => Row(r(0), Vectors.dense(1.0, r(1).toString.toDouble)))

      //println("Maped to rows:")
      //nilDataChangedToRow.show()


      /*println("Show Nil schema and data:")
      nilData.printSchema()
      nilData.show()*/

      /*val nilData2 = nilData.select("time", "Nile")
        .map( r =>LabeledPoint(r(0).toString.toDouble, Vectors.dense(1.0, r(1).toString.toDouble)) )
        .rdd.cache()*/

      /*println("Nil data 2 after labeling")
      nilData2.take(5).foreach(println)*/

      /*val numIts = 100
      val steps = 0.0000001
      val model = LinearRegressionWithSGD.train(nilData2, numIts, steps)*/

      val lrModel = new LinearRegression().fit(nilDataFormatted)

      println("parameters:")
      println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

      /*val bias = 1020 - model.weights.apply(0)*38

      println("print model weights:")
      println(model.weights)
      println("and")
      println(bias)

      // Evaluate model on training examples and compute training error
      val valuesAndPreds = nilData2.map { point =>
        val prediction = model.predict(point.features)
        (point.label, prediction)
      }
      val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2) }.mean()
      println("training Mean Squared Error = " + MSE)

      val weightReturn = model.weights.toArray

      stringToReturn = weightReturn.mkString(" ")*/

      return stringToReturn

      // working Nil example
      // ************************


    } finally {
      // do a clean stop on spark
      spark.stop()

    }

  }

}
