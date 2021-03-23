package com.maple.rubbishseparator.activity;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.dialog.TextComfirmDialog;
import com.maple.rubbishseparator.util.UtilsStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PermissionActivity extends AppCompatActivity implements TextComfirmDialog.OnButtomClickedListener {

    private final String TAG = "Permissions";
    private int REQUEST_CODE_PERMISSION = 0x00099;
    private final int dialog_1 = 101;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 可以调用该方法，设置是否允许滑动退出
        UtilsStyle.setStatusBarMode(this, true, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }


    /**
     * 请求权限
     *
     * @param permissions 请求的权限
     * @param requestCode 请求权限的请求码
     */
    public void requestPermission(String[] permissions, int requestCode) {
        this.REQUEST_CODE_PERMISSION = requestCode;
        if (checkPermissions(permissions)) {
            permissionSuccess(REQUEST_CODE_PERMISSION);
        } else {
            List<String> needPermissions = getDeniedPermissions(permissions);
            ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[0]), REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * 检测所有的权限是否都已授权
     *
     */
    private boolean checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     */
    private List<String> getDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                needRequestPermissionList.add(permission);
            }
        }
        return needRequestPermissionList;
    }

    /**
     * 系统请求权限回调
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (verifyPermissions(grantResults)) {
                permissionSuccess(REQUEST_CODE_PERMISSION);
            } else {
                permissionFail(REQUEST_CODE_PERMISSION);
                showTipsDialog();
            }
        }
    }

    /**
     * 确认所有的权限是否都已授权
     *
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示提示对话框
     */

    private void showTipsDialog() {
        TextComfirmDialog dialog = new TextComfirmDialog(this, getResources().getString(R.string.permission_comfirm), getResources().getString(R.string.require_permission), dialog_1);
        dialog.setButtomClickedListener(this);
        dialog.show();
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = Objects.requireNonNull(dialog.getWindow()).getAttributes();
        lp.alpha = 0.8f;//设置透明度
        lp.width = (int) (display.getWidth() * 0.6); //设置宽度
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public void onButtomClick(int num, int dialog_id) {
        if (num == 1) {
            switch (dialog_id) {
                case dialog_1:
                    startAppSettings();
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 启动当前应用设置页面
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    /**
     * 获取权限成功
     *
     */
    public void permissionSuccess(int requestCode) {
        Log.d(TAG, "获取权限成功=" + requestCode);
    }

    /**
     * 权限获取失败
     *
     */
    public void permissionFail(int requestCode) {
        Log.d(TAG, "获取权限失败=" + requestCode);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }


}
