package com.maple.rubbishseparator.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.adapter.ResultAdapter;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.RubbishUtil;
import com.wega.library.loadingDialog.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowResult extends PermissionActivity {
    private String search_target;

    private LoadingDialog dialog;

    //view
    private TextView tv_name;
    private ListView lv_content;
    private ImageView iv_empty;


    private ResultAdapter adapter;
    private ArrayList<RubbishUtil> rubbishUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);
        Intent intent = getIntent();
        search_target = intent.getStringExtra("search_target");
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        tv_name = findViewById(R.id.showresult_name);
        lv_content = findViewById(R.id.showresult_listview);
        iv_empty = findViewById(R.id.showresult_empty);


        tv_name.setText(search_target + getResources().getString(R.string.search_result));
        decorateLoading();
        getData();//向服务器获取信息
    }

    private void getData() {
        dialog.loading();
        Map<String, String> params = new HashMap<>();
        String url = HttpHelper.GETRUBBISHINFO + "?key=" + HttpHelper.TIANXIN_KEY + "&word=" + search_target;
        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                rubbishUtils = new ArrayList<>();
                if (jsonObject.getString("code").equals("200") && jsonObject.getString("msg").equals("success")) {
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("newslist"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject util = jsonArray.getJSONObject(i);
                        RubbishUtil rubbishUtil = new RubbishUtil();
                        rubbishUtil.name = util.getString("name");
                        rubbishUtil.type = util.getInt("type");
                        rubbishUtil.explain = util.getString("explain");
                        rubbishUtil.contain = util.getString("contain");
                        rubbishUtil.tip = util.getString("tip");
                        rubbishUtils.add(rubbishUtil);
                    }
                    adapter = new ResultAdapter(ShowResult.this,rubbishUtils);
                    lv_content.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    //数据返回为空
                    iv_empty.setVisibility(View.VISIBLE);
                }
                dialog.loadSuccess();
                dialog.dismiss();
            } catch (JSONException e) {
                dialog.loadFail();
                dialog.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            Log.i("search", error.toString());
            dialog.loadFail();
            dialog.dismiss();
        }, params);
    }


    //装饰加载条
    private void decorateLoading() {
        LoadingDialog.Builder builder = new LoadingDialog.Builder(this);
        builder.setLoading_text(getText(R.string.loading))
                .setSuccess_text(getText(R.string.success))
                .setFail_text(getText(R.string.fail));
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }
}