package com.maple.rubbishseparator.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;


import com.baidu.aip.imageclassify.AipImageClassify;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.adapter.BaiduAdapter;
import com.maple.rubbishseparator.network.HttpHelper;

import com.maple.rubbishseparator.util.BaiduPicResult;
import com.wega.library.loadingDialog.LoadingDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class ShowBaiduResult extends PermissionActivity implements BaiduAdapter.ChooseBaiduResultListener{
    private String imageurl;

    private LoadingDialog dialog;
    private ListView lv_result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_baidu_result);

        Intent intent = getIntent();
        imageurl = intent.getStringExtra("imageurl");

        decorateLoading();
        init();
    }

    private void init() {
        //view
        ImageView imageview = findViewById(R.id.show_baidu_image);
        lv_result = findViewById(R.id.show_baidu_resultlist);

        Bitmap bitmap = BitmapFactory.decodeFile(imageurl);
        imageview.setImageBitmap(bitmap);
        uploadimageToBaidu();
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

    private void uploadimageToBaidu() {
        dialog.loading();
        AipImageClassify classify = new AipImageClassify(HttpHelper.BAIDU_APP_ID, HttpHelper.BAIDU_SECRET_ID, HttpHelper.BAIDU_SECRET_KEY);
        JSONObject res = classify.advancedGeneral(imageurl, new HashMap<>());
        dialog.loadSuccess();
        dialog.dismiss();
        ArrayList<BaiduPicResult> results = new ArrayList<>();
        try {
            if (!res.getString("result_num").equals("0")) {
                JSONArray jsonArray = res.getJSONArray("result");
                JSONObject jsonObject;
                for (int i = 0; i < jsonArray.length(); i++) {
                    BaiduPicResult result = new BaiduPicResult();
                    jsonObject = jsonArray.getJSONObject(i);
                    result.name = jsonObject.getString("keyword");
                    double match = jsonObject.getDouble("score");
                    result.match = String.valueOf(((int)match*100));
                    results.add(result);
                }
            }
            //data
            BaiduAdapter adapter = new BaiduAdapter(results, ShowBaiduResult.this);
            adapter.setListener(this);
            lv_result.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void chooseResult(BaiduPicResult result) {
        Intent intent = new Intent(this, ShowResult.class);
        intent.putExtra("search_target", result.name);
        startActivity(intent);
        finish();
    }
}