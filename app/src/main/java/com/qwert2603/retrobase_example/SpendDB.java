package com.qwert2603.retrobase_example;

import com.qwert2603.retrobase.DBInterface;
import com.qwert2603.retrobase.DBQuery;
import com.qwert2603.retrobase.rx.DBInterfaceRx;
import com.qwert2603.retrobase.rx.DBMakeRx;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@DBInterface(url = SpendDB.URL, login = SpendDB.USER_NAME, password = SpendDB.PASSWORD)
@DBInterfaceRx
public interface SpendDB {

    String USER_NAME = "postgres";
    String PASSWORD = "1234";
    String URL = "jdbc:postgresql://192.168.1.26:5432/spend";

    @DBMakeRx(modelClassName = "com.qwert2603.retrobase_example.DataBaseRecord")
    @DBQuery("SELECT * from spend_test")
    ResultSet getAllRecords();

    @DBMakeRx(modelClassName = "com.qwert2603.retrobase_example.DataBaseRecord")
    @DBQuery("SELECT * FROM spend ORDER BY date, id")
    ResultSet getAllRecordsOrdered() throws SQLException;

    @DBMakeRx
    @DBQuery("DELETE FROM spend_test WHERE id = ?")
    void deleteRecord(int id) throws SQLException;

    @DBMakeRx
    @DBQuery("INSERT INTO spend_test (kind, value, date) VALUES (?, ?, ?)")
    void insertRecord(String kind, int value, Date date) throws SQLException;

}
