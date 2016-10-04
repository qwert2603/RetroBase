package com.qwert2603.retrobase_example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.postgresql.Driver;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        TextView textView = (TextView) findViewById(R.id.text_view);

        DataManager dataManager = new DataManager();

        dataManager.getAllRecordsFromDataBase()
                .subscribe(records -> textView.setText(records.toString()));

    }
}
