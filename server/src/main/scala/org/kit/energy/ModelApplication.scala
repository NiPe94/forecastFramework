package org.kit.energy

import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.round

/**
  * Created by qa5147 on 29.05.2017.
  */
class ModelApplication {

  def applicateModel(lr: LinearRegressionModel, df: sql.DataFrame, savePathCSV:String) : Unit = {

    // predict a datapoint with loaded/used model
    val transformedData = lr.transform(df)

    // show data head
    println("transformed Data schema:")
    transformedData.printSchema()

    // show some data points
    println("transformed data:")
    transformedData.show()

    println("begin saving:")
    val selectedDataBefore = transformedData.withColumn("prediction2", round(new Column("prediction"),3))
    val selectedData = selectedDataBefore.select("label", "prediction2")
    selectedData
      .write
      .format("com.databricks.spark.csv")
      .option("header","false")
      .option("sep",";")
      .mode("overwrite")
      .save(savePathCSV)

  }


}
