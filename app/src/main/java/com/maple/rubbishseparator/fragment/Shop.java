package com.maple.rubbishseparator.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.android.volley.Request;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.adapter.OrderAdapter;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.UserOrder;
import com.maple.rubbishseparator.util.ViewControl;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class Shop extends Fragment implements AMapLocationListener, View.OnClickListener {

    private View rootview;
    private SmartRefreshLayout refreshLayout;
    private ListView listView;
    private SpotsDialog dialog;
    private ImageView iv_add;

    //data
    private Context context;
    private ArrayList<UserOrder> allorder;
    private ArrayList<UserOrder> presentOrder;
    private OrderAdapter adapter;

    public Shop() {
        // Required empty public constructor
    }


    public void setParams(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootview == null) {
            rootview = inflater.inflate(R.layout.fragment_shop, container, false);
        }
        refreshLayout = rootview.findViewById(R.id.shop_page_refresh);
        listView = rootview.findViewById(R.id.shop_page_listview);
        iv_add = rootview.findViewById(R.id.shop_page_add);

        iv_add.setOnClickListener(this);
        allorder = new ArrayList<>();
        presentOrder = new ArrayList<>();

        init();
        return rootview;
    }


    private void init() {
        decoratePane(refreshLayout);
        attachRefreshListener();
        decorateLoading();

        //???????????????????????????
        initLocation();

    }

    String acuracy = "6";
    AMapLocationClient mlocationClient;
    public String latitude;
    public String longitude;


    //????????????
    private void initLocation() {
        presentOrder.clear();
        allorder.clear();
        dialog.show();
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);

        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(context);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            //??????????????????
            mlocationClient.setLocationListener(this);
            //??????????????????????????????
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(3000);
            //??????????????????
            mlocationClient.setLocationOption(mLocationOption);
        }

        mlocationClient.stopLocation();
        mlocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                if (latitude == null && longitude == null) {
                    latitude = String.valueOf(aMapLocation.getLatitude());
                    longitude = String.valueOf(aMapLocation.getLongitude());
                    getData(acuracy);
                } else {
                    latitude = String.valueOf(aMapLocation.getLatitude());
                    longitude = String.valueOf(aMapLocation.getLongitude());
                }

            }
        } else {
            dialog.dismiss();
            Toast.makeText(context, getString(R.string.fail), Toast.LENGTH_SHORT).show();
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            refreshLayout.setEnableRefresh(true);
            refreshLayout.setEnableLoadMore(true);
        }
    }


    private void getData(String acuracy) {
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.GET_ORDER_BY_LOCATION);
        params.put("latitude", latitude);
        params.put("longtitude", longitude);
        params.put("acuracy", acuracy);
        VollySimpleRequest.getInstance(context).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, response -> {
            try {
                JSONArray jsonArray = new JSONArray(response);
                //?????????
                Log.i("shop", jsonArray.toString());
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
                    allorder.add(order);
                }


                //?????????????????????present
                int number = 0;
                for (int i = allorder.size() - 1; i >= 0; i--) {
                    number++;
                    presentOrder.add(allorder.get(i));
                    if (number == 5) {
                        break;
                    }
                }

                adapter = new OrderAdapter(context, presentOrder);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                //????????????
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                refreshLayout.setEnableLoadMore(true);
                refreshLayout.setEnableRefresh(true);
                dialog.dismiss();
            } catch (JSONException e) {
                //????????????
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                refreshLayout.setEnableLoadMore(true);
                refreshLayout.setEnableRefresh(true);
                dialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            refreshLayout.finishRefresh();
            refreshLayout.finishLoadMore();
            refreshLayout.setEnableLoadMore(true);
            refreshLayout.setEnableRefresh(true);
            dialog.dismiss();
        }, params);
    }


    private void loadmore() {
        int number = 0;
        for (int i = allorder.size() - presentOrder.size() - 1; i >= 0; i--) {
            number++;
            presentOrder.add(allorder.get(i));
            if (number == 5) {
                break;
            }
        }
        adapter.notifyDataSetChanged();
        refreshLayout.finishLoadMore();
        refreshLayout.finishRefresh();
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(true);
    }

    //?????????????????????????????????

    private void attachRefreshListener() {

        //??????????????????????????????????????????
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            presentOrder.clear();
            allorder.clear();
            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);
            getData(acuracy);
        });

        //??????????????????????????????????????????
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            //????????????

            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);
            loadmore();
        });


    }

    //??????????????????
    private void decoratePane(SmartRefreshLayout pane) {
        pane.setHeaderHeight(40);
        pane.setFooterHeight(30);
        pane.setEnableLoadMore(true);//????????????????????????
        pane.setEnableOverScrollDrag(true);
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.pulltorefresh);
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.refreshing);
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.isloading);
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.releasetorefresh);
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.refreshfinish);
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.refreshfail);

    }

    //???????????????
    private void decorateLoading() {
        if (dialog == null) {
            dialog = new SpotsDialog(context);
            dialog.setCanceledOnTouchOutside(false);
        }
    }


    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.shop_page_add:
                    Intent intent = new Intent();
                    intent.setAction("com.maple.uploadOrderListener");
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    context.sendBroadcast(intent);
                    break;
                default:
                    break;
            }
        }
    }
}