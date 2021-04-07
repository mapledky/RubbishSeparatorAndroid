package com.maple.rubbishseparator.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.itheima.roundedimageview.RoundedImageView;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.activity.OrderDetail;
import com.maple.rubbishseparator.util.RubbishUtil;
import com.maple.rubbishseparator.util.UserOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderAdapter extends BaseAdapter {
    private ArrayList<UserOrder> orderlist;
    private Context context;

    public OrderAdapter(Context context, ArrayList<UserOrder> order) {
        this.context = context;
        this.orderlist = order;

    }

    @Override
    public int getCount() {
        return orderlist.size();
    }

    @Override
    public Object getItem(int position) {
        return orderlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.order_list, null);
        }
        ImageView cover = convertView.findViewById(R.id.order_list_image);
        TextView tv_title = convertView.findViewById(R.id.order_list_title);
        TextView tv_price = convertView.findViewById(R.id.order_list_price);
        TextView tv_data = convertView.findViewById(R.id.order_list_date);
        Button bt_detail = convertView.findViewById(R.id.order_list_detail);

        UserOrder order = orderlist.get(position);
        String[] images = order.images.split("\\$");
        Glide.with(context).load(images[0]).into(cover);
        tv_title.setText(order.title);
        tv_price.setText("¥" + order.price);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(Long.parseLong(order.time));

        tv_data.setText(simpleDateFormat.format(date));

        bt_detail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetail.class);
            intent.putExtra("Id", order.Id);
            context.startActivity(intent);
        });

        return convertView;
    }
}
