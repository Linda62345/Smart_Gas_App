package com.example.smartgasapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class EventAdapter extends ArrayAdapter<AnnouncementList> {
    int mresource;
    private Context mContext;
    public EventAdapter (Context context, int resource, ArrayList<AnnouncementList> objects) {
        super(context,resource,objects);
        mresource = resource;
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position).getName();
        String type = getItem(position).getType();
        String des = getItem(position).getDescription();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);
        TextView Event_name = (TextView) convertView.findViewById(R.id.announcement_text);

        if(name!=null){
            Event_name.setText(name);
        }
        return  convertView;
    }
}
