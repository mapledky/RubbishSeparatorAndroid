package com.maple.rubbishseparator.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VollySimpleRequest {
    private static RequestQueue mRequestQueue = null;
    private LruCache<String, Bitmap> mCache = null;
    private volatile static VollySimpleRequest instance = null;

    private VollySimpleRequest(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public static VollySimpleRequest getInstance(Context context) {
        if (null == instance) {
            synchronized (VollySimpleRequest.class) {
                if (null == instance) {
                    instance = new VollySimpleRequest(context);
                }
            }
        }
        return instance;
    }

    public final void sendStringRequest(String url, Response.Listener<String> success, Response.ErrorListener error) {
        StringRequest stringRequest = new StringRequest(url, success, error);
        mRequestQueue.add(stringRequest);
    }


    public final void sendStringRequest(int method, String url, Response.Listener<String> success,
                                        Response.ErrorListener error, final Map<String, String> params) {
        StringRequest stringRequest = new StringRequest(method, url, success, error) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        //设置超时
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        mRequestQueue.add(stringRequest);
    }


    public final void sendStringRequest_urlencoded(int method, String url, Response.Listener<String> success,
                                                   Response.ErrorListener error, final Map<String, String> params) {
        StringRequest stringRequest = new StringRequest(method, url, success, error) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded" ;
            }
        };
        //设置超时
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        mRequestQueue.add(stringRequest);
    }

    public final void sendJSONRequest(int method, String url, JSONObject jsonObject,
                                      Response.Listener<JSONObject> success, Response.ErrorListener error) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, jsonObject, success, error);
        mRequestQueue.add(jsonObjectRequest);
    }

    /**
     * @param decodeConfig 位图的属性配置
     */
    public final void sendImageRequest(String url, int maxWidth, int maxHeight, Bitmap.Config decodeConfig,
                                       Response.Listener<Bitmap> success, Response.ErrorListener error) {
        ImageRequest imageRequest = new ImageRequest(url, success, maxWidth, maxHeight, decodeConfig, error);
        mRequestQueue.add(imageRequest);
    }

    /**
     * 默认的ImageRequest，最大宽、高不进行设定
     * Bitmap默认为占用最小配置
     */
    public final void sendImageRequest(String url, Response.Listener<Bitmap> success, Response.ErrorListener error) {
        ImageRequest imageRequest = new ImageRequest(url, success, 0, 0, Bitmap.Config.RGB_565, error);
        mRequestQueue.add(imageRequest);
    }

    /**
     * 拥有缓存机制的加载Image方式
     *
     * @param target     目标ImageView
     * @param defalutRes 默认显示图片
     * @param errorRes   失败时显示图片
     */
    public final void sendImageLoader(String url, ImageView target, int defalutRes, int errorRes) {
        ImageLoader imageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String s) {
                final int maxSize = 10 * 1024 * 1024;//10M大小的缓存
                mCache = new LruCache<String, Bitmap>(maxSize) {
                    @Override
                    protected int sizeOf(String key, Bitmap bitmap) {
                        return bitmap.getRowBytes() * bitmap.getHeight();
                    }
                };
                return mCache.get(s);
            }

            @Override
            public void putBitmap(String s, Bitmap bitmap) {
                mCache.put(s, bitmap);
            }
        });

        ImageLoader.ImageListener imageListener = ImageLoader.
                getImageListener(target, defalutRes, errorRes);
        imageLoader.get(url, imageListener);
    }

}
