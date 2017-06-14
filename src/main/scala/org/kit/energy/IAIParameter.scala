package org.kit.energy

/**
  * Created by qa5147 on 12.06.2017.
  */
class IAIParameter (name: String, value:String){
  private var paramName = name
  private var paramValue = value
  //private var minValue = min
  //private var maxValue = max

  def setParamName(name:String){this.paramName = name}

  def setParamValue(value:String){this.paramValue = value}

  //def setMinValue(min:String){this.minValue = min}

  //def setMaxValue(max:String){this.maxValue = max}

  def getParamName():String ={return paramName}

  def getParamValue():String ={return paramValue}

  //def getMinValue():String ={return minValue}

  //def getMaxValue():String ={return maxValue}

  override def toString: String = {
    return "name: " + this.paramName + " value: " + this.paramValue
  }
}
