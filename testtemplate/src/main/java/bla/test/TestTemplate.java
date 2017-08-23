package bla.test;

import org.apache.spark.ml.Transformer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.kit.energy.api.AlgoPlugin;

/**
 * @author Robin Engel
 */
public class TestTemplate implements AlgoPlugin {
    public Transformer train(Dataset<Row> input) {
        return null;
    }

    public Dataset<Row> applyModel(Dataset<Row> input, Transformer model) {
        return null;
    }
}
