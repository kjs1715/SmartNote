package com.littleboss.smartnote;

import android.view.View;

public interface LBClickListener {

    /**
     * 点击view下面的空白处回调事件，可在此实现插入edittext，在组件下面留一条空白又好看又可以点击
     * @param v 点击的view
     * @param widget 当前的组件
     */
    void onBlankViewClick(View v, View widget);

    /**
     * 组件的点击事件
     * @param v
     * @param widget
     */
    void onContentClick(View v, View widget);
}