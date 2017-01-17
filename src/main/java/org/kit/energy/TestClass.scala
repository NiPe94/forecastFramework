package org.kit.energy

import org.apache.spark
import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.stereotype.Component
import org.apache.spark.sql

/**
  * Created by qa5147 on 16.01.2017.
  */
@Component
class TestClass {

  def startHere(): Unit ={
    println("Here is where scala starts!")

    // Initialize Spark context
    val conf = new SparkConf().setAppName("simple App").setMaster("local")
    val sc = new SparkContext(conf)


    /*
    // read example
    val lines = sc.textFile("README.txt")
    val pairs = lines.map(s => (s,1))
    val counts = pairs.reduceByKey((a,b) => a+b)
    counts.collect()
    counts.sortByKey()
    counts.foreach(s => println(s))
    */

    /*
    // accumulator example
    val accum = sc.longAccumulator("My Accu")
    sc.parallelize(Array(1,2,3,4)).foreach(x => accum.add(x))
    println(accum.value)
    */

    // stop the application
    sc.stop()
  }

}
