package com.qwert2603.retrobase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark method for query to DataBase.
 * If query is INSERT, DELETE or UPDATE, method should return void.
 * If query is SELECT, method should return java.sql.ResultSet.
 * <p>
 * Query may have params.
 * For example, <query>DELETE FROM spend_test WHERE id = ?<query/>
 * In this case method should be like
 * <code>void deleteRecord(int id) throws SQLException;</code>
 * Parameters of method and their types must correspond to params of query.
 * <p>
 * Method should be defined as <code>throws SQLException</code> or don't throw Exceptions at all.
 * If method "throws SQLException", sql-exceptions will be thrown,
 * else they will be caught and "exception.printStackTrace();" will be called.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface DBQuery {

    /**
     * @return sql-query for method.
     */
    String value();
}
