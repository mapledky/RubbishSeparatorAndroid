package com.maple.rubbishseparator.activity;

import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.itheima.roundedimageview.RoundedImageView;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.adapter.GuidePagerAdapter;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.CanUtil;
import com.maple.rubbishseparator.util.DepthPagerTransformer;
import com.maple.rubbishseparator.util.UserOrder;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class OrderDetail extends PermissionActivity {
    //data
    UserOrder order;
    private String order_id;

    //view
    private SpotsDialog dialog;
    private ViewPager viewPager;

    private TextView tv_price;
    private TextView tv_title;
    private TextView tv_des;
    private TextView tv_time;
    private TextView tv_phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        Intent intent = getIntent();
        order_id = intent.getStringExtra("Id");

        init();
    }


    private void init() {
        viewPager = findViewById(R.id.order_detail_viewpager);
        tv_price = findViewById(R.id.order_detail_price);
        tv_title = findViewById(R.id.order_detail_name);
        tv_des = findViewById(R.id.order_detail_des);
        tv_phone = findViewById(R.id.order_detail_phone);

        tv_time = findViewById(R.id.order_detail_time);

        decorateLoading();
        getInfo();
    }

    private void getInfo() {
        dialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.GETORDERBYID);
        params.put("Id", order_id);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            dialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                order = new UserOrder();
                order.Id = jsonObject.getString("Id");
                order.user_id = jsonObject.getString("user_id");
                order.price = jsonObject.getString("price");
                order.latitude = jsonObject.getString("latitude");
                order.longitude = jsonObject.getString("longitude");
                order.description = jsonObject.getString("description");
                order.title = jsonObject.getString("title");
                order.time = jsonObject.getString("time");
                order.images = jsonObject.getString("images");
                order.phoneNumber = jsonObject.getString("phoneNumber");
                order.dismiss = jsonObject.getString("dismiss");

                inputData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, volleyError -> {
            dialog.dismiss();
        }, params);
    }


    @SuppressLint("SetTextI18n")
    private void inputData() {
        ArrayList<View> image_view = new ArrayList<>();
        GuidePagerAdapter adapter_guide = new GuidePagerAdapter(image_view);
        viewPager.setAdapter(adapter_guide);
        viewPager.setPageTransformer(true, new DepthPagerTransformer());
        String[] state_array = order.images.split("\\$");
        for (String s : state_array) {
            RoundedImageView roundedImageView = new RoundedImageView(this);
            roundedImageView.setCornerRadius(20);
            image_view.add(roundedImageView);
            Glide.with(this).load(s).into(roundedImageView);
        }

        GuidePagerAdapter adapter = new GuidePagerAdapter(image_view);
        viewPager.setAdapter(adapter);

        tv_price.setText("¥" + order.price);
        tv_title.setText(order.title);
        tv_des.setText(order.description);
        tv_phone.setText(getString(R.string.phonenumber) + order.phoneNumber);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(Long.parseLong(order.time));
        tv_time.setText(simpleDateFormat.format(date));
    }

    //装饰加载条
    private void decorateLoading() {
        if (dialog == null) {
            dialog = new SpotsDialog(this);
            dialog.setCanceledOnTouchOutside(false);
        }
    }

}