package com.a8lambda8.carlog;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jakob Wasle on 15.09.2017.
 */

public class list_adapter extends ArrayAdapter<list_Item> {

    private List<list_Item> items;
    private Context context;

    public list_adapter(@NonNull Context context, int resource, List<list_Item> items ) {
        super(context, resource, items);

        this.context = context;
        this.items = items;

    }

    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if(v==null){
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.list_item,null);
        }

        list_Item item = items.get(position);

        if(item!=null){

            TextView dateTime = v.findViewById(R.id.dateTime);
            TextView dur = v.findViewById(R.id.dur);
            TextView start = v.findViewById(R.id.start);
            TextView end = v.findViewById(R.id.end);
            TextView drain = v.findViewById(R.id.drain);
            TextView speed = v.findViewById(R.id.speed);
            TextView distance = v.findViewById(R.id.distance);

            TextView driver = v.findViewById(R.id.driver);
            ImageView IVrefuel = v.findViewById(R.id.refuel);
            ConstraintLayout CL = v.findViewById(R.id.layout);


            if(dateTime!=null&&item.gettStart()!=null){
                dateTime.setText(item.gettStart().format("%d.%m.%y  %H:%M"));
            }
            if(dur!=null&&item.gettEnd()!=null&&item.gettStart()!=null){

                Time duration =  new Time(Time.getCurrentTimezone());

                duration.set(item.gettEnd().toMillis(false) - item.gettStart().toMillis(false)-3600000);
                
                dur.setText(duration.format("Dauer:      %H:%M"));

                if(item.getRefuel()) {
                    dur.setText("Kosten: "+item.getPrice()+" €");
                }
            }
            if(start!=null){
                start.setText(item.getStartLoc());
            }
            if(end!=null){
                end.setText(item.getEndLoc());
            }
            if(drain!=null){
                drain.setText(item.getDrain());
            }
            if(speed!=null){
                speed.setText(item.getSpeed());
            }
            if(distance!=null){
                distance.setText((item.getEnd()-item.getStart())+" km");
            }

            if(driver!=null) {
                driver.setText("Fahrer: " + item.getDriverName());
            }

            if(IVrefuel!=null){
                if(item.getRefuel()) {
                    IVrefuel.setImageResource(R.mipmap.refuel);
                    CL.setBackgroundColor(Color.parseColor("#009000"));
                }else{


                    IVrefuel.setImageDrawable(null);
                    CL.setBackgroundColor(Color.WHITE);
                }
            }



        }

        return v;
    }
}
