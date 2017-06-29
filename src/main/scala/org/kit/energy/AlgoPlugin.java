package org.kit.energy;

import org.apache.spark.sql.Dataset;

import java.util.List;

/**
 * Created by qa5147 on 19.06.2017.
 */
public interface AlgoPlugin {
    public <T> compute(Dataset input, List<AlgoParameter> parameters);
}
