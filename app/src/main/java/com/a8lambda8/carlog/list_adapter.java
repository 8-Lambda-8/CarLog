package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

/**
 * Created by Jakob Wasle on 15.09.2017.
 */

public class list_adapter extends ArrayAdapter<list_Item> {

    private List<list_Item> items;
    private Context context;
    boolean reverse;

    list_adapter(@NonNull Context context, int resource, List<list_Item> items,boolean reverse) {
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

        list_Item item, itemLst;
        int x = 0;
        if(reverse) {
            int revPos = getCount() - (position + 1);
            item = items.get(revPos);
            /*if(position!=0)
                x = -1;
            itemLst = items.get(revPos+x);*/
            //x = 1;
        }
        else {
            item = items.get(position);
            //itemLst = items.get();

            /*itemLst = items.get(getCount());
            if(position!=0)
                //x = -1;
                itemLst = items.get(getCount() - 1);*/
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
                dateTime.setText(item.gettStart().format("%d.%m.%y  %H:%M"));
            }

            if(!item.getRefuel()){//dur!=null&&item.gettEnd()!=null&&item.gettStart()!=null){

                Time duration =  new Time(Time.getCurrentTimezone());

                duration.set(item.gettEnd().toMillis(false) - item.gettStart().toMillis(false)-3600000);

                dur.setText(duration.format("Dauer:      %H:%M"));

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

            /*Boolean eq = item.getStart()==itemLst.getEnd();

            Log.d(TAG,String.format("KM: %d: start=%d last_end=%d   isEqual=%b",
                    position,
                    item.getStart(),
                    item.getEnd(),//items.get(position+x).getEnd(),
                    eq));

            Log.d(TAG,String.format("lst  : start=%d last_end=%d   isEqual=%b",
                    //position,
                    itemLst.getStart(),
                    itemLst.getEnd(),//items.get(position+x).getEnd(),
                    eq));

            *//*Log.d(TAG,String.format("KM: %d: start=%d last_end=%d   isEqual=%b    --last",
                    position+x,
                    items.get(position+x).getStart(),//item.getStart(),
                    items.get(position+x).getEnd(),
                    eq));*//*

            Log.d(TAG," \n");

            if(!eq){
                startKM.setTextColor(Color.RED);
            }

            if(item.getStartLoc()!=itemLst.getEndLoc()){
                start.setTextColor(Color.RED);
            }*/

        }

        return v;
    }


    @Override
    public list_Item getItem(int position){

        list_Item i;

        if(reverse)
            i = items.get(getCount() - (position+1));
        else
            i = items.get(position);
        return i;

    }
}
