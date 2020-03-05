package com.a8lambda8.carlog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import static com.a8lambda8.carlog.FilterListActivity.zb_beg;
import static com.a8lambda8.carlog.FilterListActivity.zb_end;
import static com.a8lambda8.carlog.myUtils.DateFormat_dmyhm;
import static com.a8lambda8.carlog.myUtils.TAG;

/**
 * Created by jwasl on 17.04.2018.
 */
class DateTimePicker {
    private TextView TV_out;

    private GregorianCalendar startTime, newTime;
    private int THEME = R.style.Theme_AppCompat_Light_Dialog_Alert;
    private Context con;
    private Handler mResponseHandler;

    DateTimePicker(TextView tv, Context c, final Handler mResponseHandler) {
        TV_out = tv;
        Log.d(TAG, "DateTimePicker: "+tv.getText());

        startTime = (GregorianCalendar) GregorianCalendar.getInstance();
        try {
            startTime.setTime(Objects.requireNonNull(DateFormat_dmyhm.parse(tv.getText().toString())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        con = c;
        this.mResponseHandler = mResponseHandler;
    }


    void show(){

        newTime = (GregorianCalendar) GregorianCalendar.getInstance();

        DatePickerDialog DatePicker =  new DatePickerDialog(con, THEME, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.d("xx",year+" "+monthOfYear);
                newTime.set(year,monthOfYear,dayOfMonth);


                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(con,THEME, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        newTime.set(Calendar.MINUTE,selectedMinute);
                        newTime.set(Calendar.HOUR,selectedHour);

                        if (TV_out.getId()==R.id.tv_zb_beg)
                            zb_beg.setTime(newTime.getTimeInMillis());
                        else if (TV_out.getId()==R.id.tv_zb_end)
                            zb_end.setTime(newTime.getTimeInMillis());
                        TV_out.setText(DateFormat_dmyhm.format(newTime));

                        Message msg = new Message();
                        msg.arg1 = 0;
                        mResponseHandler.sendMessage(msg);

                    }
                }, startTime.get(Calendar.HOUR), startTime.get(Calendar.MINUTE),true);
                mTimePicker.setTitle(con.getString(R.string.selectTime));
                mTimePicker.show();

            }

        },startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH));
        DatePicker.setTitle(con.getString(R.string.selectDate));
        DatePicker.show();
    }

}
