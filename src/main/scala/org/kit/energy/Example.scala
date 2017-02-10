package org.kit.energy

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
object Example {
  Logger.getLogger("org").setLevel(Level.INFO)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("edu.kit.iai").setLevel(Level.INFO)
  def main(args: Array[String]) {
    var zookeeperQuorum = "127.0.0.1" //"ip.or.hostname"
    if (args.length > 0) {
      zookeeperQuorum = args(0)
    }
    var zookeeperClientPort = "2181" //"zookeeper port"
    if (args.length > 1) {
      zookeeperClientPort = args(1)
    }
    var metric = "testmetrik" //"Metric.Name"
    if (args.length > 2) {
      metric = args(2)
    }
    var tagVal = "*" //"tag.key->tag.value" (can also be * or tag.key->*)
    if (args.length > 3) {
      tagVal = args(3)
    }
    var startD = "*" //"yyyy/MM/dd HH:mm" (or can be *)
    if (args.length > 4) {
      startD = args(4)
    }
    var endD = "*" //"yyyy/MM/dd HH:mm" (or can be *)
    if (args.length > 5) {
      endD = args(5)
    }
    var tsdbTableName = "tsdb"
    if (args.length > 6) {
      tsdbTableName = args(6)
    }
    var tsdbUidTableName = "tsdb-uid"
    if (args.length > 7) {
      tsdbUidTableName = args(7)
    }
    println("ZookeeperQuorum: " + zookeeperQuorum)
    println("ZookeeperPort: " +zookeeperClientPort)
    println("TSDB-Table Name in HBase: " + tsdbTableName)
    println("TSDB-UID-Table Name in HBase: " + tsdbUidTableName)
    println("metric=" + metric + ", tagvalues=" + tagVal + ", startdate= " + startD + ", enddate=" + endD)
    // Cluster mode:
    //val sc = new SparkContext
    // Local Mode
    val conf = new SparkConf()
      .setAppName("TSDBQuery")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)
    //Connection to OpenTSDB
    val sparkTSDB = new OpenTSDBConnector(zookeeperQuorum, zookeeperClientPort, tsdbTableName, tsdbUidTableName)
    //Create RDD from OpenTSDB
    //val dataRDD = sparkTSDB.getRDDForMetric(metricName = metric, tagKeyValueMap = tagVal, startdate = startD, enddate = endD, sc)
    //val tempRDD = sparkTSDB.getRDDForMetric("T2_STDW","*","*","*", sc)
    //dataRDD.collect.take(1).foreach(println)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    // val rdd = sparkTSDB.generateRDD("T2_STDW_60","date->13Sept","2010/01/01 00:00","2010/01/01 10:00",sc)
    // rdd.cache().take(1000).foreach(println)
    //    val heatDF = sparkTSDB.getDataFrameForMetric(metricName = "Y_STDW", tagKeyValueMap = "*", startdate = startD, enddate = endD, sc, sqlContext)
    val t1 = System.nanoTime
    val tempDF = sparkTSDB.getDataFrameForMetric(metricName = metric, tagKeyValueMap = tagVal, startdate = startD, enddate = endD, sc, sqlContext)
    tempDF.registerTempTable("temp")
    sqlContext.sql("select count(*) from temp").show
    //    heatDF.cache()
    //    heatDF.registerTempTable("heat")
    //    tempDF.cache()
    //    tempDF.registerTempTable("temp")
    //    tempDF.take(100).foreach(println)
    //      sqlContext.sql("select avg(value) AS average from temp").show(1)
    //    sqlContext.sql("select count(*) AS count from temp").show(1)
    val duration = (System.nanoTime - t1) / 1e9d
    println("Duration: " + duration)
    //    sqlContext.sql("select * from temp order by timestamp ASC limit 100").show(100)
    //
    //    val t1 = System.nanoTime
    //    val mean = sqlContext.sql("SELECT avg(value) as AVG_TEMP FROM temp")
    //    val meanHeat = sqlContext.sql("SELECT avg(value) as AVG_HEAT FROM heat")
    //    mean.take(1).foreach(println)
    //    meanHeat.take(1).foreach(println)
    //
    //    val joinedTable = sqlContext.sql("SELECT heat.timestamp AS timestamp, heat.value AS GWA, temp.value AS TEMP FROM heat INNER JOIN temp ON heat.timestamp = temp.timestamp")
    //    val dfWithDate = joinedTable.withColumn("date", date_format(col("timestamp"), "yyyy-MM-dd"))
    //    var dfWithDailyAggr = dfWithDate.groupBy("date").agg(avg("GWA").alias("GWA"), avg("TEMP").alias("TEMP"))
    //    dfWithDailyAggr.cache()
    //    dfWithDailyAggr.take(10).foreach(println)
    //    dfWithDailyAggr.printSchema()
    //    val duration = (System.nanoTime - t1) / 1e9d
    //    println("Duration of aggregation and join of two metrics: " + duration)
    //    println("Number of original DataPoints:" + joinedTable.count())
    //    println("Number of new DataPoints: " + dfWithDailyAggr.count())
    sc.stop
  }
}
