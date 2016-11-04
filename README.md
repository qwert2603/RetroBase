# RetroBase

RetroBase = Retrofit + DataBase. It turns queries to DataBase to java-interface!

[![](https://www.jitpack.io/v/qwert2603/RetroBase.svg)](https://www.jitpack.io/#qwert2603/RetroBase)

This project was inspired with [Retrofit](https://github.com/square/retrofit).
And like Retrofit it allows You turn queries to DataBase into java-interface.

Just use annotation *@DBInterface* for you interface and *@DBQuery* for method-query to DB.
Methods may have parameters.

Moreover, You can use *@DBInterfaceRx* and get results of query to DB as RxJava's Observables!!!
For that add annotation *@DBMakeRx* to method and declare model-type for query.
Class need to have open constructor that get *java.sql.ResultSet*.

## Example:

```
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
    @DBQuery("SELECT * FROM spend_test ORDER BY date, id")
    ResultSet getAllRecordsOrdered() throws SQLException;

    @DBMakeRx
    @DBQuery("DELETE FROM spend_test WHERE id = ?")
    void deleteRecord(int id) throws SQLException;

    @DBMakeRx
    @DBQuery("INSERT INTO spend_test (kind, value, date) VALUES (?, ?, ?)")
    void insertRecord(String kind, int value, Date date) throws SQLException;
}
```

##[Full working example is here](https://github.com/qwert2603/RetroBaseExample)

## Returning ID

If You need to execute INSERT query and get id of created record or get count of records those were updated with UPDATE query or get id of records those were deleted with DELETE query, You can add statement "returning id" at the end of Your SQL-query and get sought-for ids.

```
@DBMakeRx(modelClassName = "com.qwert2603.spenddemo.model.Id")
@DBQuery("UPDATE spend_test SET kind=?, value=?, date=? WHERE id=? returning id")
ResultSet updateRecord(String kind, int value, Date date, int id) throws SQLException;
```

As You can see, You can use *@DBMakeRx* annotation and get Observable<*id*>. To do that You should create model-type that get Id from *ResultSet* and use this class as parameter *modelClassName* in annotation *@DBMakeRx*. Model-type may be like this:

```
public class Id {
    private int mId;

    public Id(ResultSet resultSet) throws SQLException {
        mId = resultSet.getInt(1);
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }
}
```

##Release 1.1

- Now check <code>java.sql.Connection#isValid(0)</code>

- Rx classes now generated into package com.qwert2603.retrobase.rx.generated; (before was package com.qwert2603.retrobase_rx.generated;)

##Release 1.2

- Now using rxjava 2.x
- ...methods generated with *@DBMakeRx* return *io.reactivex.Completable* if annotated method returns *void* (INSERT, UPDATE, DELETE sql-queries)
- ...methods generated with *@DBMakeRx* return *io.reactivex.Observable* if annotated method returns *java.sql.ResultSet* (SELECT sql-query). *io.reactivex.Observable* is created with *Observable#generate*.

##Download

```
allprojects {
		repositories {
			...
			maven { url "https://www.jitpack.io" }
		}
	}
```

```
dependencies {
	        compile 'com.github.qwert2603:RetroBase:1.1'
}
```
