package com.a8lambda8.carlog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.a8lambda8.carlog.myUtils.TAG;

/**
 * Created by jwasl on 20.11.2017.
 *
 */

class KMpicker {

    private int KM;

    private TextView tv[] = new TextView[6];


    KMpicker(Activity parent, int km, final Handler mResponseHandler, String title, final int x) {
        KM = km;

        LayoutInflater inflater = (LayoutInflater) parent.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        final AlertDialog.Builder alert = new AlertDialog.Builder(parent);

        alert.setTitle(title);
        View alertView = inflater.inflate(R.layout.km_picker,null);
        alert.setView(alertView);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Message msg = new Message();
                msg.arg1 = x;
                msg.arg2 = KM;
                mResponseHandler.sendMessage(msg);

            }
        });

        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        int[] tv_ids = {R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6};
        int[] bu_ids = {R.id.up1, R.id.up2, R.id.up3, R.id.up4, R.id.up5, R.id.up6};
        int[] bd_ids = {R.id.dwn1, R.id.dwn2, R.id.dwn3, R.id.dwn4, R.id.dwn5, R.id.dwn6};

        Button[] b_u = new Button[6];
        Button[] b_d = new Button[6];

        for(int i = 0;i<6;i++){

            tv[i] = alertView.findViewById(tv_ids[i]);
            b_u[i] = alertView.findViewById(bu_ids[i]);
            b_d[i] = alertView.findViewById(bd_ids[i]);

            final int finalI = i;
            b_u[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    add(finalI);
                }
            });
            b_d[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sub(finalI);
                }
            });

        }

        updateTV();
        alert.show();

    }

    private void updateTV(){
        int km = KM;
        Log.d(TAG,"km:"+km);
        for(int i = 0;i<6;i++) {
            tv[i].setText(String.valueOf(km%10));
            km /= 10;
        }
    }

    private void add(int pot){
        Log.d(TAG,"add_"+pot+" | +"+ Math.pow(10,pot));
        KM += Math.pow(10,pot);
        updateTV();
    }

    private void sub(int pot){
        Log.d(TAG,"sub_"+pot+" | -"+Math.pow(10,pot));
        KM -= Math.pow(10,pot);
        updateTV();
    }

}
