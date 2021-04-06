package com.maple.rubbishseparator.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.maple.rubbishseparator.R;

public class CustomDialog_1 extends Dialog implements View.OnClickListener{
    public Context context;

    private final String text1;
    private final String text2;

    public  OnTextViewSelectedListener listener;

    public CustomDialog_1(Context context, String text1, String text2) {
        super(context,R.style.CustomDialog_1);
        this.context=context;
        this.text1 = text1;
        this.text2 = text2;
    }
    /*
    定义回调接口
     */

    public interface OnTextViewSelectedListener{
        public void onTextViewSelected(int num);
    }
    public void setOnTextViewSelectedListener(OnTextViewSelectedListener listener) {
        this.listener = listener;
    }
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setCanceledOnTouchOutside(true);
        initView();
    }
    private void initView() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.choosepicturefrom,null);
;
        TextView tv_chooseFromPhotos = (TextView) inflate.findViewById(R.id.getPicFromPhotos);
        TextView tv_takePhotos = (TextView) inflate.findViewById((R.id.getPicNow));


        tv_takePhotos.setText(text1);
        tv_chooseFromPhotos.setText(text2);


        tv_chooseFromPhotos.setOnClickListener(this);
        tv_takePhotos.setOnClickListener(this);

        setContentView(inflate);
    }
    /*
        点击事件
         */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int num = -1;
        switch(view.getId()){
            case R.id.getPicFromPhotos:
                num=1;
                break;
            case R.id.getPicNow:
                num=2;
                break;
        }
        if(num != -1) {
            listener.onTextViewSelected(num);
        }
        dismiss();
    }
}
