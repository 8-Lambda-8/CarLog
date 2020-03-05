package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Date;
import java.util.List;

import static com.a8lambda8.carlog.myUtils.DateFormat_dmyhm;
import static com.a8lambda8.carlog.myUtils.TimeFormat_hm;

/**
 * Created by Jakob Wasle on 15.09.2017.
 */

public class list_adapter extends ArrayAdapter<trip_Item> {

    private List<trip_Item> items;
    private Context context;
    private boolean reverse;

    list_adapter(@NonNull Context context, int resource, List<trip_Item> items, boolean reverse) {
        super(context, resource, items);

        this.context = context;
        this.items = items;
        this.reverse = reverse;

    }

    void setReverse(boolean reverse) {
        this.reverse = reverse;
    }


    @SuppressLint("SetTextI18n")
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View v = convertView;
        if(v==null){
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.list_item,null);
        }

        trip_Item item;

        if(reverse) {
            int revPos = getCount() - (position + 1);
            item = items.get(revPos);
        }
        else {
            item = items.get(position);

        }


        if(item!=null){

            TextView dateTime = v.findViewById(R.id.dateTime);
            TextView dur = v.findViewById(R.id.dur);
            TextView start = v.findViewById(R.id.start);
            TextView end = v.findViewById(R.id.end);
            TextView startKM = v.findViewById(R.id.startKM);
            TextView endKM = v.findViewById(R.id.endKM);
            TextView drain = v.findViewById(R.id.drain);
            TextView speed = v.findViewById(R.id.speed);
            TextView distance = v.findViewById(R.id.distance);

            TextView driver = v.findViewById(R.id.driver);
            ImageView IVrefuel = v.findViewById(R.id.refuel);
            ConstraintLayout CL = v.findViewById(R.id.layout);


            if(dateTime!=null&&item.gettStart()!=null){
                dateTime.setText(DateFormat_dmyhm.format(item.gettStart()));
            }

            if(!item.getRefuel()){

                Date duration =  new Date();

                duration.setTime(item.gettEnd().getTime()- item.gettStart().getTime()-3600000);

                dur.setText("Dauer:      "+ TimeFormat_hm.format(duration));

            }

            if(item.getRefuel()) {
                dur.setText("Kosten: "+item.getPrice()+" €");
            }

            if(start!=null){
                start.setText(item.getStartLoc());
            }
            if(end!=null){
                end.setText(item.getEndLoc());
            }
            if(startKM!=null){
                startKM.setText(item.getStart()+context.getString(R.string.km));
            }
            if(endKM!=null){
                endKM.setText(item.getEnd()+context.getString(R.string.km));
            }
            if(drain!=null){
                drain.setText(item.getDrain());
            }
            if(speed!=null){
                speed.setText(item.getSpeed());
            }
            if(distance!=null){
                distance.setText((item.getEnd()-item.getStart())+context.getString(R.string.km));
            }

            if(driver!=null) {
                driver.setText(context.getString(R.string.fahrer)+" "+ item.getDriverName());
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


    @Override
    public trip_Item getItem(int position){

        trip_Item i;

        if(reverse)
            i = items.get(getCount() - (position+1));
        else
            i = items.get(position);
        return i;

    }
}
