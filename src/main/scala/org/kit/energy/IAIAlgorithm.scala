package org.kit.energy
import org.apache.spark.sql.DataFrame
import scala.collection.immutable._
/**
  * Created by qa5147 on 12.06.2017.
  */
abstract class IAIAlgorithm {
  //var paramList: List[IAIParameter] = List()
  //var algoName = ""

  //def setParamList(list:List[IAIParameter]){this.paramList = list}

  //def setAlgoName(name:String){this.algoName = name}

  //def getParamList():List[IAIParameter] ={return paramList}

  //def getAlgoName():String ={return algoName}

  def execute(paramList:List[IAIParameter], inputData:DataFrame):String
}
