package com.maple.rubbishseparator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.util.QRCode;

public class QRActivity extends PermissionActivity {

    private String user_id;

    private ImageView iv_sq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r);
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        init();
    }

    private void init() {
        iv_sq = findViewById(R.id.sqpage_iv);

        createQR();
    }

    private void createQR(){
        Bitmap bitmap = QRCode.createQRcodeImage(user_id,200,200);
        iv_sq.setImageBitmap(bitmap);
    }

}