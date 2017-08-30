package org.kit.energy

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.{concat, concat_ws, lit, udf, lag}

/**
  * Class to combine the already loaded input time series from the spark environment to one single DataFrame to be used by an algorithm.
  */
class DataCombiner {

  /**
    * Combines all DataFrames uploaded in the spark environment to one single DataFrame object so it cab be used by an algorithm.
    * @param spark the spark session to access the spark framework.
    * @param features all uploaded feature data which consists of vectors with values of type double.
    * @param labelData the uploaded label data which consists of values of type double.
    * @return a new DataFrame representing all uploaded data with one column for the combined features and one column for the label.
    */
  def combineLoadedData(labelData:DataFrame, features:List[DataFrame], spark:SparkSession) : DataFrame = {

    // combine the feature dataframes List[DF] with [4,78] [2,3] => DF with [4,78,2,3]
    // for each feature, do: take first and second => erg, then take erg and third => erg

    var i = 0
    var ab: DataFrame = spark.emptyDataFrame
    var a: DataFrame = spark.emptyDataFrame
    var b: DataFrame = spark.emptyDataFrame

    val toVectorNew = udf((i: org.apache.spark.ml.linalg.Vector, j:org.apache.spark.ml.linalg.Vector) =>
      (Vectors.dense((i.toArray.mkString(",")+","+j.toArray.mkString(",")).split(",").map(str => str.toDouble)): org.apache.spark.ml.linalg.Vector))

    // if there is more than 1 feature data in the list, zip them together
    if(features.length > 1){

      for( i <- 0 to features.length - 2){
        a = features(i).withColumnRenamed("data","data"+i)
        if(ab != spark.emptyDataFrame){
          a = ab
        }
        b = features(i+1).withColumnRenamed("data","data"+(i+1))
        // Merge rows
        val rows = a.rdd.zip(b.rdd).map{
          case (rowLeft, rowRight) => Row.fromSeq(rowLeft.toSeq ++ rowRight.toSeq)}
        // Merge schemas
        val schema = StructType(a.schema.fields ++ b.schema.fields)
        // Create new data frame
        ab = spark.createDataFrame(rows, schema)
      }

      var j = 0
      var numberCols = 0

      for( j <- 0 to features.length - 2){
        numberCols = ab.columns.length
        if(j==0){
          ab = ab.withColumn("dataCombined"+j,toVectorNew(ab(ab.columns(j)), ab(ab.columns(j+1))))
        }
        else{
          ab = ab.withColumn("dataCombined"+j,toVectorNew(ab(ab.columns(j+1)), ab(ab.columns(numberCols-1))))
        }
      }
      ab = ab.withColumnRenamed(ab.columns(ab.columns.length-1),"features").select("features")
    }
    else {
      ab = features(0).withColumnRenamed("data","features").select("features")
    }

    // zip label with features
    val label = labelData.withColumnRenamed("data","label").select("label")
    a = ab
    b = label
    // Merge rows
    val rows = a.rdd.zip(b.rdd).map{
      case (rowLeft, rowRight) => Row.fromSeq(rowLeft.toSeq ++ rowRight.toSeq)}
    // Merge schemas
    val schema = StructType(a.schema.fields ++ b.schema.fields)
    // Create new data frame
    val finalDataToReturn = spark.createDataFrame(rows, schema)

    return finalDataToReturn
  }

}
