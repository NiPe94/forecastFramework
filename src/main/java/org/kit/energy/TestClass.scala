package org.kit.energy

import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.stereotype.Component

/**
  * Created by qa5147 on 16.01.2017.
  */
@Component
class TestClass {

  def startHere(): Unit ={
    println("Here is where scala starts!")
    val conf = new SparkConf().setAppName("simple App").setMaster("local")
    val sc = new SparkContext(conf)
    val lines = sc.textFile("README.txt")
    val pairs = lines.map(s => (s,1))
    val counts = pairs.reduceByKey((a,b) => a+b)
    counts.collect()
    counts.sortByKey()
    counts.foreach(s => println(s))
    sc.stop()
  }

}
