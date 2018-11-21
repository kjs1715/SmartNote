package com.littleboss.smartnote;

import android.view.View;

public interface LBAbstractView {
    String toDataString();
    /**
     * 获取view类型
     */
    ViewType getViewType();

    /**
     * 获取文件本地路径
     * @return
     */
    String getFilePath();

    /**
     * 获取具体实现的view
     * @return
     */
    View getView();

    /**
     * 设置点击组件下面的空白回调事件
     * @param listener
     */
    void setOnClickViewListener(LBClickListener listener);

    /**
     * 通过文本设置内容
     */
    void setContent(String s);

    //这里定个了多个组件类型
    enum ViewType{
        IMAGE,FILE,VOICE,AUDIO,VIDEO,LOCATION,CONTENT,TITLE,UNKOWN
    }
}
