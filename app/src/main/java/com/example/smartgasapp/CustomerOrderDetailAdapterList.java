package com.example.smartgasapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CustomerOrderDetailAdapterList extends ArrayAdapter<CustomerOrderDetail> {
    int mresource;
    private Context mContext;
    public CustomerOrderDetailAdapterList(Context context, int resource, ArrayList<CustomerOrderDetail> objects) {
        super(context, resource, objects);
        mresource = resource;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String quantity = getItem(position).getQuantity();
        String type = getItem(position).getType();
        String weight = getItem(position).getWeight();

        CustomerOrderDetail orderDetail = new CustomerOrderDetail(quantity,type,weight);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);

        TextView tvQuantity = (TextView) convertView.findViewById(R.id.quantity);
        TextView tvType = (TextView) convertView.findViewById(R.id.type);
        TextView tvWeight = (TextView) convertView.findViewById(R.id.weight);

        if(tvQuantity!=null&&tvType!=null&&tvWeight!=null){
            tvQuantity.setText("瓦斯桶數量: "+quantity);
            tvType.setText("瓦斯桶類別: "+type);
            tvWeight.setText("瓦斯桶重量: "+weight);
        }

        return  convertView;
    }
}
