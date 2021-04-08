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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.helper.CompressHelper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import dmax.dialog.SpotsDialog;

import static java.lang.String.valueOf;

public class UploadOrder extends PermissionActivity implements View.OnClickListener, CustomDialog_1.OnTextViewSelectedListener {
    //data
    String user_id;
    String phoneNumber;
    String latitude;
    String longitude;


    private ArrayList<String> image_src;

    private String IMAGE_FILE_NAME;//图片名称
    private String imageUri;//图片的最终手机地址

    //view
    private GridLayout gridView;
    private SpotsDialog dialog;
    private ImageView iv_addpic;//添加图片

    private EditText et_inputname;
    private EditText et_inputprice;
    private EditText et_inputdes;

    //activity result code
    private final int CAMERA_BACK = 301;
    private final int RESIZE_BACK = 302;
    private final int CHOOSE_IMAGE = 303;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_order);
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        phoneNumber = intent.getStringExtra("phoneNumber");
        latitude = intent.getStringExtra("latitude");
        longitude = intent.getStringExtra("longitude");

        initPhotoError();
        fixNetWork();
        init();
    }


    @SuppressLint("SetTextI18n")
    private void init() {
        //view
        ImageView iv_back = findViewById(R.id.upload_order_back);
        gridView = findViewById(R.id.upload_order_gidview);
        et_inputname = findViewById(R.id.upload_order_inputname);
        TextView tv_phone = findViewById(R.id.upload_order_inputphone);
        et_inputprice = findViewById(R.id.upload_order_inputprice);
        Button bt_upload = findViewById(R.id.upload_order_upload);
        et_inputdes = findViewById(R.id.upload_order_des);
        tv_phone.setText(getString(R.string.phonenumber) + phoneNumber);
        bt_upload.setOnClickListener(this);
        image_src = new ArrayList<>();

        //添加addpic
        iv_addpic = new ImageView(this);
        iv_addpic.setImageDrawable(getResources().getDrawable(R.drawable.addpic));
        gridView.addView(iv_addpic);
        ViewGroup.LayoutParams params = iv_addpic.getLayoutParams();
        params.width = 300;
        params.height = 300;
        iv_addpic.setLayoutParams(params);

        iv_addpic.setOnClickListener(v -> chooseMutiPic());
        iv_back.setOnClickListener(this);
        decorateLoading();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (ViewControl.avoidRetouch()) {
            switch (v.getId()) {
                case R.id.upload_order_back:
                    finish();
                    break;
                case R.id.upload_order_upload:
                    upload();
                    break;
                default:
                    break;
            }
        }
    }


    //上传
    private void upload() {
        String name = et_inputname.getText().toString();
        String price = et_inputprice.getText().toString();
        String des = et_inputdes.getText().toString();

        //图片判断
        if (image_src.size() == 0) {
            Toast.makeText(this, getString(R.string.at_least_one), Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.equals("")) {
            Toast.makeText(this, getString(R.string.input_order_title), Toast.LENGTH_SHORT).show();
            return;
        }
        if (price.equals("")) {
            Toast.makeText(this, getString(R.string.input_order_price), Toast.LENGTH_SHORT).show();
            return;
        }
        if (des.equals("")) {
            Toast.makeText(this, getString(R.string.input_order_des), Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.show();
        //上传图片
        uploadImage();
    }

    private ArrayList<String> uploadstate;

    //上传图片
    private void uploadImage() {
        uploadstate = new ArrayList<>();
        new Thread(() -> {
            UploadUtil uploadUtil = new UploadUtil();
            AtomicInteger number = new AtomicInteger(0);
            uploadUtil.setListener(message -> {
                number.getAndIncrement();
                if (number.get() == image_src.size()) {
                    uploadinfo();
                }
            });
            for (int i = 0; i < image_src.size(); i++) {
                String compress_state = CompressHelper.compressFile(image_src.get(i), 300);
                String name = String.valueOf(System.currentTimeMillis()) + i + ".PNG";
                uploadstate.add(HttpHelper.IMAGES + "user" + user_id + "/" + name);
                Log.i("upload", HttpHelper.IMAGES + "user" + user_id + "/" + name);
                uploadUtil.uploadHttpClient(HttpHelper.upload_file, compress_state, name, user_id);
            }

        }).start();
    }


    private void uploadinfo() {
        String name = et_inputname.getText().toString();
        String price = et_inputprice.getText().toString();
        String des = et_inputdes.getText().toString();


        StringBuilder images_state = new StringBuilder();
        for (int i = 0; i < uploadstate.size(); i++) {
            if (i == 0) {
                images_state = new StringBuilder(uploadstate.get(i));
            } else {
                images_state.append("$").append(uploadstate.get(i));
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.UPLOAD_INFO);
        params.put("user_id", user_id);
        params.put("title", name);
        params.put("description", des);
        params.put("images", images_state.toString());
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("price", price);
        params.put("phoneNumber", phoneNumber);

        VollySimpleRequest.getInstance(this).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            dialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("result").equals("1")) {
                    //成功
                    Toast.makeText(UploadOrder.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, volleyError -> {
            dialog.dismiss();
        }, params);
    }

    private void chooseMutiPic() {
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

    //处理图片
    public void handleImage() {
        File file = new File(imageUri);
        if (file.exists()) {
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(new File(imageUri)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            gridView.removeView(iv_addpic);
            image_src.add(imageUri);
            gridView.addView(imageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = 300;
            params.height = 300;
            imageView.setLayoutParams(params);

            if (image_src.size() != 9) {
                gridView.addView(iv_addpic);
            }

            imageView.setOnLongClickListener(v -> {
                final View view_popup = View.inflate(this, R.layout.popup_delete, null);
                final PopupWindow popupWindow_delete = new PopupWindow();
                popupWindow_delete.setContentView(view_popup);
                popupWindow_delete.setOutsideTouchable(true);
                popupWindow_delete.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow_delete.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow_delete.setFocusable(true);
                popupWindow_delete.showAsDropDown(imageView, 0, -400);
                //处理删除的点击事件
                view_popup.setOnClickListener(v1 -> {
                    gridView.removeView(imageView);
                    image_src.remove(imageUri);
                    if (image_src.size() + 1 == 9) {
                        gridView.addView(iv_addpic);
                        ViewGroup.LayoutParams params1 = iv_addpic.getLayoutParams();
                        params1.width = 300;
                        params1.height = 300;
                        iv_addpic.setLayoutParams(params1);
                    }
                    //执行删除图片业务
                    popupWindow_delete.dismiss();
                });
                return false;
            });
        }
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
                    File file = new File(imageUri);
                    if (file.exists() && file.length() > 0) {
                        handleImage();
                    }
                }
                break;

            default:
                break;
        }
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

    //装饰加载条
    private void decorateLoading() {
        if (dialog == null) {
            dialog = new SpotsDialog(this);
            dialog.setCanceledOnTouchOutside(false);
        }
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


}