package com.maple.rubbishseparator.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class GuidePagerAdapter extends PagerAdapter {
    private ArrayList<View> list;

    public GuidePagerAdapter(ArrayList<View> list) {
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;

    }

    public void destroyItem(ViewGroup view, int position, @NonNull Object object) {
        // TODO Auto-generated method stub
        view.removeView(list.get(position));//删除页卡
    }

    @NonNull
    public Object instantiateItem(ViewGroup view, int position) {
        // TODO Auto-generated method stub
        //添加图片控件到轮播图控件
        view.addView(list.get(position));
        return list.get(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

}
