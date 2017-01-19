package org.kit.energy

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.stereotype.Component
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint

/**
  * Created by qa5147 on 16.01.2017.
  */
@Component
class TestClass extends Serializable{

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

  def startHere(): Unit ={
    println("Here is where scala starts!")

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

    // read Data
    val data = sc.textFile("PV2015.csv")

    // generates RDD[ Array[String] ]
    val splitRdd = data.map( line => line.split(";"))
    println("Print first String array:")
    splitRdd.take(1).foreach(array => array.foreach(println))

    // generates RDD[ (String, Int(Minutes), String, String) ] asuming there are 4 fields!!!
    println("Print first String tuple:")
    val mySplitRdd = splitRdd.map( array => (array(0), timeConversion(array(1)), array(2), array(3)))
    mySplitRdd.take(1).foreach(println)

    // generates RDD[ LabeledPoint(double, vector[double]) ]
    //val myLabelDataset = myParsedSplitRdd.map(t => LabeledPoint.apply(t._3.toDouble, Vectors.dense(t._2.toDouble)))
    val myLabelDataset = mySplitRdd.map(t => LabeledPoint.apply(t._3.toDouble, Vectors.dense(t._2.toDouble)))

    // generates DataSet out of the RDD
    // the vector from mllib-library does not work with the LinearRegression() method because it uses vector from ml-library
    val myLabelFrame = spark.createDataset(myLabelDataset)
    println("Show mistery dataframe:")
    myLabelFrame.show()

    /*
    // generates RDD[ Row(Array[Int], Double) ]
    val myNextDataset = myParsedSplitRdd
        .map(myTupel => Row(Array(myTupel._2.toDouble) , myTupel._3.toDouble))

    // create schema
    val schema = StructType(Array(
      StructField("features", ArrayType.apply(DoubleType), true),
      StructField("label", DoubleType, true)
      ))

    // create Dataframe[array(double), double] with RDD and schema
    val trainSet = spark.createDataFrame(myNextDataset,schema)
    println("Show dataframe with array:")
    trainSet.show()

    // change to Dataframe[labeledPoint]
    val newTrainSet = trainSet.select("features","label").map(r => LabeledPoint.apply(r.get(1).toString().toDouble, Vectors.dense(r.get(0).toString().toDouble))).toDF()
    println("Show dataframe with points:")
    newTrainSet.show()*/

    /*// Linear Regression: create models
    val lr = new LogisticRegressionClass()
    lr.setRegParam(0.0)
    val modelA = lr.fit(myLabelFrame)
    lr.setRegParam(100.0)
    val modelB = lr.fit(myLabelFrame)

    println("Print both models:")
    println(modelA)
    println(modelB)*/

    /*
    println("Show prediction:")
    val predictionsA = modelA.transform(trainSet)
    predictionsA.show()
    */

    // stop the application
    spark.stop()



  }


}
