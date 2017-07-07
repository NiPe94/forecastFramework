package org.kit.energy

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
  * Created by qa5147 on 19.06.2017.
  */
class AlgoWithAnno2 extends AlgoPlugin{

  @AlgoParam(name="Regularization2", value="0.0")
  private var regParam2:String = _

  @AlgoParam(name="Elastic Net2", value="1.3")
  private var elasticNet2:String = _

  @AlgoParam(name="With Intercept2", value="1")
  private var fitIntercept2:String = _


  //@AlgoParam("no matchi1") param1:String, @AlgoParam("no matchi2") param2:String
  //inputData: DataFrame, regParam: String, elasticNet: String, fitIntercept: String
  def train(inputData: DataFrame): Transformer = {

    // set regression parameter and start the regression
    val lrModelStart = new LinearRegression().setRegParam(regParam2.toDouble).setElasticNetParam(elasticNet2.toDouble).setFitIntercept(fitIntercept2.toBoolean)
    var lrModel = lrModelStart.fit(inputData)

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

  def applyModel(input: DataFrame, model: Transformer): DataFrame = {
    val transformedData = model.transform(input)
    return transformedData
  }
}
