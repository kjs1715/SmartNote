package com.littleboss.smartnote;;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.littleboss.smartnote.NoteEditActivity;
import com.littleboss.smartnote.XFBean;

import java.util.ArrayList;

public class AudioInterface {

    /**
     * AudioInterface 提供某一ui活动调用语音识别的接口。
     * 本类的使用约定如下：
     * - 调用者约定
     *  - 定义方法：void dealString(String content)
     * - 被调用者约定
     *
     * - 调用过程约定
     *  - 初始化：通过方法setEnvActivity(final MainActivity mainActivity)设置服务调用的语境(Context)
     *  - 调用：listen()
     *  - 接受回调：回调方法写在void dealString(String content)方法中
     * */

    @NonNull
    private static String parseData(String resultString) { //创建gson对象.记得要关联一下gson.jar包,方可以使用
        Gson gson = new Gson();                     //参数1 String类型的json数据 参数2.存放json数据对应的bean类
        XFBean xfBean = gson.fromJson(resultString,XFBean.class);          //创建集合,用来存放bean类里的对象
        ArrayList<XFBean.WS> ws = xfBean.ws;                      //创建一个容器,用来存放从每个集合里拿到的数据,使用StringBUndle效率高
        StringBuilder stringBuilder = new StringBuilder();
        for (XFBean.WS w : ws) {
            String text = w.cw.get(0).w;
            stringBuilder.append(text);
        }
        //把容器内的数据转换为字符串返回出去
        return stringBuilder.toString();
    }

    public static void listen() {
        RecognizerDialog mDialog = new RecognizerDialog(envActivity, null);
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        final StringBuilder mStringBuilder = new StringBuilder();

        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String resultString = recognizerResult.getResultString();
                String content = parseData(resultString);

                mStringBuilder.append(content);

                AudioInterface.dealString(mStringBuilder.toString());
            }

            @Override
            public void onError(SpeechError speechError) {
                Log.d("line 71: ",speechError.toString());
            }
        });

        envActivity.show(mDialog);
    }

    // service-environment (caller & result-dealer)
    private static NoteEditActivity envActivity;
    public static void setEnvActivity(final NoteEditActivity mainActivity) {
        AudioInterface.envActivity = mainActivity;
    }

    // service
    private static void dealString(String content) {
        envActivity.dealString(content);
    }
}
