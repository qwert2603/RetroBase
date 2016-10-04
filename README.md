# RetroBase

RetroBase = Retrofit + DataBase. It turns queries to DataBase to java-interface!

[![](https://www.jitpack.io/v/qwert2603/RetroBase.svg)](https://www.jitpack.io/#qwert2603/RetroBase)

This project was inspired with [Retrofit](https://github.com/square/retrofit).
And like Retrofit it allows You turn queries to DataBase to java-interface.

Just use annotation @DBInterface for you interface and @DBQuery for method-query to DB.
Methods may have parameters.

Moreover, You can use @DBInterfaceRx and get results of query to DB as RxJava's Observables!!!
For that add annotation @DBMakeRx to method and declare model-type for query.
Class need to have open constructor that get {@link java.sql.ResultSet}.

Example:

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
	        compile 'com.github.qwert2603:RetroBase:1.0.3'
}
```
