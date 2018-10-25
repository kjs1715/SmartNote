package com.littleboss.smartnote;

import android.view.View;

import java.io.Serializable;

public interface LBAbstractView {
    String toString();
    /**
     * 获取view类型
     */
    Enum getViewType();

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
     * 获取显示的文本
     * @return
     */
    String getContent();

    //这里定个了多个组件类型
    enum Type{
        IMAGE,FILE,VOICE,LOCATION,CONTENT,TITLE,UNKOWN
    }
}
