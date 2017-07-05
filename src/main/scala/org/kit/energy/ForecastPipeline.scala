package org.kit.energy

import org.apache.spark.ml.{Pipeline, PipelineModel, Transformer}
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.springframework.stereotype.Component


/**
  * Created by qa5147 on 29.05.2017.
  */
@Component
class ForecastPipeline {


  //(dataPath:String, savePathModel:String, savePathCSV:String, performModeling:Boolean, performModelApplication:Boolean, hasHead:Boolean, delimeter:String, labelIndex:String, featuresIndex:String)
  def startForecasting(forecast:Forecast, algoPlugin:AlgoPlugin, performModeling:Boolean, performModelApplication:Boolean) : String = {

    println()
    println("start of pipeline!")
    println()

    var forecastResult = "";
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

        println("params.printForEach: ")
        resultingModel.params.foreach(x => println(x.toString()))
        println()
        println("explain params: ")
        println(resultingModel.explainParams())
        println()
        println("to String: ")
        println(resultingModel.toString())

        // model parameters to return
        //forecastResult = resultingModel.coefficients.toString + " " + resultingModel.intercept.toString
        forecastResult = "noch nicht"

        // save the evaluated new model
        var myPipeline:Pipeline = new Pipeline()
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
        forecastResult = "noch ned app"
        predictedData = algoPlugin.applyModel(preparedData,loadedModel)
        println("done :)")
      }

      // save existing predicted dataframe
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

}
