package org.kit.energy.api;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.Transformer;

/**
 * This is the interface for defining a new forecast algorithm.
 * A forecast algorithm has to implement two methods:
 * A training method "train()" to train a model and
 * a method "applyModel()" to apply such a trained model to a given dataset.
 */
public interface AlgoPlugin {
    public Transformer train(Dataset<Row> input);
    public Dataset<Row> applyModel(Dataset<Row> input, Transformer model);
}
