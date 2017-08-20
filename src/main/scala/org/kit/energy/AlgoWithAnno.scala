package org.kit.energy

import org.apache.spark.ml.{PipelineModel, PipelineStage, Transformer}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
  * Created by qa5147 on 19.06.2017.
  */
class AlgoWithAnno extends AlgoPlugin{


  @AlgoParam(name="Regularization", value="0")
  var regParam:String = _

  @AlgoParam(name="Elastic Net", value="0")
  var elasticNet:String = _

  @AlgoParam(name="Repartition", value="16")
  var repart:String  =_

  def train(inputData: DataFrame): Transformer = {
    // set regression parameter and start the regression
    val cachedInput = inputData.persist(StorageLevel.MEMORY_ONLY).repartition(repart.toInt)
    val lrModelStart = new LinearRegression().setRegParam(regParam.toDouble).setElasticNetParam(elasticNet.toDouble)

    val t11 = System.nanoTime()
    var lrModel = lrModelStart.fit(cachedInput)
    val duration1 = ((System.nanoTime() - t11)/1e9d)

    val t12 = System.nanoTime()
    val evaluation = lrModel.evaluate(cachedInput)
    val duration2 = ((System.nanoTime() - t12)/1e9d)

    println()
    println("***********************")
    println("training: "+ duration1 +" evaluation: "+ duration2)
    println("***********************")
    println()

    return lrModel
  }

  def applyModel(input: DataFrame, model: Transformer): DataFrame = {

    val transformedData = model.transform(input).select("prediction")

    return transformedData
  }

}
