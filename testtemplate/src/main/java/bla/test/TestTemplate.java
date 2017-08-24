package bla.test;

import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.kit.energy.api.AlgoParam;
import org.kit.energy.api.AlgoPlugin;

/**
 * @author Robin Engel
 */
public class TestTemplate implements AlgoPlugin {

    @AlgoParam(name="testName",value = "0.6")
    public String testPara = "";

    public Transformer train(Dataset<Row> input) {
        System.out.println("oh my god, i start yeah!!!!");
        return null;
    }

    public Dataset<Row> applyModel(Dataset<Row> input, Transformer model) {
        return null;
    }
}
