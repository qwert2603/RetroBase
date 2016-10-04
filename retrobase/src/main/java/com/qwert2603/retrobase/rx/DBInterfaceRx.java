package com.qwert2603.retrobase.rx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for class or interface, that contains methods-queries to DataBase.
 * Methods-queries to DB should be marked with {@link DBMakeRx}.
 *
 * For every class marked with this annotations will be generated class with open constructor,
 * that gets object of annotated class and calls his methods.
 *
 * Name of generated class will be *classname* + "Rx".
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DBInterfaceRx {
}
