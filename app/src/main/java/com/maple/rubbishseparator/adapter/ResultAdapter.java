package com.maple.rubbishseparator.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itheima.roundedimageview.RoundedImageView;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.util.RubbishUtil;

import java.util.ArrayList;

public class ResultAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<RubbishUtil> rubbishlist;


    public ResultAdapter(Context context, ArrayList<RubbishUtil> list) {
        this.context = context;
        this.rubbishlist = list;
    }

    @Override
    public int getCount() {
        return rubbishlist.size();
    }

    @Override
    public Object getItem(int position) {
        return rubbishlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.rubbishlist, null);
        }
        RoundedImageView imageView = convertView.findViewById(R.id.canlist_icon);
        TextView tv_name = convertView.findViewById(R.id.canlist_name);
        TextView tv_tip = convertView.findViewById(R.id.canlist_tip);

        RubbishUtil util = rubbishlist.get(position);
        switch (util.type){
            case 0:
                imageView.setImageDrawable(context.getDrawable(R.drawable.recycle_icon_text));
                break;
            case 1:
                imageView.setImageDrawable(context.getDrawable(R.drawable.harmful_icon_text));
                break;
            case 2:
                imageView.setImageDrawable(context.getDrawable(R.drawable.kitchen_icon_text));
                break;
            case 3:
                imageView.setImageDrawable(context.getDrawable(R.drawable.other_icon_text));
                break;
            default:
                break;
        }
        tv_name.setText(util.name);
        tv_tip.setText(util.tip);
        return convertView;
    }
}
