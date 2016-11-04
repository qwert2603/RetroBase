package com.qwert2603.retrobase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for interface with methods -- queries to DataBase.
 * All methods should be marked with {@link DBQuery}.
 * <p>
 * url, login & password are needed for access to DB.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DBInterface {
    String url();

    String login();

    String password();
}
