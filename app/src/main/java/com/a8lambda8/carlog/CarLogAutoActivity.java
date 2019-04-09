package com.a8lambda8.carlog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.apps.auto.sdk.CarActivity;

public class CarLogAutoActivity extends CarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_log_auto);
        getCarUiController().getStatusBarController().setTitle("Hello AA");

    }

}
