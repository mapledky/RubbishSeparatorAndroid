package com.maple.rubbishseparator.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;


/**
 * 设置状态栏透明工具类
 * Created by Carry
 */



public class UtilsStyle {

    public static void setStatusBarMode(Activity activity, boolean bDark,Context context) {

        //6.0以上

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            View decorView = activity.getWindow().getDecorView();

            int vis = decorView.getSystemUiVisibility();

            if (bDark) {

                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            } else {

                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            }

            decorView.setSystemUiVisibility(vis);


        }

    }

}

