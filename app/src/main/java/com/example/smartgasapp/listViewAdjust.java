package com.example.smartgasapp;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.widget.ListView;

public class listViewAdjust extends ListView {
    public listViewAdjust(Context context){
        super(context);
    }
    public listViewAdjust(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public listViewAdjust(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
    }
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}