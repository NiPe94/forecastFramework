package org.kit.energy

import org.apache.spark.{SparkConf, SparkContext}
import org.springframework.stereotype.Component
import org.apache.spark.sql.SparkSession

/**
  * Created by qa5147 on 16.01.2017.
  */
@Component
class TestClass {

  def startHere(): Unit ={
    println("Here is where scala starts!")

    System.setProperty("hadoop.home.dir", "C:\\winutils-master\\hadoop-2.7.1");

    /* working minimal example
    val conf = new SparkConf().setAppName("Simple Application")
      .setMaster("local")
    val sc = new SparkContext(conf)

    // read example
    val lines = sc.textFile("README.txt")
    val pairs = lines.map(s => (s,1))
    val counts = pairs.reduceByKey((a,b) => a+b)
    counts.collect()
    counts.sortByKey()
    counts.foreach(s => println(s))
     // */

    // Initialize Spark context
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("New Name")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()

    val df = spark.read.csv("PV2015.csv")
    df.show()


    /*
    // accumulator example
    val accum = sc.longAccumulator("My Accu")
    sc.parallelize(Array(1,2,3,4)).foreach(x => accum.add(x))
    println(accum.value)
    */

    // stop the application
    spark.stop()
  }

}
