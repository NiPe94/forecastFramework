package org.kit.energy

import org.apache.spark.sql.SparkSession

/**
  * Created by qa5147 on 19.06.2017.
  */
sealed class SparkEnvironment protected (val masterAdress:String){

  val spark = SparkSession
    .builder()
    .master(masterAdress)
    .appName("Forecast Service")
    .getOrCreate()


}
