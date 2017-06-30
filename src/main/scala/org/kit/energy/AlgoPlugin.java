package org.kit.energy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

/**
 * Created by qa5147 on 19.06.2017.
 */
public interface AlgoPlugin {
    public String compute(Dataset<Row> input);
}
