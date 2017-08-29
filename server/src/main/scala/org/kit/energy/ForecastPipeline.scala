package org.kit.energy

import java.lang.reflect.Field

import org.apache.spark.ml.{Pipeline, PipelineModel, Transformer}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.kit.energy.api.AlgoPlugin
import org.springframework.stereotype.Component


/**
  * Class to process a forecast or model training. The DataCombiner combines the already loaded input data to one DataFrame
  * which then will be lagged via the PastShifter to generate a ARX model. After performing a trainign or prediction,
  * the resulting metadata will be returned.
  */
@Component
class ForecastPipeline {

  def startForecasting(forecast:Forecast, algoPlugin:AlgoPlugin, performModeling:Boolean, performModelApplication:Boolean, sparkEnv:SparkEnvironment) : String = {

    var forecastResult = ""
    val savePathModel = forecast.getModeling.getSavePathModel
    val savePathCSV = forecast.getSavePathCSV

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\Spark\\winutils-master\\hadoop-2.7.1");

    try {

      var predictedData:DataFrame = null

      // combine the loaded data to one dataframe with columns "features" with [double,double,double] and "label" with double
      val combiner:DataCombiner = new DataCombiner
      val combinedData = combiner.combineLoadedData(sparkEnv.getLabel(), sparkEnv.getFeatures(), sparkEnv.getInstance())

      // start a new modeling job
      if(performModeling){

        // shift the dataframe values due to the past-shift parameter from the web ui to build a AR(Autoregressive)-model
        val shifter:PastShifter = new PastShifter
        val shiftedData = shifter.shiftData(combinedData, sparkEnv.getInstance(), forecast.getModeling.getPastHorizon)

        // new model evaluation
        val resultingModel = algoPlugin.train(shiftedData)

        // string with model parameters for the web-ui
        forecastResult = this.getResultString(resultingModel)

        // save the evaluated new model
        val myPipeline:Pipeline = new Pipeline()
        myPipeline.setStages(Array(resultingModel))
        myPipeline.write.overwrite().save(savePathModel)

        // perform a model application directly afterwards
        if(performModelApplication){
          predictedData = algoPlugin.applyModel(combinedData,resultingModel)
        }
      }

      // perform a model application via a loaded model (without model evaluation right before this)
      if(!performModeling && performModelApplication){
        val loadedPipeline = Pipeline.load(savePathModel)
        val loadedModel = loadedPipeline.getStages(0).asInstanceOf[Transformer]
        // val loadedModel = PipelineModel.load(savePathModel)
        forecastResult = this.getResultString(loadedModel)
        predictedData = algoPlugin.applyModel(combinedData,loadedModel)
      }

      // save existing predicted dataFrame
      if(predictedData!=null){
        predictedData
          .write
          .format("com.databricks.spark.csv")
          .option("header","false")
          .option("sep",";")
          .mode("overwrite")
          .save(savePathCSV)
      }

      return forecastResult

    } catch {
      case e: Exception => e.printStackTrace(); return "error while calculating the algorithm"
    }
  }

  private def getResultString(transformer:Transformer) : String = {
    // model parameters to return
    val coefficients = transformer.getClass.getDeclaredField("coefficients")
    coefficients.setAccessible(true)
    val coefficientsValue = coefficients.get(transformer).toString()
    val intercept = transformer.getClass.getDeclaredField("intercept")
    intercept.setAccessible(true)
    val interceptValue = intercept.get(transformer).toString()
    val result = "Coefficients: "+coefficientsValue+" Intercept: "+interceptValue
    return result
  }

}
