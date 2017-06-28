package org.kit.energy;

import org.apache.commons.digester.annotations.rules.BeanPropertySetter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by qa5147 on 19.06.2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AlgoParam {
    String name();
    String value = "";
}
