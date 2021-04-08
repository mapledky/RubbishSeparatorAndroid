package com.maple.rubbishseparator.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.android.volley.Request;

import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import dmax.dialog.SpotsDialog;

public class SplashActivity extends PermissionActivity {
    private String Id;//缓存中的id
    private String phoneNumber;//缓存中的电话号码

    private SpotsDialog loadingDialog;//加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();
    }


    //初始化
    private void init() {
        //初始化加载框
        decorateLoading();
        //获取缓存中的id信息
        getStorageAccount();
    }

    //获取缓存中的id以及phonenumber 信息
    private void getStorageAccount() {
        SharedPreferences sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE);
        String user_id = sharedPreferences.getString("user_id", "");
        String phone_number = sharedPreferences.getString("phoneNumber", "");

        if (user_id != null && phone_number != null) {
            if (!user_id.equals("") && !phone_number.equals("")) {
                Id = user_id;
                phoneNumber = phone_number;
                //进行id有效性判断
                judgeId();
            } else {
                jumpToMain(0);
            }
        } else {
            jumpToMain(0);
        }
    }


    //id有效性判断
    private void judgeId() {
        loadingDialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.GETID_EFFIT);
        params.put("Id", Id);
        params.put("phoneNumber", phoneNumber);
        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String result = jsonObject.getString("result");
                if (result.equals("1")) {
                    //id有效,成功
                    jumpToMain(1);
                } else {
                    //清空缓存数据
                    SharedPreferences sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    jumpToMain(0);
                }
                loadingDialog.dismiss();
            } catch (JSONException e) {
                jumpToMain(0);
                loadingDialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            loadingDialog.dismiss();
            jumpToMain(0);
        }, params);
    }

    //跳转到mainactivity
    private void jumpToMain(int state) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("state", String.valueOf(state));
                startActivity(intent);
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 3000);//延迟3秒后跳转到main页面
    }

    //装饰加载条
    private void decorateLoading() {
        loadingDialog = new SpotsDialog(this);
        loadingDialog.setCanceledOnTouchOutside(false);
    }
}