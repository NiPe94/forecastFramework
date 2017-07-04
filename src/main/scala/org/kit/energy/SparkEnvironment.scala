package org.kit.energy

import org.apache.spark.sql.SparkSession

/**
  * Created by qa5147 on 19.06.2017.
  */
sealed class SparkEnvironment protected (val masterAdress:String){

  private var spark:SparkSession = null

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

}
