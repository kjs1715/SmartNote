package com.example.liaoyuanda.ifly_5_0;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView tv;

    public void show(Dialog dialog) {
        dialog.show();
    }

    public void dealString(String content) {
        tv.setText(content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        tv = findViewById(R.id.textView);

        SpeechUtility.createUtility(this, SpeechConstant.APPID +getString(R.string.APPID));
        AudioInterface.setEnvActivity(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInterface.listen();
            }
        });
    }
}
