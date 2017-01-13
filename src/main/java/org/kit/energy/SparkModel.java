package org.kit.energy;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.stereotype.Component;

/**
 * Created by qa5147 on 13.01.2017.
 */
@Component
public class SparkModel {
    public void doStuff(){
        System.out.println("I do stuff, dough!");
        String logFile = "README.txt";
        SparkConf conf = new SparkConf().setAppName("simple app")
                .setMaster("local").set("spark.executor.memory","1g");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(logFile).cache();

        long numAs = logData.filter(s -> s.contains("a")).count();
        long numBs = logData.filter(s -> s.contains("b")).count();

        System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);

        sc.stop();
    }

}

