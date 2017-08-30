package org.kit.energy

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.{desc, lag, monotonically_increasing_id}
import org.apache.spark.sql.types.StructType

/**
  * Class to lag the input data so a ARX model can be generated.
  * For more information about lagging, visit:
  * databricks.com/blog/2015/07/15/introducing-window-functions-in-spark-sql.html
  * stackoverflow.com/questions/41158115/spark-sql-window-function-lag
  * spark.apache.org/docs/1.6.0/api/R/lag.html
  * www.otexts.org/fpp/8/3
  */
class PastShifter {

  /**
    * lags the input DataFrame to be able to train a arx model.
    * @param inputData the input DataFrame which has features and label values inside.
    * @param spark the access to the spark framework.
    * @param shiftParameter the number of shifts to be applied to the input data. So for a AR(1) model, this is 1.
    * @return a DataFrame with new lagged features.
    */
  def shiftData(inputData:DataFrame, spark:SparkSession, shiftParameter:Int) : DataFrame = {

    var i=1
    var j=2
    var shiftedData:DataFrame = inputData
    var a:DataFrame = spark.emptyDataFrame
    var b:DataFrame = spark.emptyDataFrame
    var finalData:DataFrame = spark.emptyDataFrame
    var nameArray:Array[String] = Array()

    // shifting (lagging):
    // https://databricks.com/blog/2015/07/15/introducing-window-functions-in-spark-sql.html
    // https://stackoverflow.com/questions/41158115/spark-sql-window-function-lag
    // https://spark.apache.org/docs/1.6.0/api/R/lag.html
    val w = Window.orderBy("time")

    // when there is no time column for ordering the data, create one
    if(!inputData.columns.mkString(" ").contains("time")){
      shiftedData = shiftedData.withColumn("time",monotonically_increasing_id())
    }

    for(i <- 1 to shiftParameter){
      //val columnName = shiftedData.columns(shiftedData.columns.length-2)
      shiftedData = shiftedData.withColumn("Shift"+i, lag("features",i).over(w))
      nameArray = nameArray :+ ("Shift"+i)
    }

    // example for moving average implementation (not used here):
    // http://xinhstechblog.blogspot.de/2016/04/spark-window-functions-for-dataframes.html

    // filter rows with null
    shiftedData = shiftedData.na.drop()

    // when more than the half of the feature vector consists of zeros, a SparseVector with (size, [idx1,idx2,idx3...], [val1, val2, val3,...]) will be created.
    // Otherwise a DenseVector with [val0,val1,val2,..] will be created
    // https://stackoverflow.com/questions/42719087/spark-1-6-vectorassembler-unexpected-results?rq=1
    // https://stackoverflow.com/questions/40505805/spark-ml-vectorassembler-returns-strange-output
    val assembler = new VectorAssembler()
      .setInputCols(nameArray)
      .setOutputCol("featuresFinal")

    val output = assembler.transform(shiftedData)

    finalData = output.select("featuresFinal","label")
    finalData = finalData.withColumnRenamed("featuresFinal","features").select("features","label")


    return finalData
  }

}
