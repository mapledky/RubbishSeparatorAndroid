package com.maple.rubbishseparator.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maple.rubbishseparator.R;

public class Shop extends Fragment {
    private View rootview;

    public Shop() {
        // Required empty public constructor
    }

    private Context context;
    public void setParams(Context context){
        this.context = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootview == null) {
            rootview = inflater.inflate(R.layout.fragment_shop, container, false);
        }
        return rootview;
    }
}