package com.a8lambda8.carlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import static com.a8lambda8.carlog.myUtils.DateFormat_dmhms;
import static com.a8lambda8.carlog.myUtils.TAG;

/**
 * Created by jwasl on 23.11.2017.

 */

class TIME_picker {

    private Button B_now;
    private TimePicker TP;
    private DatePicker DP;

    TIME_picker(Activity parent, final Date in, final Handler mResponseHandler, String title, final int HandlerIdentifier){

        final GregorianCalendar out = (GregorianCalendar) GregorianCalendar.getInstance();

        out.setTime(in);

        LayoutInflater inflater = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final AlertDialog.Builder alert = new AlertDialog.Builder(parent);

        alert.setTitle(title);
        View alertView = Objects.requireNonNull(inflater).inflate(R.layout.time_picker_layout,null);
        alert.setView(alertView);

        TabLayout tabLayout = alertView.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(tab.getPosition()==0){
                    B_now.setVisibility(View.VISIBLE);
                    TP.setVisibility(View.VISIBLE);
                    DP.setVisibility(View.GONE);
                }
                else if(tab.getPosition()==1)
                {
                    B_now.setVisibility(View.GONE);
                    TP.setVisibility(View.GONE);
                    DP.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        B_now = alertView.findViewById(R.id.b_now);
        B_now.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                out.setTime(new Date());
                TP.setHour(out.get(Calendar.HOUR));
                TP.setMinute(out.get(Calendar.MINUTE));

            }
        });

        TP = alertView.findViewById(R.id.timePicker);

        TP.setIs24HourView(true);
        TP.setHour(out.get(Calendar.HOUR_OF_DAY));
        TP.setMinute(out.get(Calendar.MINUTE));

        TP.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int selectedHour, int selectedMinute) {
                out.set(out.get(Calendar.YEAR), out.get(Calendar.MONTH), out.get(Calendar.DAY_OF_MONTH), selectedMinute,selectedHour,0);
                Log.d(TAG, "onTimeChanged: "+ String.format("changed to: %s", DateFormat_dmhms.format((out.getTime()))));
            }
        });

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Message msg = new Message();
                msg.arg1 = HandlerIdentifier;
                msg.obj = out;
                mResponseHandler.sendMessage(msg);
                //Log.i("xxx","OK: "+((Time)msg.obj).format("%H:%M"));

            }
        });

        DP = alertView.findViewById(R.id.datePicker);
        DP.updateDate(out.get(Calendar.YEAR),out.get(Calendar.MONTH),out.get(Calendar.DAY_OF_MONTH));
        DP.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                out.set(year,monthOfYear,dayOfMonth,out.get(Calendar.HOUR_OF_DAY),out.get(Calendar.MINUTE),out.get(Calendar.SECOND));
                Log.d(TAG, "onTimeChanged: "+ String.format("Start Zeit: %s", DateFormat_dmhms.format((out.getTime()))));
            }
        });

        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        alert.show();
    }

}
