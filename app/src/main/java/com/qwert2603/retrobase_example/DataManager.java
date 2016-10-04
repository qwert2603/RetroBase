package com.qwert2603.retrobase_example;

import com.qwert2603.retrobase.generated.SpendDBImpl;
import com.qwert2603.retrobase_rx.generated.SpendDBRx;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DataManager {

    private SpendDB mSpendDB = new SpendDBImpl();
    private SpendDBRx mSpendDBRx = new SpendDBRx(mSpendDB);

    public Observable<List<DataBaseRecord>> getAllRecordsFromDataBase() {
        return mSpendDBRx.getAllRecordsOrdered()
                .toList()
                .compose(applySchedulers());
    }

    public Observable<Object> insertRecord(DataBaseRecord dataBaseRecord) {
        return mSpendDBRx.insertRecord(dataBaseRecord.getKind(), dataBaseRecord.getValue(), dataBaseRecord.getDate())
                .compose(applySchedulers());
    }

    public Observable<Object> removeRecord(int id) {
        return mSpendDBRx.deleteRecord(id)
                .compose(applySchedulers());
    }

    @SuppressWarnings("all")    // redundant casting
    private final Observable.Transformer mTransformer = observable -> ((Observable) observable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings("unchecked")
    private <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) mTransformer;
    }

}
