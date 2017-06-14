package org.kit.energy
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.DataFrame

/**
  * Created by qa5147 on 13.06.2017.
  */
class AlgoExample extends IAIAlgorithm{

  var blubi:IAIParameter = null
  var pRegParam:IAIParameter = null
  var pElasticNet:IAIParameter = null
  //final val pBlub = new IAIParameter("Oh yeah","1")
  //setParamList(List(pRegParam,pElasticNet))

  override def execute(paramList1: List[IAIParameter], inputData: DataFrame): String = {

    // set regression parameter and start the regression
    val lrModelStart = new LinearRegression().setRegParam(0.0).setElasticNetParam(0.0).setFitIntercept(true)
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
