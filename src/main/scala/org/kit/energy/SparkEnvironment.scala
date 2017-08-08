package org.kit.energy

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by qa5147 on 19.06.2017.
  */
sealed class SparkEnvironment protected (val masterAdress:String){

  private var spark:SparkSession = null

  private var features:List[DataFrame] = List.empty

  private var label:DataFrame = null

  def getInstance() : SparkSession = {
    if(this.spark != null){
      return this.spark
    }
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
  }

  def getFeatures() : List[DataFrame] = {
    return this.features
  }

  def getLabel() : DataFrame = {
    return this.label
  }

}
