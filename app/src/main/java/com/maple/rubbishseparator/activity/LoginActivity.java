package com.maple.rubbishseparator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.util.ViewControl;

public class LoginActivity extends PermissionActivity implements View.OnClickListener {

    private ImageView iv_back;
    private EditText et_input;
    private Button bt_next;


    private String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        registerListener();
        init();
    }

    private void init() {
        iv_back = findViewById(R.id.login_back);
        et_input = findViewById(R.id.login_inputphone);
        bt_next = findViewById(R.id.login_next);

        bt_next.setClickable(false);
        et_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneNumber = et_input.getText().toString();
                if (phoneNumber.length() == 11) {
                    bt_next.setClickable(true);
                    bt_next.setBackground(getResources().getDrawable(R.drawable.small_corner_button_blue));
                } else {
                    bt_next.setClickable(false);
                    bt_next.setBackground(getResources().getDrawable(R.drawable.small_corner_button));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        iv_back.setOnClickListener(this);
        bt_next.setOnClickListener(this);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.login_back:
                    finish();
                    break;
                case R.id.login_next:
                    next();
                    break;
                default:
                    break;
            }
        }
    }

    private void next() {
        if (phoneNumber.length() == 11) {
            Intent intent = new Intent(this, CheckActivity.class);
            intent.putExtra("phoneNumber", phoneNumber);
            startActivity(intent);
        }
    }


    private class LoginListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }

    private void registerListener(){
        LoginListener loginListener = new LoginListener();
        IntentFilter intentFilter = new IntentFilter("com.maple.loginSuccessReceiver");
        registerReceiver(loginListener,intentFilter);

    }
}