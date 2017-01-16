package org.kit.energy;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * Created by qa5147 on 13.01.2017.
 */
@Component
public class SparkModel {
    public void doStuff(){
        // First output to see if it works till here
        System.out.println("I do stuff, dough!");


        /*
         // Initialize Spark for Java
        SparkConf conf = new SparkConf().setAppName("simple app")
                .setMaster("local").set("spark.executor.memory","1g");
        JavaSparkContext sc = new JavaSparkContext(conf);
        */

        /*
        // Parallelized Collections example
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> distData = sc.parallelize(data);
        distData.reduce((a,b) -> a+b);
        System.out.println(distData.collect());
        */

        /*
        // Key-Value-Pairs printing example
        // each line of the file is one element
        JavaRDD<String> lines = sc.textFile("README.txt");
        // each element of the dataset is mapped to a tuple as (key: line, value: 1)
        JavaPairRDD<String, Integer> pairs = lines.mapToPair(s -> new Tuple2(s,1));
        // all elements with the same key (here: the line) will be reduced using the given function (sum the values of tupels with equal keys)
        JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a,b) -> a+b);
        counts.sortByKey();
        counts.collect();
        System.out.println("Here it comes!:");
        counts.foreach(l -> System.out.println(l._1() + " " + l._2()));
        */

        /*
        // Text reading and counting example
        String logFile = "README.txt";
        JavaRDD<String> logData = sc.textFile(logFile).cache();
        long numAs = logData.filter(s -> s.contains("a")).count();
        long numBs = logData.filter(s -> s.contains("b")).count();
        System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);
        */


        // stop spark
        //sc.stop();

    }

}

