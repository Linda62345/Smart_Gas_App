package com.example.smartgasapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smartgasapp.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FamilyMemberAdapterList extends ArrayAdapter<String> {
    int mresource;
    private Context mContext;
    FamilyInvitationCode familyInvitationCode =  new FamilyInvitationCode();
    int id;

    public FamilyMemberAdapterList (Context context, int resource, ArrayList<String> objects) {
        super(context,resource,objects);
        mresource = resource;
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mresource,parent,false);

        Button delete = (Button) convertView.findViewById(R.id.deleteButton);
        TextView TV_name = (TextView) convertView.findViewById(R.id.member_name);
        if(name!=null){
            TV_name.setText(name);
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = familyInvitationCode.family_Id.get(position);
                //使用pop up 視窗確認
                deleteMember(String.valueOf(id));
                //版面更新

            }
        });

        return  convertView;
    }
    public void deleteMember(String id){
        try {
            String URL = "http://10.0.2.2/SQL_Connect/Delete_FamilyMember.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Delete Family response",response);
                    if (response.equals("success")) {
                        Log.i("Delete Family Member", "Successfully store family member.");
                        Toast.makeText(mContext.getApplicationContext(), "成功刪除", Toast.LENGTH_SHORT).show();
                        //版面更新
                        //familyInvitationCode.updateDelete(familyInvitationCode.name,familyInvitationCode.family_Id, Integer.parseInt(id),mContext);
                        LoginActivity loginActivity = new LoginActivity();
                        String Customer_ID = String.valueOf(loginActivity.getCustomerID());
                        if(Customer_ID.equals(id)){
                            //整個刪掉
                            familyInvitationCode.family_Id.clear();
                            familyInvitationCode.name.clear();
                        }
                        else{
                            int index = familyInvitationCode.family_Id.indexOf(id);
                            if (index != -1) {
                                familyInvitationCode.family_Id.remove(index);
                                familyInvitationCode.name.remove(index);
                            }
                        }
                        Log.i("delete size", String.valueOf(familyInvitationCode.name.size()));
                        //如何在此更新版面
                        //here
                        
                    } else if (response.equals("failure")) {
                        Log.i("D Family Member failure", response);
                    }

                }}, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext.getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", id);
                    return data;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
            requestQueue.add(stringRequest);
        }
        catch (Exception e){
            Log.i("D Family Member Exception", e.toString());
        }
    }
}
