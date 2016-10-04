package com.qwert2603.retrobase.rx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import rx.Observable;

/**
 * Annotation to mark method for query to DataBase.
 *
 * For every annotated method will be generated method-wrapper,
 * that call annotated method and return its result as {@link Observable ).
 *
 * Method should return void or {@link java.sql.ResultSet}.
 *
 * If method returns void, method-wrapper will return {@link Observable<Object>}
 * and call <code>subscriber.onNext(new Object());</code> only once after query to DB executed.
 * Then it calls <code>subscriber.onCompleted();</code>.
 *
 * If method returns {@link java.sql.ResultSet}, method-wrapper will return Observable<*classname*>,
 * where *classname* is defined with {@link DBMakeRx#modelClassName()}.
 * That's why *classname* must have open constructor, that get {@link java.sql.ResultSet}.
 * Method-wrapper call subscriber.onNext(new *classname*(resultSet)) for every record from {@link java.sql.ResultSet}.
 * Then it calls subscriber.onCompleted().
 *
 * If annotated method throws any exceptions, they will be caught and sent to <code>subscriber.onError(exception)</code>.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface DBMakeRx {

    /**
     * @return name of class, that will be received from {@link java.sql.ResultSet} throw his constructor.
     * That's why that class must have open constructor, that get {@link java.sql.ResultSet}.
     */
    String modelClassName() default "java.lang.Object";
}
