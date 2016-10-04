package com.qwert2603.retrobase_example;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseRecord {
    private int mId;
    private String mKind;
    private int mValue;
    private java.sql.Date mDate;

    public DataBaseRecord(int id, String kind, int value, java.sql.Date date) {
        mId = id;
        mKind = kind;
        mValue = value;
        mDate = date;
    }

    public DataBaseRecord(ResultSet resultSet) throws SQLException {
        mId = resultSet.getInt("id");
        mKind = resultSet.getString("kind");
        mValue = resultSet.getInt("value");
        mDate = resultSet.getDate("date");
    }

    public int getId() {
        return mId;
    }

    public String getKind() {
        return mKind;
    }

    public int getValue() {
        return mValue;
    }

    public Date getDate() {
        return mDate;
    }

    @Override
    public String toString() {
        return "DataBaseRecord{" +
                "mId=" + mId +
                ", mKind='" + mKind + '\'' +
                ", mValue=" + mValue +
                ", mDate=" + mDate +
                '}';
    }
}
