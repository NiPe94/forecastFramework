package org.kit.energy

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.stereotype.Component
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionModel, LinearRegressionWithSGD}
import org.apache.spark.sql.types.{ArrayType, DoubleType, StructField, StructType}

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

  def startHere(): Unit = {



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

    try {

      // ************************
      /* Working Nile csv example
      //https://spark.apache.org/docs/2.1.0/mllib-linear-methods.html#linear-least-squares-lasso-and-ridge-regression
      val nilCSV = spark.read
        .format("com.databricks.spark.csv")
        .option("header","true")
        .option("mode","DROPMALFORMED")
        .load("Nile.csv")

      val nilData = nilCSV.drop("_c0")

      println("Show Nil schema and data:")
      nilData.printSchema()
      nilData.show()

      val nilData2 = nilData.select("time", "Nile")
        .map( r =>LabeledPoint(r(0).toString.toDouble, Vectors.dense(r(1).toString.toDouble)) )
        .rdd.cache()

      println("Nil data 2 after labeling")
      nilData2.take(5).foreach(println)

      val numIts = 100
      val steps = 0.0000001
      val model = LinearRegressionWithSGD.train(nilData2, numIts, steps)

      println("print model weights:")
      println(model.weights)

      // Evaluate model on training examples and compute training error
      val valuesAndPreds = nilData2.map { point =>
        val prediction = model.predict(point.features)
        (point.label, prediction)
      }
      val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2) }.mean()
      println("training Mean Squared Error = " + MSE)

      model.save(sc,"DADModel")
      val sameOldThing = LinearRegressionModel.load(sc, "DADModel")
      println("should be the same:")
      println(sameOldThing.weights)
      */ // working Nil example
      // ************************

      // read Data
      val data = sc.textFile("PV2015.csv")

      val tryData = spark.read
        .format("com.databricks.spark.csv")
        .option("mode","DROPMALFORMED")
        .option("header","false")
        .load("PV2015.csv")
      println("TryData2:")
      tryData.show()

      val tryData2 = tryData.select("_c0")
        .map(r => LabeledPoint( timeConversion(r(0).toString.split(";").apply(1)).toDouble , Vectors.dense(r(0).toString.split(";").apply(2).toDouble) ))
        .rdd
        .cache()

      println("Print trainData2 after parsing:")
      tryData2.take(5).foreach(println)

      println("Begin training:")
      val numIts = 100
      val steps = 0.0000001
      val model = LinearRegressionWithSGD.train(tryData2, numIts, steps)

      println("print model weights:")
      println(model.weights)

      // Evaluate model on training examples and compute training error
      val valuesAndPreds = tryData2.map { point =>
        val prediction = model.predict(point.features)
        (point.label, prediction)
      }
      val MSE = valuesAndPreds.map{ case(v, p) => math.pow((v - p), 2) }.mean()
      println("training Mean Squared Error = " + MSE)

      /*
      // ****** FROM ******
      // RDD[ Array[String] ]
      val splitRdd = data.map(line => line.split(";"))
      println("Print first String array:")
      splitRdd.take(1).foreach(array => array.foreach(println))

      // RDD[ (String, Int(Minutes), String, String) ] asuming there are 4 fields!!!
      println("Print first String tuple:")
      val mySplitRdd = splitRdd.map(array => (array(0), timeConversion(array(1)), array(2).replace(",", "."), array(3)))
      mySplitRdd.take(40).foreach(println)


      // RDD[ Row(Array[Int], Double) ]
      val myNextDataset = mySplitRdd
          .map(myTupel => Row(Array(myTupel._2.toDouble) , myTupel._3.toDouble))

      // schema
      val schema = StructType(Array(
        StructField("features", ArrayType.apply(DoubleType), true),
        StructField("label", DoubleType, true)
        ))

      // Dataframe[array(double), double] with RDD and schema
      val trainSet = spark.createDataFrame(myNextDataset,schema)
      println("Show dataframe with array:")
      trainSet.show()

      // Dataframe[labeledPoint]
      val newTrainSet = trainSet.select("features","label").map(r => LabeledPoint(r(1).toString.toDouble, Vectors.dense(r(0).toString.toDouble))).rdd.cache()
      //println("Show dataframe with points:")
      //newTrainSet.take(5).foreach(println)

      println("Training begins:")
      val lr = new LinearRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8)
      val lrModel = lr.fit(newTrainSet.toDS())
      //val model = LinearRegressionWithSGD.train(newTrainSet, 100, 0.00000001)

      println("training ended, model:")
      println(lrModel)

      // ******* TO *********
      */

      /*
      // Linear Regression: create models
      val lr = new LinearRegression()
      lr.setRegParam(0.0)
      val modelA = lr.fit(myLabelFrame)

      println("Print both models:")
      println(modelA)
      */

      /*
      println("Show prediction:")
      val predictionsA = modelA.transform(trainSet)
      predictionsA.show()
      */

      // stop the application
      //spark.stop()

      /* Train DataSet[Labeled Point]
      // RDD[ LabeledPoint(double, vector[double]) ]
      //val myLabelDataset = myParsedSplitRdd.map(t => LabeledPoint.apply(t._3.toDouble, Vectors.dense(t._2.toDouble)))
      val myLabelDataset = mySplitRdd.map(t => LabeledPoint.apply(t._3.toDouble, Vectors.dense(t._2.toDouble)))

      // DataSet out of the RDD
      // the vector from mllib-library does not work with the LinearRegression() method because it uses vector from ml-library
      val myLabelFrame = spark.createDataset(myLabelDataset)
      println("Show mistery dataframe:")
      myLabelFrame.show()

      println("Training begins:")
      val model = LinearRegressionWithSGD.train(myLabelFrame.rdd, 100, 0.00000001)
      println("training ended, model:")
      println(model)
      */

    } finally {
      spark.stop()
    }

  }


}
