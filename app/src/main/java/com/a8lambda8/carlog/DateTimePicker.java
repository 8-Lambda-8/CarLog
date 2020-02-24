package com.a8lambda8.carlog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import static com.a8lambda8.carlog.FilterListActivity.zb_beg;
import static com.a8lambda8.carlog.FilterListActivity.zb_end;
import static com.a8lambda8.carlog.myUtils.TAG;
import static com.a8lambda8.carlog.myUtils.TimeParser;

/**
 * Created by jwasl on 17.04.2018.
 */
class DateTimePicker {
    private TextView TV_out;
    private final String dateFormat = "%d.%m.%y  %H:%M";
    private Time startTime;
    private Time newTime;
    private int THEME = R.style.Theme_AppCompat_Light_Dialog_Alert;
    private Context con;
    private Handler mResponseHandler;

    DateTimePicker(TextView tv, Context c, final Handler mResponseHandler) {
        TV_out = tv;
        Log.d(TAG, "DateTimePicker: "+tv.getText());
        startTime = TimeParser(tv.getText().toString(),dateFormat);
        con = c;
        this.mResponseHandler = mResponseHandler;
    }


    void show(){

        newTime = new Time(Time.getCurrentTimezone());

        DatePickerDialog DatePicker =  new DatePickerDialog(con, THEME, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.d("xx",year+" "+monthOfYear);
                newTime.set(dayOfMonth,monthOfYear,year);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(con,THEME, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        newTime.set(0,selectedMinute,selectedHour,newTime.monthDay,newTime.month,newTime.year);
                        if (TV_out.getId()==R.id.tv_zb_beg)
                            zb_beg.set(newTime.toMillis(false));
                        else if (TV_out.getId()==R.id.tv_zb_end)
                            zb_end.set(newTime.toMillis(false));
                        TV_out.setText(newTime.format(dateFormat));

                        Message msg = new Message();
                        msg.arg1 = 0;
                        mResponseHandler.sendMessage(msg);

                    }
                }, startTime.hour, startTime.minute,true);
                mTimePicker.setTitle(con.getString(R.string.selectTime));
                mTimePicker.show();

            }

        },startTime.year, startTime.month-1, startTime.monthDay);
        DatePicker.setTitle(con.getString(R.string.selectDate));
        DatePicker.show();
    }

}
