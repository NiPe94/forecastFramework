package org.kit.energy

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.{desc, lag}
import org.apache.spark.sql.types.StructType

/**
  * Created by qa5147 on 09.08.2017.
  */
class PastShifter {

  def shiftData(inputData:DataFrame, spark:SparkSession, shiftParameter:Int) : DataFrame = {

    var i=1
    var j=2
    var shiftedData:DataFrame = inputData
    var a:DataFrame = spark.emptyDataFrame
    var b:DataFrame = spark.emptyDataFrame
    var finalData:DataFrame = spark.emptyDataFrame
    var nameArray = Array("features")

    // shifting (lagging):
    // https://databricks.com/blog/2015/07/15/introducing-window-functions-in-spark-sql.html
    // https://stackoverflow.com/questions/41158115/spark-sql-window-function-lag
    // https://spark.apache.org/docs/1.6.0/api/R/lag.html
    val w = Window.orderBy(desc("label"))

    for(i <- 1 to shiftParameter){
      val columnName = shiftedData.columns(shiftedData.columns.length-1)
      shiftedData = shiftedData.withColumn("Shift"+i, lag("features",i).over(w))
      nameArray = nameArray :+ ("Shift"+i)
    }

    println()
    println("shifted: ")
    println()
    shiftedData.show()

    // filter rows with null
    shiftedData = shiftedData.na.drop()

    val assembler = new VectorAssembler()
      .setInputCols(nameArray)
      .setOutputCol("featuresFinal")

    val output = assembler.transform(shiftedData)

    println()
    println("assembled output: ")
    println()
    output.show()

    var blun = output.select("featuresFinal","label")
    blun = blun.withColumnRenamed("featuresFinal","features").select("features","label")

    println()
    println("final: ")
    println()
    blun.show()

    val hie = 78

    // zip feature and label again
    val label = inputData.select("label")
    a = output
    b = label
    // Merge rows
    val rows = a.rdd.zip(b.rdd).map{
      case (rowLeft, rowRight) => Row.fromSeq(rowLeft.toSeq ++ rowRight.toSeq)}
    // Merge schemas
    val schema = StructType(a.schema.fields ++ b.schema.fields)
    // Create new data frame
    finalData = spark.createDataFrame(rows, schema)

    println()
    println("final data:")
    finalData.show()
    println()

    val hufe=67

    /*
    // zip label with features
    a = ab
    b = label
    // Merge rows
    val rows = a.rdd.zip(b.rdd).map{
      case (rowLeft, rowRight) => Row.fromSeq(rowLeft.toSeq ++ rowRight.toSeq)}
    // Merge schemas
    val schema = StructType(a.schema.fields ++ b.schema.fields)
    // Create new data frame
    val finalDataToReturn = spark.createDataFrame(rows, schema)
    */

    // andreas bartschats code: ***************************

    /*
    // define an ordering
    val w = Window.orderBy("Time")

    // create new columns
    val dataTmp = data
      .withColumn("Load-Min", lag("Load", 1).over(w) )
      .withColumn("Load-Hour", lag("Load", 60).over(w) )
      .withColumn("Load-Day", lag("Load", 1440).over(w) )
      .withColumn("Load-Week", lag("Load", 10080).over(w) )

    println("dataTmp tuple: " + dataTmp.count() )
  // */
    /*
    // now create a feature vector column
    // https://spark.apache.org/docs/2.1.0/ml-features.html#vectorassembler
    val assembler = new VectorAssembler()
      .setInputCols(Array("Load-Min", "Load-Hour", "Load-Day", "Load-Week"))
      .setOutputCol("Features")

    // apply assembler
    val output = assembler.transform(data)
    //.filter(dataTmp("Time").geq("2012-01-08 00:00:00"))  // 10 080 values
    println("output tuple: " + output.count() )

    val output2 = output.filter(output("Time").gt(10080))
    println("output2 tuple: " + output2.count() )


    // commented
    val format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
    //unix_timestamp($"dts", "MM/dd/yyyy HH:mm:ss").cast("timestamp")
    val output2 = output.filter(output("Time").gt(new
Timestamp(format.parse("2012-01-01 00:01:00").getTime)))  // 10 080 values
    // // end commented

    return output2
    */

    //***************************

    return shiftedData
  }

}
