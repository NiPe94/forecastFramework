package org.kit.energy

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Class to hold an spark instance for a specific url and to hold uploaded input data from the web ui.
  */
sealed class SparkEnvironment protected (val masterAdress:String){

  private var spark:SparkSession = null

  private var features:List[DataFrame] = List.empty

  private var label:DataFrame = null

  def getInstance() : SparkSession = {
    if(this.spark != null){
      return this.spark
    }
    println("used adress for spark: "+masterAdress)
    this.spark = SparkSession
      .builder()
      .master(masterAdress)
      .appName("Forecast Service")
      .getOrCreate()

    return spark
  }

  def stopSpark() : Unit = {
    this.spark.stop()
    this.spark = null
  }

  def addData(df:DataFrame, dataType:String) : Unit = {
    if(dataType.equals("label")){
      this.label = df
    }
    if(dataType.equals("feature")){
      if(this.features == null){
        this.features = List(df);
      }
      else {
        this.features = df :: this.features
      }
    }
  }

  def deleteData() : Unit = {
    this.features = null
    this.label = null
    println("all data is now set to null")
  }

  def getFeatures() : List[DataFrame] = {
    return this.features
  }

  def getLabel() : DataFrame = {
    return this.label
  }

}
