package com.maple.rubbishseparator.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.util.BaiduPicResult;

import java.util.ArrayList;

public class BaiduAdapter extends BaseAdapter {
    private ArrayList<BaiduPicResult> results;
    private Context context;

    public BaiduAdapter(ArrayList<BaiduPicResult> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface ChooseBaiduResultListener {
        void chooseResult(BaiduPicResult result);
    }
    public void setListener(ChooseBaiduResultListener listener){
        this.listener = listener;
    }
    ChooseBaiduResultListener listener;
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv_name;
        TextView tv_match;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.baidu_result, null);
        }
        tv_name = convertView.findViewById(R.id.show_baidu_result_list_name);
        tv_match = convertView.findViewById(R.id.show_baidu_result_list_match);

        tv_name.setText(results.get(position).name);
        tv_match.setText(results.get(position).match +"%");
        convertView.setOnClickListener(v -> listener.chooseResult(results.get(position)));
        return convertView;
    }
}
