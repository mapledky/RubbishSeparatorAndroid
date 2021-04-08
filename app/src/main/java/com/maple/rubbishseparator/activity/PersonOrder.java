package com.maple.rubbishseparator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.android.volley.Request;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.adapter.PersonOrderAdapter;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.UserOrder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class PersonOrder extends PermissionActivity implements PersonOrderAdapter.FinishListener {
    //data
    private String user_id;
    private String phoneNumber;
    private ArrayList<UserOrder> all_personorder;
    private ArrayList<UserOrder> present_personorder;


    private PersonOrderAdapter adapter;
    //view
    private SpotsDialog dialog;
    private SmartRefreshLayout refreshLayout;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_order);
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        phoneNumber = intent.getStringExtra("phoneNumber");


        init();
    }


    private void init() {
        refreshLayout = findViewById(R.id.person_order_refresh);
        listView = findViewById(R.id.person_order_listview);


        attachRefreshListener();
        decoratePane(refreshLayout);
        decorateLoading();

        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getData();
    }


    private void getData() {
        dialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.GETUSERORDER);
        params.put("Id", user_id);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            try {
                JSONArray jsonArray = new JSONArray(s);
                all_personorder = new ArrayList<>();
                present_personorder = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    UserOrder order = new UserOrder();
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
                    all_personorder.add(order);
                }

                //将五条数据放入present
                int number = 0;
                for (int i = all_personorder.size() - 1; i >= 0; i--) {
                    number++;
                    present_personorder.add(all_personorder.get(i));
                    if (number == 10) {
                        break;
                    }
                }
                adapter = new PersonOrderAdapter(PersonOrder.this, present_personorder);
                setListener();
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            refreshLayout.setEnableRefresh(true);
            refreshLayout.setEnableLoadMore(true);
            dialog.dismiss();
        }, volleyError -> {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            refreshLayout.setEnableRefresh(true);
            refreshLayout.setEnableLoadMore(true);
            dialog.dismiss();
        }, params);
    }

    private void setListener() {
        adapter.setFinishListener(this);
    }

    private void attachRefreshListener() {
        //添加刷新事件，该监听下拉刷新
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);

            getData();
        });

        //添加加载事件，该监听上拉加载
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            //上拉加载

            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);
            loadmore();
        });


    }

    @Override
    public void finishOrder(int position) {
        dialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.SETORDER);
        params.put("Id", user_id);
        params.put("order_id", present_personorder.get(position).Id);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("result").equals("1")) {
                    present_personorder.get(position).dismiss = "0";
                }
                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            dialog.dismiss();
        }, volleyError -> {
            dialog.dismiss();
        }, params);
    }

    private void loadmore() {
        int number = 0;
        for (int i = all_personorder.size() - present_personorder.size() - 1; i >= 0; i--) {
            number++;
            present_personorder.add(all_personorder.get(i));
            if (number == 10) {
                break;
            }
        }
        adapter.notifyDataSetChanged();
        refreshLayout.finishLoadMore();
        refreshLayout.finishRefresh();
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(true);
    }


    //修饰下啦加载
    private void decoratePane(SmartRefreshLayout pane) {
        pane.setHeaderHeight(40);
        pane.setFooterHeight(30);
        pane.setEnableLoadMore(true);//设置下拉刷新允许
        pane.setEnableOverScrollDrag(true);
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.pulltorefresh);
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.refreshing);
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.isloading);
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.releasetorefresh);
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.refreshfinish);
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.refreshfail);

    }

    //装饰加载条
    private void decorateLoading() {
        if (dialog == null) {
           dialog = new SpotsDialog(this);
           dialog.setCanceledOnTouchOutside(false);
        }
    }


}