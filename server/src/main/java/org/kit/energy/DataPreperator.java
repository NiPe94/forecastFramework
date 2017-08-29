package org.kit.energy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Interface for the pre processing of different input formats to generate DataFrames for the spark algorithms.
 */
public interface DataPreperator {

    /**
     * Generates a dataFrame out of a input time series meta data object with the help of spark.
     * The result will be added to the spark environment.
     * @see SparkEnvironment
     * @return a DataFrame with the relevant time series in it.
     * @param input the meta data object of a time series to be loaded.
     * @param spark the spark sql session from the spark environment to create DataFrames.
     * @return the DataFrame with loaded time series.
     */
    Dataset<Row> prepareDataset(InputFile input, SparkSession spark);
}
