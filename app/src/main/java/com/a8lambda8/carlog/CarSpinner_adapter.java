package com.a8lambda8.carlog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class CarSpinner_adapter extends ArrayAdapter<CarSpinnerItem> {

    private List<CarSpinnerItem> items;
    private Context context;
    private int resource;

    CarSpinner_adapter(@NonNull Context context, int resource, List<CarSpinnerItem> items) {
        super(context, resource, items);
        this.items = items;

        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        TextView textView = (TextView) View.inflate(context, resource, null);
        textView.setText(items.get(position).name);
        textView.setTextSize(25);
        return textView;

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = Objects.requireNonNull(vi).inflate(resource, parent, false);
        }
        ((TextView) convertView).setText(items.get(position).name);
        ((TextView) convertView).setTextSize(20);
        return convertView;

    }

    @Override
    public void notifyDataSetChanged() {
        if(items.size()==0){
            items.add(new CarSpinnerItem("x","Create New Car"));
        }
        super.notifyDataSetChanged();
    }
}
