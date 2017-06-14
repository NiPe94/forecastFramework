package org.kit.energy

import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SparkSession}
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by qa5147 on 12.06.2017.
  */
@Component
class JSONDataPreperator {

  def prepareDataSet(data: String): Unit ={

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    // required for using .toDF()-function later
    import spark.implicits._

    // find the boundary of the datapoints within the json-data ( ... "dps":{"time":value,"time":value,"time":value} ... )
    val indexOfDataBegin = data.indexOf("\"dps\":{")
    val indexOfDataEnd = data.indexOf("}", indexOfDataBegin)

    // extract the datapoint section from the json-data ( "time":value,"time":value,"time":value )
    val substring = data.substring(indexOfDataBegin + 7, indexOfDataEnd)

    // split the extracted string on each "," to get all time-value-pairs as an string-array ["time":value | "time":value | "time":value]
    val splittedString = substring.split(",")

    // initialize variables to extract the time values and energy values and to put each of them on seperate lists
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
    val timeDF = timeList.toDF("time")
    val pointDF = pointList.toDF("value")

    println("Here the DFs come:")
    timeDF.show()
    pointDF.show()


    // Ziel: Dataframe mit Spalten time|value
    // Seq(time,value), schema: (time: String, value: String)
    // spark.createDataframe(seq,schema)

    val bla = 0

    //val timelist = times.toList
    //val pointlist = points.toList

    // At this point, we have a list for the time data and the amplitude data





  }

}
