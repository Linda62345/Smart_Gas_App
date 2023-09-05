package com.example.smartgasapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

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
        String type = getItem(position).getType().toUpperCase(Locale.ROOT);   //make all char for the type input in db as uppercase
        String des = getItem(position).getDescription();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);
        TextView Event_name = (TextView) convertView.findViewById(R.id.announcement_text);
        ImageView Event_pict = (ImageView) convertView.findViewById(R.id.announcement_icon);


        if(name!=null){
            Event_name.setText(name);
        }

        if (type != null) {
            // Set the image based on the type received
            if (type.equals("HOLIDAY")) {
                Event_pict.setImageResource(R.drawable.baseline_event_24);
            } else if (type.equals("PROMOTION")) {
                Event_pict.setImageResource(R.drawable.baseline_info_24);
            } else if (type.equals("EMERGENCY")) {
                Event_pict.setImageResource(R.drawable.baseline_event_busy_24);
            } else if (type.equals("COMPANY")) {
                Event_pict.setImageResource(R.drawable.logo);
            }
            // Add more conditions for other types if needed
        }
        return  convertView;
    }
}
