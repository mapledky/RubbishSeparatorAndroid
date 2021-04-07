package com.maple.rubbishseparator.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.itheima.roundedimageview.RoundedImageView;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.activity.ChangeHead;
import com.maple.rubbishseparator.activity.MainActivity;
import com.maple.rubbishseparator.activity.MapActivity;
import com.maple.rubbishseparator.activity.QRActivity;
import com.maple.rubbishseparator.activity.SearchActivity;
import com.maple.rubbishseparator.adapter.GuidePagerAdapter;
import com.maple.rubbishseparator.dialog.TextComfirmDialog;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.DepthPagerTransformer;
import com.maple.rubbishseparator.util.QRCode;
import com.maple.rubbishseparator.util.ViewControl;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.wega.library.loadingDialog.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SeparateGuide extends Fragment implements View.OnClickListener {
    private View rootview;

    //data
    private Context context;
    private ArrayList<View> topimage_array;
    private String user_id;
    private String phoneNumber;
    private String name;
    private String headstate = "0";


    //view
    private RoundedImageView iv_head;
    private TextView tv_search;
    private ImageView iv_choice;
    private SmartRefreshLayout refreshLayout;
    private ViewPager viewPager;
    private LinearLayout layout_getqrcode;//获取二维码

    private ImageView iv_qrcode;
    private ImageView iv_record;
    private ImageView iv_takepic;
    private ImageView iv_photos;

    private LoadingDialog dialog;

    //adapter
    private GuidePagerAdapter adapter_guide;


    public SeparateGuide() {
        // Required empty public constructor
    }

    public void setParams(Context context, String user_id, String phoneNumber) {
        this.context = context;
        this.user_id = user_id;
        this.phoneNumber = phoneNumber;
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
            rootview = inflater.inflate(R.layout.fragment_separate_guide, container, false);
            iv_head = rootview.findViewById(R.id.guide_head);
            tv_search = rootview.findViewById(R.id.guide_serach);
            iv_choice = rootview.findViewById(R.id.guide_choice);
            refreshLayout = rootview.findViewById(R.id.guide_refreshlayout);
            viewPager = rootview.findViewById(R.id.guide_viewpager);
            layout_getqrcode = rootview.findViewById(R.id.guide_getQrCode);
            iv_qrcode = rootview.findViewById(R.id.guide_qrcode_pic);
            iv_record = rootview.findViewById(R.id.guide_record);
            iv_takepic = rootview.findViewById(R.id.guide_takepic);
            iv_photos = rootview.findViewById(R.id.guide_choosepic);


            iv_record.setOnClickListener(this);
            iv_takepic.setOnClickListener(this);
            iv_photos.setOnClickListener(this);

            topimage_array = new ArrayList<>();
            adapter_guide = new GuidePagerAdapter(topimage_array);
            viewPager.setAdapter(adapter_guide);
            viewPager.setPageTransformer(true, new DepthPagerTransformer());
        }
        init();
        return rootview;
    }


    private void init() {
        iv_head.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        iv_choice.setOnClickListener(this);
        layout_getqrcode.setOnClickListener(this);

        decoratePane(refreshLayout);
        attachRefreshListener();
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(false);


        decorateLoading();//初始化加载
        getTopImage();//获取头图片
        createQRCode();//创建用户二维码
        getUserInfo();//获取用户信息

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.guide_head:
                    toPerson();
                    break;
                case R.id.guide_serach:
                    searchRubbish();
                    break;
                case R.id.guide_choice:
                    guidechoice();
                    break;
                case R.id.guide_getQrCode:
                    getQrCode();
                    break;

                case R.id.guide_record:
                    recordVoice();
                    break;
                case R.id.guide_takepic:
                    Intent intent_takepic = new Intent();
                    intent_takepic.setAction("com.maple.detectPhoto");
                    intent_takepic.putExtra("type", "0");
                    context.sendBroadcast(intent_takepic);
                    break;
                case R.id.guide_choosepic:
                    Intent intent_choosepic = new Intent();
                    intent_choosepic.setAction("com.maple.detectPhoto");
                    intent_choosepic.putExtra("type", "1");
                    context.sendBroadcast(intent_choosepic);
                    break;

                default:
                    break;
            }
        }
    }


    //获取录音
    private void recordVoice() {
        Intent intent = new Intent();
        intent.setAction("com.maple.RecordDialog");
        context.sendBroadcast(intent);
    }

    //获取用户二维码
    private void getQrCode() {
        if (user_id != null) {
            Intent intent = new Intent(context, QRActivity.class);
            intent.putExtra("user_id", user_id);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, getString(R.string.login_account), Toast.LENGTH_SHORT).show();
        }
    }

    //创建用户二维码
    private void createQRCode() {
        Bitmap bitmap;
        if (user_id == null) {
            //当前用户为登陆
            bitmap = QRCode.createQRcodeImage("1", 200, 200);
        } else {
            bitmap = QRCode.createQRcodeImage(user_id, 200, 200);
        }
        iv_qrcode.setImageBitmap(bitmap);
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

    //获取用户信息
    public void getUserInfo() {
        if (user_id != null && phoneNumber != null) {
            dialog.loading();
            Map<String, String> params = new HashMap<>();
            params.put("requestCode", ServerCode.GETID_EFFIT);
            params.put("Id", user_id);
            params.put("phoneNumber", phoneNumber);
            VollySimpleRequest.getInstance(context).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String result = jsonObject.getString("result");
                    if (result.equals("1")) {
                        //id有效,成功
                        headstate = jsonObject.getString("headstate");
                        name = jsonObject.getString("username");

                        refreshPage();
                        dialog.loadSuccess();
                    }

                    //刷新成功
                    refreshLayout.finishRefresh();
                    refreshLayout.setEnableRefresh(true);
                    dialog.dismiss();
                } catch (JSONException e) {
                    //刷新成功
                    refreshLayout.finishRefresh();
                    refreshLayout.setEnableRefresh(true);
                    dialog.loadFail();
                    dialog.dismiss();
                    e.printStackTrace();
                }
            }, error -> {
                refreshLayout.finishRefresh();
                refreshLayout.setEnableRefresh(true);
                dialog.loadFail();
                dialog.dismiss();
            }, params);
        } else {
            refreshLayout.finishRefresh();
            refreshLayout.setEnableRefresh(true);
        }
    }

    //刷新页面
    private void refreshPage() {
        if (!headstate.equals("0")) {
            Glide.with(context).load(headstate).into(iv_head);
        }
    }


    //为下拉加载框添加监视器

    private void attachRefreshListener() {

        //添加刷新事件，该监听下拉刷新
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            getUserInfo();
            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);
        });

        //添加加载事件，该监听上拉加载
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            //上拉加载
            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);

        });


    }


    private void getTopImage() {
        final RoundedImageView iv = new RoundedImageView(context);
        iv.setCornerRadius(25);
        iv.setOnClickListener(v -> {
            //轮播图点击事件的响应
        });
        topimage_array.add(iv);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.top_page_1));
        final RoundedImageView iv_2 = new RoundedImageView(context);
        iv_2.setCornerRadius(25);
        iv_2.setOnClickListener(v -> {
            //轮播图点击事件的响应
        });
        topimage_array.add(iv_2);
        iv_2.setImageDrawable(getResources().getDrawable(R.drawable.top_page_2));

        final RoundedImageView iv_3 = new RoundedImageView(context);
        iv_3.setCornerRadius(25);
        iv_3.setOnClickListener(v -> {
            //轮播图点击事件的响应
        });
        topimage_array.add(iv_3);
        iv_3.setImageDrawable(getResources().getDrawable(R.drawable.top_page_3));
        adapter_guide.notifyDataSetChanged();
    }


    //前往头像修改页面
    private void toPerson() {
        //发送广播给mainactivity
        Intent intent = new Intent();
        intent.setAction("com.maple.changeHeadReceiver");
        context.sendBroadcast(intent);
    }


    //搜索垃圾
    private void searchRubbish() {
        Intent intent = new Intent(context, SearchActivity.class);
        startActivity(intent);
    }

    //选择更多的输入方案
    private void guidechoice() {

    }

    //装饰加载条
    private void decorateLoading() {
        if (dialog == null) {
            LoadingDialog.Builder builder = new LoadingDialog.Builder(context);
            builder.setLoading_text(getText(R.string.loading))
                    .setSuccess_text(getText(R.string.success))
                    .setFail_text(getText(R.string.fail));

            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
    }


}