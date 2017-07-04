package org.kit.energy

import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql.SparkSession
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

      // prepare dataset for using
      val preperator = new CSVDataPreperator()
      println("Start preparing the data")
      val preparedData = preperator.prepareDataset(forecast.getFileCSV(), spark)
      println("Ended preparing the data")

      // start a new modeling job
      if(performModeling){

        // new model evaluation
        val resultingModel = algoPlugin.train(preparedData)

        // evaluate the new model
        val algorithm = new LinearRegressionWithCSV()
        val resultModel = algorithm.start(preparedData)

        // model parameters to return
        forecastResult = resultModel.coefficients.toString + " " + resultModel.intercept.toString

        // save the evaluated new model
        resultModel.write.overwrite().save(savePathModel)

        // perform a model application directly afterwards
        if(performModelApplication){
          val modelApplication = new ModelApplication()
          modelApplication.applicateModel(resultModel,preparedData,savePathCSV)
        }
      }

      // perform a model application via a loaded model (without model evaluation right before this)
      if(!performModeling && performModelApplication){
        val loadedModel = LinearRegressionModel.load(savePathModel)
        forecastResult = loadedModel.coefficients.toString + " " + loadedModel.intercept.toString
        val modelApplication = new ModelApplication()
        modelApplication.applicateModel(loadedModel,preparedData,savePathCSV)
        println("done :)")
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
