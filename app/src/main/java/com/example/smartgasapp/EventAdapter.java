package com.example.smartgasapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class EventAdapter extends ArrayAdapter<String> {
    int mresource;
    private Context mContext;
    public EventAdapter (Context context, int resource, ArrayList<String> objects) {
        super(context,resource,objects);
        mresource = resource;
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);
        TextView TV_name = (TextView) convertView.findViewById(R.id.announcement_text);
        if(name!=null){
            TV_name.setText(name);
        }
        return  convertView;
    }
}
