package com.maple.rubbishseparator.activity;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.Request;

import com.bumptech.glide.Glide;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.StoreState;
import com.maple.rubbishseparator.util.UploadUtil;
import com.maple.rubbishseparator.util.ViewControl;
import com.maple.rubbishseparator.view.CustomDialog_1;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static java.lang.String.valueOf;

public class ChangeHead extends PermissionActivity implements View.OnClickListener, CustomDialog_1.OnTextViewSelectedListener {
    private String user_id = null;
    private String phoneNumber = null;

    private String IMAGE_FILE_NAME;//图片名称
    private String imageUri;//图片的最终手机地址


    private SpotsDialog dialog;
    private ImageView iv_head;
    private Button bt_change;


    //activity result code
    private final int CAMERA_BACK = 301;
    private final int RESIZE_BACK = 302;
    private final int CHOOSE_IMAGE = 303;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_head);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        phoneNumber = intent.getStringExtra("phoneNumber");


        initPhotoError();
        fixNetWork();
        init();
    }

    private void init() {
        iv_head = findViewById(R.id.change_head_icon);
        bt_change = findViewById(R.id.change_head_change);


        decorateLoading();
        getUserInfo();
        iv_head.setOnClickListener(this);
        bt_change.setOnClickListener(this);
    }

    private void getUserInfo() {
        dialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.GETID_EFFIT);
        params.put("Id", user_id);
        params.put("phoneNumber", phoneNumber);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (!jsonObject.getString("headstate").equals("0")) {
                    String headstate = jsonObject.getString("headstate");
                    Glide.with(ChangeHead.this).load(headstate).into(iv_head);

                    dialog.dismiss();
                } else {

                    dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, volleyError -> {

            dialog.dismiss();
        }, params);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.change_head_change:
                    chooseImage();
                    break;
                default:
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case CAMERA_BACK:
                if (new File(StoreState.IMAGE_STATE, IMAGE_FILE_NAME).exists()) {
                    resizeImage(Uri.fromFile(new File(StoreState.IMAGE_STATE, IMAGE_FILE_NAME)));
                }
                break;

            case CHOOSE_IMAGE:
                if (data != null) {
                    if (data.getData() != null) {
                        resizeImage(data.getData());
                    }
                }
                break;

            case RESIZE_BACK:
                //裁剪返回
                if (data != null) {

                    handleImage();
                }
                break;

            default:
                break;
        }
    }


    private void chooseImage() {
        CustomDialog_1 dialog = new CustomDialog_1(this, getString(R.string.choosePictureFromCamera), getString(R.string.choosePictureFromPhotos));
        dialog.setOnTextViewSelectedListener(this);
        dialog.show();
        // 将对话框的大小按屏幕大小的百分比设置
        // 将对话框的大小按屏幕大小的百分比设置
        Display display = getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams lp = Objects.requireNonNull(dialog.getWindow()).getAttributes();
        lp.alpha = 0.9f;
        lp.width = (int) (display.getWidth() * 0.92); //设置宽度
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onTextViewSelected(int num) {
        switch (num) {
            case 1:
                Intent intentFromPhotos = getGalleryIntent();
                startActivityForResult(intentFromPhotos, CHOOSE_IMAGE);
                break;
            case 2:
                checkStoragePermission();
                break;
            default:
                break;

        }
    }

    //处理图片
    public void handleImage() {
        File file = new File(imageUri);
        if (file.exists() && file.length() > 0) {

            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(new File(imageUri)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            iv_head.setImageBitmap(bitmap);
            uploadImage();
        }
    }

    //上传图片
    private void uploadImage() {
        dialog.show();
        new Thread(() -> {
            UploadUtil uploadUtil = new UploadUtil();
            uploadUtil.setListener(message -> uploaddata());
            uploadUtil.uploadHttpClient(HttpHelper.upload_file, imageUri, IMAGE_FILE_NAME, user_id);
        }).start();
    }

    private void uploaddata() {
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.CHANGE_HEAD);
        params.put("Id", user_id);
        params.put("phoneNumber", phoneNumber);
        params.put("headstate", HttpHelper.IMAGES + "user" + user_id + "/" + IMAGE_FILE_NAME);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("result").equals("1")) {

                    dialog.dismiss();
                } else {

                    dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, volleyError -> {

            dialog.dismiss();
        }, params);
    }

    /**
     * 裁剪原始的图片
     */
    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        // outputX , outputY : 裁剪图片宽高
        int output_X = 480;
        intent.putExtra("outputX", output_X);
        int output_Y = 480;
        intent.putExtra("outputY", output_Y);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        String cover_Image_State = valueOf(System.currentTimeMillis());
        IMAGE_FILE_NAME = cover_Image_State + ".png";
        File file = new File(StoreState.IMAGE_STATE, cover_Image_State + ".png");
        imageUri = StoreState.IMAGE_STATE + cover_Image_State + ".png";

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        intent.putExtra("noFaceDetection", true);

        startActivityForResult(intent, RESIZE_BACK);

    }


    private void checkStoragePermission() {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int resultCAMERA = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_DENIED || resultCAMERA == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermission(permissions, 0);
        } else {
            choseHeadImageFromCameraCapture();
        }
    }

    private void choseHeadImageFromCameraCapture() {
        IMAGE_FILE_NAME = System.currentTimeMillis() + ".png";
        //存储照片
        //判断储存卡是否可用
        if (hasSdcard()) {
            //调用系统相机
            Intent intentCamera = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            }
            intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            //将拍照结果保存至photo_file的Uri中，不保留在相册中
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(StoreState.IMAGE_STATE, IMAGE_FILE_NAME)));

            startActivityForResult(intentCamera, CAMERA_BACK);

        } else {
            Toast.makeText(this, getResources().getString(R.string.withoutSdcard), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public Intent getGalleryIntent() {


        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        }
        return intent;
    }

    private void fixNetWork() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }


    private void initPhotoError() {
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }


    //装饰加载条
    private void decorateLoading() {
        if (dialog == null) {
            dialog = new SpotsDialog(this);
            dialog.setCanceledOnTouchOutside(false);
        }
    }


}