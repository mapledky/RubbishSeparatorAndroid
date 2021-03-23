package com.maple.rubbishseparator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.maple.rubbishseparator.R;

public class MainActivity extends PermissionActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}