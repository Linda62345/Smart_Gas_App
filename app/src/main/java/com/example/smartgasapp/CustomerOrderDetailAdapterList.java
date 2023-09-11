package com.example.smartgasapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

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
        String exchange_String = getItem(position).getExchange();

        CustomerOrderDetail orderDetail = new CustomerOrderDetail(quantity,type,weight);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);

        TextView tvQuantity = (TextView) convertView.findViewById(R.id.quantity);
        TextView tvType = (TextView) convertView.findViewById(R.id.type);
        TextView tvWeight = (TextView) convertView.findViewById(R.id.weight);
        TextView Exchange = (TextView) convertView.findViewById(R.id.exchange);

        if(tvQuantity!=null&&tvType!=null&&tvWeight!=null){
            tvQuantity.setText(quantity);
            if(type.toLowerCase(Locale.ROOT).equals("composite")){
                tvType.setText("複合式鋼瓶");
            }
            else if(type.toLowerCase(Locale.ROOT).equals("tradition")){
                tvType.setText("傳統鋼瓶");
            }
            else if(type.equals("類別")){
                tvType.setText("類別");
            }
            else{
                tvType.setText(type);
            }

            tvWeight.setText(weight);
        }
        if(exchange_String!=null && exchange_String.equals("1") && Exchange!=null){
            Exchange.setText("已使用兌換");
        }
        else{
            Exchange.setVisibility(View.GONE);
        }

        return  convertView;
    }
}
