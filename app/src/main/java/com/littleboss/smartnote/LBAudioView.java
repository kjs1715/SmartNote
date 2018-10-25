package com.littleboss.smartnote;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.microsoft.cognitiveservices.speech.Recognizer;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.EventHandler;

import java.io.IOException;

/**
 * 录音模块。
 * 单击播放按钮可进行播放，在播放中单击播放按钮可实现暂停。
 * 文本部分是一个内置的文本编辑可进行编辑。
 */
public class LBAudioView extends LinearLayout implements LBAbstractView {
    private ImageView playIcon;
    private EditText content;
    private String audioFilePath;
    private MediaPlayer mediaPlayer;
    private SpeechRecognizer speechRecognizer;
    private void createPlayIconAndText() {
        playIcon = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        playIcon.setLayoutParams(params);
        playIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.play));
        content = new EditText(getContext());
        addView(playIcon);
        addView(content);
    }
    private void startSpeechRecognizing() {
        SpeechConfig config = SpeechConfig.fromSubscription(
                "65b7cc0c702146d4a6e15b48e79cc0fa",
                "westus"
        );
        //config.setSpeechRecognitionLanguage("zh-Hans");
        AudioConfig audioInput = AudioConfig.fromWavFileInput(audioFilePath);
        speechRecognizer = new SpeechRecognizer(config, audioInput);

        speechRecognizer.recognizing.addEventListener(new EventHandler<SpeechRecognitionEventArgs>() {
            @Override
            public void onEvent(Object o, SpeechRecognitionEventArgs e) {
                content.setText("Recognizing... " + e.getResult().getText());
            }
        });

        speechRecognizer.recognized.addEventListener(new EventHandler<SpeechRecognitionEventArgs>() {
            @Override
            public void onEvent(Object o, SpeechRecognitionEventArgs e) {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    content.setText(e.getResult().getText());
                }
                else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    content.setText("Failed recognizing...");
                }
            }
        });

        try {
            speechRecognizer.startContinuousRecognitionAsync().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addPlayIconClickListener() {
        playIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(audioFilePath);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.prepareAsync();
                }
                else {
                    mediaPlayer.stop();
                }
            }
        });
    }
    public LBAudioView(Activity _context) {
        super(_context);
        createPlayIconAndText();
    }
    public LBAudioView(Activity _context, String _audioFilePath) {
        super(_context);
        createPlayIconAndText();
        audioFilePath = _audioFilePath;
        addPlayIconClickListener();
        //startSpeechRecognizing();
        content.setText(_audioFilePath);
    }

    /**
     * Instantiates a new Lb audio view.
     *
     * @param _context       上下文环境
     * @param _audioFilePath 音频存储的完整路径
     * @param text           语音识别后的结果（可自己修改）
     */
    public LBAudioView(Activity _context, String _audioFilePath, String text) {
        super(_context);
        createPlayIconAndText();
        audioFilePath = _audioFilePath;
        addPlayIconClickListener();
        content.setText(text);
    }

    @Override
    public String toString() {
        String replace = content.getText().toString();
        replace.replace('"', '\"');
        return "<audio src = \"" + audioFilePath + "\" content = \"" + replace + "\">";
    }
}
