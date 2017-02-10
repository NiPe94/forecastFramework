package org.kit.energy

import java.nio.ByteBuffer
import java.sql.Timestamp
import java.util
import java.util.Arrays
import java.util.Map.Entry
import org.apache.commons.logging.{Log, LogFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.types.{FloatType, StructField, StructType, TimestampType}
import scala.Array.canBuildFrom
import scala.collection.mutable.ArrayBuffer
class OpenTSDBConnector(zkQuorum: String, zkClientPort: String, tsdbTableName: String = "tsdb", tsdbUidTableName: String = "tsdb-uid") extends Serializable {
  private val zookeeperQuorum = zkQuorum
  private val zookeeperClientPort = zkClientPort
  private val format_data = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm")
  //mapr cluster: "/mapr/iai-energylab.iai.kit.edu/user/mapr/hbase/tsdb"
  private val TSDB_TABLE = tsdbTableName
  //mapr cluster: "/mapr/iai-energylab.iai.kit.edu/user/mapr/hbase/tsdb-uid"
  private val TSDB_UID_TABLE = tsdbUidTableName
  private val LOG: Log = LogFactory.getLog(classOf[OpenTSDBConnector])
  /**
    * Generate RDD for the given query. All tag keys and values must exist, if tag key or value is missing no data
    * will be returned.
    *
    * @param metricName
    * @param tagsKeysValues should be on this format: tagk1->tagv1,tagk2->tagv2,....
    * @param startdate      start date of the query, should be on this format: yyyy/MM/dd HH:mm
    * @param enddate        end date of the query, should be on this format: yyyy/MM/dd HH:mm
    * @param sc             Spark Context
    * @return RDD that represents the time series fetched from opentsdb, will be on this format (time, value)
    */
  def generateRDD(metricName: String, tagsKeysValues: String, startdate: String, enddate: String,
                  sc: SparkContext): RDD[(Long, Float)] = {
    LOG.info("Generating RDD...for metric..." + metricName)
    val tags: Map[String, String] = parseTags(tagsKeysValues)
    val tsdbUID = read_tsdbUID_table(metricName, tags, sc)
    LOG.debug("tsdbUID Count: " + tsdbUID.count)
    LOG.debug("MetricsUID: ")
    val metricsUID = getMetricUID(tsdbUID)
    metricsUID.foreach(arr => LOG.debug(arr.mkString(", ")))
    if (metricsUID.isEmpty) {
      LOG.error("Can't find metric: " + metricName)
      System.exit(1)
    }
    LOG.debug("tagKUIDs: ")
    val tagKUIDs = getTagUIDs(tsdbUID, "tagk")
    tagKUIDs.foreach(m => LOG.debug(m._1 + " => " + m._2.mkString(", ")))
    LOG.debug("tagVUIDs: ")
    val tagVUIDs = getTagUIDs(tsdbUID, "tagv")
    tagVUIDs.foreach(m => LOG.debug(m._1 + " => " + m._2.mkString(", ")))
    // all tags must exist
    if (tags.size != tagKUIDs.size || tagKUIDs.size != tagVUIDs.size) {
      LOG.error("Can't find keys or values")
      // TODO print missing values
      System.exit(1)
    }
    val tagKV = joinTagKeysWithValues(tags, tagKUIDs, tagVUIDs)
    val tsdb = read_tsdb_table(metricsUID, tagKV, startdate, enddate, sc)
    val timeSeriesRDD: RDD[(Long, Float)] = decode_tsdb_table(tsdb)
    timeSeriesRDD
  }
  def getDataFrameForMetric(metricName: String, tagKeyValueMap: String, startdate: String, enddate: String, sc: SparkContext, sqlContext: SQLContext): DataFrame = {
    val rdd = generateRDD(metricName, tagKeyValueMap, startdate, enddate,sc)
    val df  = longFloatRDDToDF(rdd, sqlContext)
    return df
  }
  private def longFloatRDDToDF(rdd: RDD[(Long, Float)], sqlContext: SQLContext): DataFrame = {
    val dataRDDRows = rdd.map { case (x, y) => Row(new Timestamp(x * 1000), y) }
    val fields = Seq(
      StructField("timestamp", TimestampType, true),
      StructField("value", FloatType, true)
    )
    val schema = StructType(fields)
    val df = sqlContext.createDataFrame(dataRDDRows, schema)
    return df
  }
  private def decode_tsdb_table(tsdb: RDD[(ImmutableBytesWritable, Result)]): RDD[(Long, Float)] = {
    //Decoding retrieved data into RDD
    tsdb
      // columns from 3-7 (the base time)
      .map(kv => (Arrays.copyOfRange(kv._1.copyBytes(), 3, 7), kv._2.getFamilyMap("t".getBytes())))
      .flatMap({ //Return a new RDD by first applying a function to all elements of this RDD, and then flattening the results.
        kv =>
          val basetime: Int = ByteBuffer.wrap(kv._1).getInt
          val iterator: util.Iterator[Entry[Array[Byte], Array[Byte]]] = kv._2.entrySet().iterator()
          val arrayBufferOfRows = new ArrayBuffer[(Long, Float)]
          LOG.debug("New Row, basetime: "+ basetime)
          while (iterator.hasNext()) {
            val next = iterator.next()
            val qualifierBytes: Array[Byte] = next.getKey()
            val valueBytes = next.getValue()
            // Column Quantifiers are stored as follows:
            // if num of bytes=2: 12 bits (delta timestamp value in sec) + 4 bits flag
            // if num of bytes=4: 4 bits flag(must be 1111) + 22 bits (delta timestamp value in ms) +
            //                       2 bits reserved + 4 bits flag
            // last 4 bits flag = (1 bit (int 0 | float 1) + 3 bits (length of value bytes - 1))
            // if num of bytes>4 & even: columns with compacted for the hour, with each qualifier having 2 bytes
            // TODO make sure that (qualifier only has 2 bytes)
            // if num of bytes is odd: Annotations or Other Objects
            LOG.debug("New Column:")
            LOG.debug("qualifierBytes:" + bytes2hex(qualifierBytes))
            LOG.debug("valueBytes:" + bytes2hex(valueBytes))
            if (qualifierBytes.length == 2) {
              // 2 bytes qualifier
              parseValues(qualifierBytes, valueBytes, basetime, 2, (x => x >> 4), arrayBufferOfRows)
            } else if (qualifierBytes.length == 4 && is4BytesQualifier(qualifierBytes)) {
              // 4 bytes qualifier
              parseValues(qualifierBytes, valueBytes, basetime, 4, (x => (x << 4) >> 10), arrayBufferOfRows)
            } else if (qualifierBytes.length >= 4 && qualifierBytes.length % 2 == 0) {
              LOG.debug("qualifierBytes length: " + qualifierBytes.length)
              LOG.debug("valueBytes length: " + valueBytes.length)
              var positionInByteArray = 0
              for(i <- qualifierBytes.indices by 2){
                val singleQualifierBytes = Arrays.copyOfRange(qualifierBytes,i,i+2)
                LOG.debug("singleQualifierBytes:" + bytes2hex(singleQualifierBytes))
                val qualifierAsInt = parseQualifier(singleQualifierBytes,0,2)
                val lastBitBool = ((qualifierAsInt) & 1) == 1
                val secondLastBitBool = ((qualifierAsInt >> 1) & 1) == 1
                val thirdLastBitBool = ((qualifierAsInt >> 2) & 1) == 1
                //val s1 = String.format("%8s", Integer.toBinaryString(singleQualifierBytes(0) & 0xFF)).replace(' ', '0');
                //val binaryRepresentation = String.format("%8s", Integer.toBinaryString(singleQualifierBytes(1) & 0xFF)).replace(' ', '0');
                // LOG.debug("Binary: " + s1 + " " + binaryRepresentation);
                //val timeOffset = s1.concat(binaryRepresentation.substring(0,4))
                // first 12 bits of the qualifer represent an integer that is a delta from the timestamp in the row key
                //val timeOffsetInSeconds = Integer.parseInt(timeOffset, 2)
                //LOG.debug("time offset: " + timeOffset + " / " + Integer.parseInt(timeOffset, 2) + " seconds.")
                val test = util.BitSet.valueOf(singleQualifierBytes).get(4,16).toLongArray.headOption
                val timeOffsetInSecondsNewFloatOption = getBitSetFromByteArray(singleQualifierBytes).get(4,16).toLongArray.headOption
                // in compacted rows, the first data-point does not have an offset
                var timeOffsetInSeconds = 0
                if(timeOffsetInSecondsNewFloatOption != None)
                  timeOffsetInSeconds = timeOffsetInSecondsNewFloatOption.get.toInt
                LOG.debug("time offset: " + timeOffsetInSeconds + " seconds.")
                //val last3Bits = binaryRepresentation.substring(binaryRepresentation.length-3,binaryRepresentation.length)
                //LOG.debug(last3Bits)
                var byteOffset = 0
                // last3Bits.equals("000")
                if (!thirdLastBitBool && !secondLastBitBool && !lastBitBool) {
                  byteOffset = 1
                  // last3Bits.equals("001")
                } else if (lastBitBool && !thirdLastBitBool && !secondLastBitBool) {
                  byteOffset = 2
                  // last3Bits.equals("010")
                } else if (secondLastBitBool && !thirdLastBitBool && !lastBitBool ) {
                  byteOffset = 2
                  // last3Bits.equals("011")
                } else if (!thirdLastBitBool && secondLastBitBool && lastBitBool) {
                  byteOffset = 4
                  // last3Bits.equals("100")
                } else if (thirdLastBitBool && !secondLastBitBool && !lastBitBool) {
                  byteOffset = 8
                  // last3Bits.equals("111")
                } else if (thirdLastBitBool && secondLastBitBool && lastBitBool) {
                  byteOffset = 8
                } else {
                  throw new RuntimeException("Last three bits of column qualifier is not 000,001,010,011, 100 or 111")
                }
                val singleValueBytes = Arrays.copyOfRange(valueBytes,positionInByteArray,positionInByteArray + byteOffset)
                positionInByteArray = positionInByteArray + byteOffset
                parseValues(singleQualifierBytes, singleValueBytes, basetime, 2, (x => timeOffsetInSeconds), arrayBufferOfRows)
              }
            } else {
              // TODO (Annotations or Other Objects)
              throw new RuntimeException("Annotations or Other Objects not supported yet")
            }
          }
          arrayBufferOfRows
      })
  }
  private def parseTags(tagKeyValueMap: String) =
    if (tagKeyValueMap.trim != "*") tagKeyValueMap.split(",").map(_.split("->")).map(l => (l(0).trim, l(1).trim)).toMap
    else Map[String, String]()
  private def getMetricUID(tsdbUID: RDD[(ImmutableBytesWritable, Result)]): Array[Array[Byte]] = {
    val metricUIDs: Array[Array[Byte]] = tsdbUID
      .map(l => l._2.getValue("id".getBytes(), "metrics".getBytes()))
      .filter(_ != null).collect //Since we will have only one metric uid
    metricUIDs
  }
  private def getTagUIDs(tsdbUID: RDD[(ImmutableBytesWritable, Result)], qualifier: String): Map[String, Array[Byte]] = {
    val tagkUIDs: Map[String, Array[Byte]] = tsdbUID
      .map(l => (new String(l._1.copyBytes()), l._2.getValue("id".getBytes(), qualifier.getBytes())))
      .filter(_._2 != null).collect.toMap
    tagkUIDs
  }
  private def joinTagKeysWithValues(tags: Map[String, String], tagKUIDs: Map[String, Array[Byte]],
                                    tagVUIDs: Map[String, Array[Byte]]): List[Array[Byte]] = {
    val tagkv: List[Array[Byte]] = tagKUIDs
      .map(k => (k._2, tagVUIDs(tags(k._1)))) // key(byte Array), value(byte Array)
      .map(l => (l._1 ++ l._2)) // key + value
      .toList
      .sorted(Ordering.by((_: Array[Byte]).toIterable))
    tagkv
  }
  private def parseValues(qualifierBytes: Array[Byte], valueBytes: Array[Byte], basetime: Int, step: Int,
                          getOffset: Int => Int, result: ArrayBuffer[(Long, Float)]) = {
    var index = 0
    for (i <- 0 until qualifierBytes.length by step) {
      val qualifier = parseQualifier(qualifierBytes, i, step)
      val timeOffset = getOffset(qualifier)
      val isFloat = (((qualifier >> 3) & 1) == 1)
      val valueByteLength = (qualifier & 7) + 1
      val value = parseValue(valueBytes, index, isFloat, valueByteLength)
      LOG.debug("value: " + value)
      result += ((basetime + timeOffset, value))
      index = index + valueByteLength
    }
  }
  private def parseValue(valueBytes: Array[Byte], index: Int, isFloat: Boolean, valueByteLength: Int): Float = {
    val bufferArr = ByteBuffer.wrap(Arrays.copyOfRange(valueBytes, index, index + valueByteLength))
    if (isFloat) {
      if (valueByteLength == 4) bufferArr.getFloat()
      else if (valueByteLength == 8) bufferArr.getDouble().toFloat
      else throw new IllegalArgumentException(s"Can't parse Value (isFloat:$isFloat, valueByteLength:$valueByteLength")
    } else {
      if (valueByteLength == 1) valueBytes(index).toFloat
      else if (valueByteLength == 2) bufferArr.getShort.toFloat
      else if (valueByteLength == 4) bufferArr.getInt.toFloat
      else if (valueByteLength == 8) bufferArr.getLong.toFloat
      else throw new IllegalArgumentException(s"Can't parse Value (isFloat:$isFloat, valueByteLength:$valueByteLength")
    }
  }
  private def parseQualifier(arr: Array[Byte], startIdx: Int, endIdx: Int) = {
    val length = endIdx - startIdx
    var value = 0
    for (i <- startIdx until endIdx)
      value = value | (arr(i).toInt << ((length - 1 - i) * 8))
    value
  }
  private def is4BytesQualifier(qualifierBytes: Array[Byte]): Boolean = {
    val firstByte = qualifierBytes(0)
    ((firstByte >> 4) & 15) == 15 // first 4 bytes = 1111
  }
  private def read_tsdbUID_table(metricName: String, tags: Map[String, String], sc: SparkContext):
  RDD[(ImmutableBytesWritable, Result)] = {
    val tsdbUID = sc.newAPIHadoopRDD(
      tsdbuidConfig(
        zookeeperQuorum,
        zookeeperClientPort,
        Array(metricName, tags.map(_._1).mkString("|"), tags.map(_._2).mkString("|"))
      ),
      classOf[org.kit.energy.TSDBInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])
    tsdbUID
  }
  private def read_tsdb_table(metricsUID: Array[Array[Byte]], tagKV: List[Array[Byte]], startdate: String,
                              enddate: String, sc: SparkContext): RDD[(ImmutableBytesWritable, Result)] = {
    val tsdb = sc.newAPIHadoopRDD(
      tsdbConfig(
        zookeeperQuorum,
        zookeeperClientPort,
        metricsUID.last,
        if (tagKV.size != 0) Option(tagKV.flatten.toArray) else None,
        if (startdate != "*") Option(startdate) else None,
        if (enddate != "*") Option(enddate) else None
      ),
      classOf[org.kit.energy.TSDBInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])
    tsdb
  }
  //Prepares the configuration for querying the TSDB-UID table and extracting UIDs for metrics and tags
  private def tsdbuidConfig(zookeeperQuorum: String, zookeeperClientPort: String, columnQ: Array[String]) = {
    val config = generalConfig(zookeeperQuorum, zookeeperClientPort)
    config.set(TableInputFormat.INPUT_TABLE, TSDB_UID_TABLE)
    config.set(TSDBInputFormat.TSDB_UIDS, columnQ.mkString("|"))
    config
  }
  //Prepares the configuration for querying the TSDB table
  private def tsdbConfig(zookeeperQuorum: String, zookeeperClientPort: String,
                         metricUID: Array[Byte], tagkv: Option[Array[Byte]] = None,
                         startdate: Option[String] = None, enddate: Option[String] = None): Configuration = {
    val config = generalConfig(zookeeperQuorum, zookeeperClientPort)
    config.set(TableInputFormat.INPUT_TABLE, TSDB_TABLE)
    config.set(TSDBInputFormat.METRICS, bytes2hex(metricUID))
    if (tagkv != None) {
      config.set(TSDBInputFormat.TAGKV, bytes2hex(tagkv.get))
    }
    if (startdate != None)
      config.set(TSDBInputFormat.SCAN_TIMERANGE_START, getTime(startdate.get))
    if (enddate != None)
      config.set(TSDBInputFormat.SCAN_TIMERANGE_END, getTime(enddate.get))
    config
  }
  private def generalConfig(zookeeperQuorum: String, zookeeperClientPort: String): Configuration = {
    //Create configuration
    val config = HBaseConfiguration.create()
    //config.addResource(System.getenv("HBASE_HOME") + "/conf/hbase-site.xml")
    config.set("hbase.zookeeper.quorum", zookeeperQuorum)
    config.set("hbase.zookeeper.property.clientPort", zookeeperClientPort)
    config.set("hbase.mapreduce.scan.column.family", "t")
    if(TSDB_UID_TABLE.contains("/")) {
      config.set("mapr.hbase.default.db", "maprdb");
    }
    config
  }
  private def getTime(date: String): String = {
    val MAX_TIMESPAN = 3600
    def getBaseTime(date: String): Int = {
      val timestamp = format_data.parse(date).getTime()
      val base_time = ((timestamp / 1000) - ((timestamp / 1000) % MAX_TIMESPAN)).toInt
      base_time
    }
    val baseTime = getBaseTime(date)
    //Integer.BYTES from Java 8:
    val integerBytes = 4
    val intByteArray: Array[Byte] = ByteBuffer.allocate(integerBytes).putInt(baseTime).array()
    bytes2hex(intByteArray)
  }
  //Converts Bytes to Hex (see: https://gist.github.com/tmyymmt/3727124)
  private def bytes2hex(bytes: Array[Byte]): String = {
    val sep = "\\x"
    val regex = sep + bytes.map("%02x".format(_).toUpperCase()).mkString(sep)
    regex
  }
  def getBitSetFromByteArray(bytes: Array[Byte]): util.BitSet = {
    val bits = new util.BitSet()
    for (i <- 0 until bytes.length*8) {
      if ((bytes(bytes.length - i / 8 - 1) & (1 << (i % 8))) > 0) {
        bits.set(i)
      }
    }
    bits
  }
}
