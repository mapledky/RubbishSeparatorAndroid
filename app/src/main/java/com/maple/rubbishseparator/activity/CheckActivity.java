package com.maple.rubbishseparator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.luozm.captcha.Captcha;
import com.maple.rubbishseparator.R;

public class CheckActivity extends PermissionActivity {

    private String phoneNumber = "";

    private Captcha captcha;
    private ImageView iv_back;

    //view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        registerListener();
        init();

    }

    private void init() {
        captcha = findViewById(R.id.check_capcha);
        iv_back = findViewById(R.id.check_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        captcha.setCaptchaListener(new Captcha.CaptchaListener() {
            @Override
            public String onAccess(long time) {
                Toast.makeText(CheckActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CheckActivity.this, CheckSms.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
                return "验证通过,耗时" + time + "毫秒";
            }

            @Override
            public String onFailed(int failedCount) {
                Toast.makeText(CheckActivity.this, "验证失败", Toast.LENGTH_SHORT).show();
                return "验证失败,已失败" + failedCount + "次";
            }

            @Override
            public String onMaxFailed() {
                Toast.makeText(CheckActivity.this, "验证超过次数", Toast.LENGTH_SHORT).show();
                return "验证失败,帐号已封锁";
            }
        });
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