package org.kit.energy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import scala.collection.immutable.List;

/**
 * Created by qa5147 on 14.06.2017.
 */
public class CheaterAlgo extends IAIAlgorithm {

    static final IAIParameter firstPara = new IAIParameter("elasticSomething","0.7");

    @Override
    public String execute(List<IAIParameter> paramList, Dataset<Row> inputData) {
        return "";
    }
}
