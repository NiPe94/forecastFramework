package org.kit.energy

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
  * Created by qa5147 on 08.08.2017.
  */
class DataCombiner {

  def combineLoadedData(labelData:DataFrame, features:List[DataFrame], spark:SparkSession) : DataFrame = {

    // combine the feature dataframes List[DF] with [4,78] [2,3] => DF with [4,78,2,3]
    // for each feature, do: take first and second => erg, then take erg and third => erg
    var i = 0
    var ab: DataFrame = spark.emptyDataFrame
    var a: DataFrame = spark.emptyDataFrame
    var b: DataFrame = spark.emptyDataFrame
    if(features.length > 1){
      for( i <- 0 to features.length - 2){
        a = features(i)
        if(ab != spark.emptyDataFrame){
          a = ab
        }
        b = features(i+1)

        // Merge rows
        val rows = a.rdd.zip(b.rdd).map{
          case (rowLeft, rowRight) => Row.fromSeq(rowLeft.toSeq ++ rowRight.toSeq)}

        // Merge schemas
        val schema = StructType(a.schema.fields ++ b.schema.fields)

        // Create new data frame
        ab = spark.createDataFrame(rows, schema)
      }
    }
    else {
      ab = labelData
    }
    ab.show()
    val blub = 9

    // combine label with features

    return labelData
  }

}
