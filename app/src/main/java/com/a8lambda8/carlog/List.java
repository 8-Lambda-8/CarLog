package com.a8lambda8.carlog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class List extends AppCompatActivity {

    Spinner SP_driver,SP_refuel;

    TextView TV_ZB_Beg, TV_ZB_End;

    Time zb_beg, zb_end;


    DatabaseReference mDatabase;

    final String DBdateFormat = "%y-%m-%d_%H-%M-%S";
    final String dateFormat = "%d.%m.%y %H:%M:%S";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        mDatabase = FirebaseDatabase.getInstance().getReference();

        zb_beg=new Time(Time.getCurrentTimezone());
        zb_end=new Time(Time.getCurrentTimezone());
        zb_beg.set(0,0,17);
        zb_end.setToNow();

        init();

    }

    void init(){
        SP_driver = findViewById(R.id.sp_driver);
        SP_refuel = findViewById(R.id.sp_refuel);

        TV_ZB_Beg = findViewById(R.id.tv_zb_beg);
        TV_ZB_End = findViewById(R.id.tv_zb_end);

        TV_ZB_Beg.setText(zb_beg.format(dateFormat));
        TV_ZB_End.setText(zb_end.format(dateFormat));

        TV_ZB_Beg.setOnClickListener(TV_zb_OnClickListener);
        TV_ZB_End.setOnClickListener(TV_zb_OnClickListener);
    }

    View.OnClickListener TV_zb_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TextView tv = findViewById(v.getId());


            tv.setText("bla");


        }
    };

}
