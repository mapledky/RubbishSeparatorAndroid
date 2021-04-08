package com.maple.rubbishseparator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ChangeName extends PermissionActivity {
    //data
    private String user_id;
    private String phoneNumber;

    //view
    private EditText et_name;
    private Button bt_upload;
    private SpotsDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        phoneNumber = intent.getStringExtra("phoneNumber");

        decorateLoading();
        init();
    }


    private void init() {
        et_name = findViewById(R.id.change_name_inpur);
        bt_upload = findViewById(R.id.change_name_upload);


        bt_upload.setOnClickListener(v -> {
            String name = et_name.getText().toString();
            if (!name.equals("")) {
                uploadname(name);
            } else {
                Toast.makeText(ChangeName.this, getString(R.string.changename), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void uploadname(String name) {
        dialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.CHANGE_NAME);
        params.put("phoneNumber", phoneNumber);
        params.put("Id", user_id);
        params.put("name", name);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("result").equals("1")) {
                    Toast.makeText(ChangeName.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }, volleyError -> {
            dialog.dismiss();
        }, params);
    }

    //装饰加载条
    private void decorateLoading() {
        if (dialog == null) {
            dialog = new SpotsDialog(this);
            dialog.setCanceledOnTouchOutside(false);
        }
    }
}