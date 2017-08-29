package org.kit.energy

import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Class to pre process a json file from a OpenTSDB database to generate a DataFrame for the spark algorithms
  */
@Component
class JSONDataPreperator extends DataPreperator{

  def prepareDataset(input: InputFile, spark: SparkSession): DataFrame ={

    // required for using .toDF()-function later
    import spark.implicits._
    //val data = input.asInstanceOf[TSDBFile].jsonData
    val data = input.asInstanceOf[TSDBFile].getJsonData

    // find the boundary of the datapoints within the json-data ( ... "dps":{"time":value,"time":value,"time":value} ... )
    val indexOfDataBegin = data.indexOf("\"dps\":{")
    val indexOfDataEnd = data.indexOf("}", indexOfDataBegin)

    // extract the datapoint section from the json-data ( "time":value,"time":value,"time":value )
    val substring = data.substring(indexOfDataBegin + 7, indexOfDataEnd)

    // split the extracted string on each "," to get all time-value-pairs as an string-array ["time":value | "time":value | "time":value]
    val splittedString = substring.split(",")

    // initialize variables to extract the time values and energy values and to put each of them on separate lists
    var currentCombinedLine:Array[String] = new Array[String](2)
    var currentTime = ""
    var currentPoint = ""
    var pointList = mutable.MutableList[String]()
    var timeList = mutable.MutableList[String]()

    // for each pair of "time":value in the string-array, split the string on ":" and put both string-values to corresponding lists (timelist: time0,time1,time2  pointlist: energy0,energy1,energy2)
    for(line <- splittedString){
      currentCombinedLine = line.split(":")
      currentTime = currentCombinedLine(0).replace("\"","")
      currentPoint = currentCombinedLine(1)
      timeList += currentTime
      pointList += currentPoint
    }

    // via importing implicits above, lists can be converted directly to dataframes
    //val timeDF = timeList.toDF("time")
    val pointDF = pointList.toDF("value")

    // user defined functions
    val toDouble = udf[Double, String](_.replace(",",".").toDouble)
    val toVector = udf((i: String) => (Vectors.dense(i.split(",").map(str => str.replace(",",".").toDouble)): org.apache.spark.ml.linalg.Vector))

    var finalDataToReturn = spark.emptyDataFrame

    if(input.getDataPurpose.equals("label")){
      finalDataToReturn = pointDF.withColumn("data", toDouble(pointDF("value"))).select("data")
    }
    else{
      finalDataToReturn = pointDF.withColumn("data", toVector(pointDF("value"))).select("data")
    }

    /*
    // zip time with values
    val a = timeDF
    val b = pointDF
    // Merge rows
    val rows = a.rdd.zip(b.rdd).map{
      case (rowLeft, rowRight) => Row.fromSeq(rowLeft.toSeq ++ rowRight.toSeq)}
    // Merge schemas
    val schema = StructType(a.schema.fields ++ b.schema.fields)
    // Create new data frame
    val finalDataToReturn = spark.createDataFrame(rows, schema)
    */


    return finalDataToReturn



  }

}
