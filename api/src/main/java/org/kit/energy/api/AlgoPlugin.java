package org.kit.energy.api;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.Transformer;

/**
 * Created by qa5147 on 19.06.2017.
 */
public interface AlgoPlugin {
    public Transformer train(Dataset<Row> input);
    public Dataset<Row> applyModel(Dataset<Row> input, Transformer model);
}
