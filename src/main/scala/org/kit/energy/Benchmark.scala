package org.kit.energy

import org.apache.spark.ml.Transformer
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.storage.StorageLevel

/**
  * Created by qa5147 on 19.06.2017.
  */
class Benchmark extends AlgoPlugin{

  @AlgoParam(name="Regularization2", value="0.3")
  private var regParam2:String = _

  @AlgoParam(name="Elastic Net2", value="1.0")
  private var elasticNet2:String = _

  @AlgoParam(name="Repartition2", value="16")
  var repart2:String  =_

  def train(inputData: DataFrame): Transformer = {

    var benchResult:List[String] = List("")
    var currentResult = ""
    val repartition16 = 4
    val storageLevelNone = StorageLevel.NONE
    val storageLevelWith = StorageLevel.MEMORY_ONLY

    // Storage None, rep 16, MLR
    val regul1 = 0.0
    val elast1 = 0.0

    val cachedInput1 = inputData.persist(storageLevelNone).repartition(repartition16)
    val lrModelStart1 = new LinearRegression().setRegParam(regul1).setElasticNetParam(elast1)
    val ta1 = System.nanoTime()
    var lrModel1 = lrModelStart1.fit(cachedInput1)
    val durationa1 = ((System.nanoTime() - ta1)/1e9d)
    val tb1 = System.nanoTime()
    val evaluation1 = lrModel1.evaluate(cachedInput1)
    val durationb1 = ((System.nanoTime() - tb1)/1e9d)

    currentResult = "persist: "+storageLevelNone.toString() +" elastic: "+ elast1.toString() + " training: " + durationa1.toString() + " evaluation: "+ durationb1.toString()
    benchResult = benchResult :+ currentResult

    // Storage Yes, rep 16, MLR

    val cachedInput2 = inputData.persist(storageLevelWith).repartition(repartition16)
    val lrModelStart2 = new LinearRegression().setRegParam(regul1).setElasticNetParam(elast1)
    val ta2 = System.nanoTime()
    var lrModel2 = lrModelStart2.fit(cachedInput2)
    val durationa2 = ((System.nanoTime() - ta2)/1e9d)
    val tb2 = System.nanoTime()
    val evaluation2 = lrModel2.evaluate(cachedInput2)
    val durationb2 = ((System.nanoTime() - tb2)/1e9d)

    currentResult = "persist: "+storageLevelWith.toString() +" elastic: "+ elast1.toString() + " training: " + durationa2.toString() + " evaluation: "+ durationb2.toString()
    benchResult = benchResult :+ currentResult

    // Storage None, rep 16, Lasso
    val regul2 = 0.3
    val elast2 = 1.0

    val cachedInput3 = inputData.persist(storageLevelNone).repartition(repartition16)
    val lrModelStart3 = new LinearRegression().setRegParam(regul2).setElasticNetParam(elast2)
    val ta3 = System.nanoTime()
    var lrModel3 = lrModelStart3.fit(cachedInput3)
    val durationa3 = ((System.nanoTime() - ta3)/1e9d)
    val tb3 = System.nanoTime()
    val evaluation3 = lrModel3.evaluate(cachedInput3)
    val durationb3 = ((System.nanoTime() - tb3)/1e9d)

    currentResult = "persist: "+storageLevelNone.toString() +" elastic: "+ elast2.toString() + " training: " + durationa3.toString() + " evaluation: "+ durationb3.toString()
    benchResult = benchResult :+ currentResult

    // Storage Mem, rep 16, Lasso
    val cachedInput4 = inputData.persist(storageLevelWith).repartition(repartition16)
    val lrModelStart4 = new LinearRegression().setRegParam(regul2).setElasticNetParam(elast2)
    val ta4 = System.nanoTime()
    var lrModel4 = lrModelStart4.fit(cachedInput4)
    val durationa4 = ((System.nanoTime() - ta4)/1e9d)
    val tb4 = System.nanoTime()
    val evaluation4 = lrModel4.evaluate(cachedInput4)
    val durationb4 = ((System.nanoTime() - tb4)/1e9d)

    currentResult = "persist: "+storageLevelWith.toString() +" elastic: "+ elast2.toString() + " training: " + durationa4.toString() + " evaluation: "+ durationb4.toString()
    benchResult = benchResult :+ currentResult

    println()
    println()
    println("*************************************")
    benchResult.foreach(u => println(u))
    println("*************************************")
    println()
    println()

    return lrModel1
  }

  def applyModel(input: DataFrame, model: Transformer): DataFrame = {
    val transformedData = model.transform(input)
    return transformedData
  }
}
