package bla.test;

import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.kit.energy.api.AlgoParam;
import org.kit.energy.api.AlgoPlugin;

/**
 * This class can be used to implement and build an java based algorithm for the webservice
 */
public class TestTemplate implements AlgoPlugin {

    @AlgoParam(name="testReg",value = "0.62")
    public String testPara = "";

    @AlgoParam(name="testNet",value = "1")
    public String testPara2 = "";

    public Transformer train(Dataset<Row> input) {

        LinearRegression lrModelStart = new LinearRegression().setRegParam(Double.parseDouble(testPara)).setElasticNetParam(Double.parseDouble(testPara2));

        LinearRegressionModel lrModel = lrModelStart.fit(input);

        return lrModel;
    }

    public Dataset<Row> applyModel(Dataset<Row> input, Transformer model) {

        Dataset<Row> transformedData = model.transform(input).select("prediction");

        return transformedData;
    }
}
