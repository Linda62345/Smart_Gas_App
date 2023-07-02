package com.example.smartgasapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OrderListFinishAdapterList extends ArrayAdapter<OrderListFinishList> {
    int mresource;
    private Context mContext;
    public OrderListFinishAdapterList(Context context, int resource, ArrayList<OrderListFinishList> objects) {
        super(context, resource, objects);
        mresource = resource;
        mContext = context;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        String time = getItem(position).getTime();
        String condition = getItem(position).getCondition();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);

        TextView tvTime = (TextView) convertView.findViewById(R.id.Ordertime);
        TextView tvCondition = (TextView) convertView.findViewById(R.id.OrderCondition);
        if(tvTime!=null&&tvCondition!=null){
            tvTime.setText(time);
            tvCondition.setText(condition);
        }
        return  convertView;
    }
}
