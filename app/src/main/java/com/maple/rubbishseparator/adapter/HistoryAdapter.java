package com.maple.rubbishseparator.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.maple.rubbishseparator.R;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private List<String> list;
    private Context context;

    public HistoryAdapter(List<String> list_, Context context_) {
        this.list = list_;
        this.context = context_;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public interface ChooseHistoryListener{
        void chooseHistory(String history);
    }

    public ChooseHistoryListener historyListener;

    public void setHistoryListener(ChooseHistoryListener listener){
        this.historyListener = listener;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.historylist, null);
        }
        tv = convertView.findViewById(R.id.history_list_tv);
        tv.setText(list.get(position));

        convertView.setOnClickListener(v -> {
            historyListener.chooseHistory(list.get(position));
        });
        return convertView;
    }

}


