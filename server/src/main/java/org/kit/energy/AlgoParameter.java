package org.kit.energy;

/**
 * Created by qa5147 on 28.06.2017.
 */
public class AlgoParameter {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AlgoParameter{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}