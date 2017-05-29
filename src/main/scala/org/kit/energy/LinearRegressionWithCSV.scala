package org.kit.energy

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, round, udf}
import org.springframework.stereotype.Component
import org.apache.spark.sql.functions.{concat, concat_ws, lit}

/**
  * Created by qa5147 on 16.01.2017.
  */
@Component
class LinearRegressionWithCSV extends Serializable{

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

  // method to start and execute the regression
  def start(finalData:sql.DataFrame): LinearRegressionModel = {

      // set regression parameter and start the regression
      val lrModelStart = new LinearRegression().setRegParam(0.0).setElasticNetParam(0.0).setFitIntercept(true)
      var lrModel = lrModelStart.fit(finalData)

      // print parameter
      println("parameters:")
      println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

      // Summarize the model over the training set and print out some metrics
      val trainingSummary = lrModel.summary
      println(s"numIterations: ${trainingSummary.totalIterations}")
      println(s"MSE: ${trainingSummary.meanSquaredError}")
      println(s"r2: ${trainingSummary.r2}")

      return lrModel

  }

}
