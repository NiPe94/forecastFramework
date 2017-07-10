package org.kit.energy
import org.apache.spark.ml.Transformer
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{Dataset, Row, SparkSession}

/**
  * Created by qa5147 on 06.07.2017.
  */
class LinearRegressionSparkExample extends AlgoPlugin{
  def train(input: Dataset[Row]): Transformer = {
    val lr = new LinearRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)

    // Fit the model
    val lrModel = lr.fit(input)

    // Print the coefficients and intercept for linear regression
    println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

    // Summarize the model over the training set and print out some metrics
    val trainingSummary = lrModel.summary
    println(s"numIterations: ${trainingSummary.totalIterations}")
    println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
    trainingSummary.residuals.show()
    println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
    println(s"r2: ${trainingSummary.r2}")
    return null
  }

  def applyModel(input: Dataset[Row], model: Transformer): Dataset[Row] = {
    return null
  }
}
