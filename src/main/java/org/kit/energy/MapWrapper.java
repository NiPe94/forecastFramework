package org.kit.energy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qa5147 on 27.06.2017.
 */
public class MapWrapper {
    private Map<String, List<AlgoParam>> map = new HashMap<>();

    public Map<String, List<AlgoParam>> getMap() {
        return map;
    }

    public void setMap(Map<String, List<AlgoParam>> map) {
        this.map = map;
    }
}
