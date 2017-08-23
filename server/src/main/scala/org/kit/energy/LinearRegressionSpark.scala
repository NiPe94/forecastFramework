package org.kit.energy

import org.apache.spark.ml.{PipelineModel, PipelineStage, Transformer}
import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel
import org.kit.energy.api.{AlgoParam, AlgoPlugin}

/**
  * Created by qa5147 on 19.06.2017.
  */
class LinearRegressionSpark extends AlgoPlugin{


  @AlgoParam(name="Regularization", value="0")
  var regParam:String = _

  @AlgoParam(name="Elastic Net", value="0")
  var elasticNet:String = _

  @AlgoParam(name="Repartition", value="16")
  var repart:String  =_

  def train(inputData: DataFrame): Transformer = {
    // set regression parameter and start the regression
    val lrModelStart = new LinearRegression().setRegParam(regParam.toDouble).setElasticNetParam(elasticNet.toDouble)

    var lrModel = lrModelStart.fit(inputData)

    return lrModel
  }

  def applyModel(input: DataFrame, model: Transformer): DataFrame = {

    val transformedData = model.transform(input).select("prediction")

    return transformedData
  }

}