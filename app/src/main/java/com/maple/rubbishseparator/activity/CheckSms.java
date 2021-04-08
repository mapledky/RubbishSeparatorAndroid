package com.maple.rubbishseparator.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.ViewControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class CheckSms extends PermissionActivity implements View.OnClickListener {

    private String phoneNumber = "";
    private String sms = null;
    private String sms_code = null;

    private EditText et_inputsms;
    private Button bt_check;

    private SpotsDialog loadingDialog;//加载


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_sms);
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        decorateLoading();
        init();
        sendSms();
    }


    private void init() {
        //view
        ImageView iv_back = findViewById(R.id.check_sms_back);
        et_inputsms = findViewById(R.id.check_sms_inputsms);
        bt_check = findViewById(R.id.check_sms_check);

        bt_check.setClickable(false);
        et_inputsms.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sms = et_inputsms.getText().toString();
                if (sms.length() == 4) {
                    bt_check.setClickable(true);
                    bt_check.setBackground(getResources().getDrawable(R.drawable.small_corner_button_blue));
                } else {
                    bt_check.setClickable(false);
                    bt_check.setBackground(getResources().getDrawable(R.drawable.small_corner_button));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        iv_back.setOnClickListener(this);
        bt_check.setOnClickListener(this);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.check_back:
                    finish();
                    break;
                case R.id.check_sms_check:
                    checkSms();
                    break;
                default:
                    break;

            }
        }
    }

    private void sendSms() {
        loadingDialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.REQUEST_SMS);
        params.put("phoneNumber", phoneNumber);
        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.SMS_MAIN, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String result = jsonObject.getString("result");
                if (result.equals("1")) {
                    //id有效,成功
                    Toast.makeText(CheckSms.this, getResources().getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                    sms_code = jsonObject.getString("sms_id");
                } else if (result.equals("2")) {
                    Toast.makeText(CheckSms.this, getResources().getString(R.string.send_frequet), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CheckSms.this, getResources().getString(R.string.fail), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            } catch (JSONException e) {
                loadingDialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            loadingDialog.dismiss();
        }, params);
    }


    //装饰加载条
    private void decorateLoading() {
        loadingDialog = new SpotsDialog(this);
        loadingDialog.setCanceledOnTouchOutside(false);
    }

    private void checkSms() {
        if (phoneNumber != null && sms != null && sms_code != null) {
            if (sms.length() == 4) {
                //检测二维码
                loadingDialog.show();
                Map<String, String> params = new HashMap<>();
                params.put("requestCode", ServerCode.CHECK_SMS);
                params.put("phoneNumber", phoneNumber);
                params.put("code", sms);
                params.put("sms_id", sms_code);
                VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.SMS_MAIN, response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String result = jsonObject.getString("result");
                        if (result.equals("1")) {
                            //id有效,成功
                            String user_id = jsonObject.getString("user_id");
                            loginSuccess(user_id);
                        } else if (result.equals("2")) {
                            Toast.makeText(CheckSms.this, getResources().getString(R.string.message_error), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CheckSms.this, getResources().getString(R.string.fail), Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    } catch (JSONException e) {
                        loadingDialog.dismiss();
                        e.printStackTrace();
                    }
                }, error -> {
                    loadingDialog.dismiss();
                }, params);
            }
        }
    }

    private void loginSuccess(String user_id) {
        //将数据存入库
        SharedPreferences sharedPreferences = getSharedPreferences("account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        editor.putString("user_id", user_id);
        editor.putString("phoneNumber", phoneNumber);
        editor.commit();

        //发送登录成功广播
        Intent intent = new Intent();
        intent.putExtra("user_id",user_id);
        intent.putExtra("phoneNumber",phoneNumber);
        intent.setAction("com.maple.loginSuccessReceiver");
        sendBroadcast(intent);


        finish();
    }
}