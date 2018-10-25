package com.littleboss.smartnote;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private void createPlayIconAndText() {
        playIcon = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        playIcon.setLayoutParams(params);
        playIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.play));
        content = new EditText(getContext());
        addView(playIcon);
        addView(content);
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
