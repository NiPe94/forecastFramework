package org.kit.energy

import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.DataFrame

/**
  * Created by qa5147 on 19.06.2017.
  */
class AlgoWithAnno extends AlgoPlugin{

  /*
  @AlgoParam(name="Regularization", value="0.0")
  private var regParam:String = _

  @AlgoParam(name="Elastic Net", value="1.3")
  private var elasticNet:String = _

  @AlgoParam(name="With Intercept", value="1")
  private var fitIntercept:String = _
  */

  private var regParam:AlgoParameter = new AlgoParameter("Regularization","0.0")
  private var elasticNet:AlgoParameter = new AlgoParameter("Elastic Net","1.3")
  private var fitIntercept:AlgoParameter = new AlgoParameter("With Intercept","1")


  //@AlgoParam("no matchi1") param1:String, @AlgoParam("no matchi2") param2:String
  //inputData: DataFrame, regParam: String, elasticNet: String, fitIntercept: String
  @AlgoDef
  def compute(inputData: DataFrame, regParam: String, elasticNet: String, fitIntercept: String): String = {

    // set regression parameter and start the regression
    val lrModelStart = new LinearRegression().setRegParam(regParam.toDouble).setElasticNetParam(elasticNet.toDouble).setFitIntercept(fitIntercept.toBoolean)
    var lrModel = lrModelStart.fit(inputData)

    // print parameter
    println("parameters:")
    println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

    // Summarize the model over the training set and print out some metrics
    val trainingSummary = lrModel.summary
    println(s"numIterations: ${trainingSummary.totalIterations}")
    println(s"MSE: ${trainingSummary.meanSquaredError}")
    println(s"r2: ${trainingSummary.r2}")

    return ""
  }

}
