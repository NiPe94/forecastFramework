package org.kit.energy

import java.lang.reflect.Field

import org.apache.spark.ml.{Pipeline, PipelineModel, Transformer}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.springframework.stereotype.Component


/**
  * Created by qa5147 on 29.05.2017.
  */
@Component
class ForecastPipeline {

  def startForecasting(forecast:Forecast, algoPlugin:AlgoPlugin, performModeling:Boolean, performModelApplication:Boolean) : String = {

    var forecastResult = ""
    val savePathModel = forecast.getModeling.getSavePathModel
    val savePathCSV = forecast.getSavePathCSV

    // WINDOWS: set system var for hadoop fileserver emulating via installed winutils.exe
    System.setProperty("hadoop.home.dir", "C:\\Spark\\winutils-master\\hadoop-2.7.1");

    // initialize spark context vars
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    try {

      var predictedData:DataFrame = null

      // prepare dataset for using
      val preperator = new CSVDataPreperator()
      println("Start preparing the data")
      val preparedData = preperator.prepareDataset(forecast.getFileCSV(), spark)
      println("Ended preparing the data")

      // start a new modeling job
      if(performModeling){

        // new model evaluation
        val resultingModel = algoPlugin.train(preparedData)

        // string with model parameters for the web-ui
        forecastResult = this.getResultString(resultingModel)

        // save the evaluated new model
        val myPipeline:Pipeline = new Pipeline()
        myPipeline.setStages(Array(resultingModel))
        myPipeline.write.overwrite().save(savePathModel)

        // perform a model application directly afterwards
        if(performModelApplication){
          predictedData = algoPlugin.applyModel(preparedData,resultingModel)
        }
      }

      // perform a model application via a loaded model (without model evaluation right before this)
      if(!performModeling && performModelApplication){
        val loadedModel = PipelineModel.load(savePathModel)
        forecastResult = this.getResultString(loadedModel)
        predictedData = algoPlugin.applyModel(preparedData,loadedModel)
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
    finally {
      spark.stop()
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
