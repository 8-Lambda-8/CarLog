package com.a8lambda8.carlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Objects;

/**
 * Created by jwasl on 23.11.2017.

 */

class TIME_picker {

    TIME_picker(Activity parent, final Time in, final Handler mResponseHandler, String title, final int x){


        final Time out = new Time(Time.getCurrentTimezone());
        final Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        out.set(in.toMillis(false));

        LayoutInflater inflater = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final AlertDialog.Builder alert = new AlertDialog.Builder(parent);

        alert.setTitle(title);
        View alertView = Objects.requireNonNull(inflater).inflate(R.layout.time_picker_layout,null);
        alert.setView(alertView);


        final TimePicker TP = alertView.findViewById(R.id.timePicker);

        TP.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TP.setHour(in.hour);
            TP.setMinute(in.minute);
        }


        Button B_now = alertView.findViewById(R.id.b_now);
        B_now.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                out.setToNow();
                TP.setHour(out.hour);
                out.setToNow();
                TP.setMinute(out.minute);

            }
        });

        TP.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int selectedHour, int selectedMinute) {
                out.set(0,selectedMinute,selectedHour, now.monthDay, now.month, now.year);
            }
        });



        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Message msg = new Message();
                msg.arg1 = x;
                msg.obj = out;
                mResponseHandler.sendMessage(msg);
                Log.i("xxx","OK: "+((Time)msg.obj).format("%H:%M"));

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
