package org.kit.energy.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is the definition for the annotation used to write input parameters for an algorithm.
 * The annotations above the interface are saying that the annotation can be parsed during runtime and
 * that the annotation can only be used for fields (attributes).
 * This annotation for input parameters holds a name for the parameter name and
 * a value for the default value of the parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AlgoParam {
    String name();
    String value();
}
