package org.kit.energy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Created by qa5147 on 12.07.2017.
 */
public interface DataPreperator {
    Dataset<Row> prepareDataset(InputFile input, SparkSession spark);
}
