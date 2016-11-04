package com.qwert2603.retrobase.rx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.reactivex.Completable;
import io.reactivex.Emitter;
import io.reactivex.Observable;

/**
 * Annotation to mark method for query to DataBase.
 * <p>
 * For every annotated method will be generated method-wrapper,
 * that call annotated method and return its result as {@link Observable)} or {@link Completable}.
 * <p>
 * Method should return void or {@link java.sql.ResultSet}.
 * <p>
 * If annotated method returns void, method-wrapper will return {@link Completable},
 * that completes after successful query execution.
 * <p>
 * If annotated method returns {@link java.sql.ResultSet}, method-wrapper will return Observable<*classname*>,
 * where *classname* is defined with {@link DBMakeRx#modelClassName()}.
 * That's why *classname* must have open constructor, that get {@link java.sql.ResultSet}.
 * Method-wrapper calls <code>Emitter#onNext(new *classname*(resultSet))</code> for every record from {@link java.sql.ResultSet}.
 * Then it calls {@link Emitter#onComplete()}.
 * <p>
 * If annotated method throws any exceptions, they will be caught and sent to {@link Emitter#onError(Throwable)}.
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
