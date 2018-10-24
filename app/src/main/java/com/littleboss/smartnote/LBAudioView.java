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

public class LBAudioView extends LinearLayout {
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
    public LBAudioView(Activity _context, String _audioFilePath, String text) {
        super(_context);
        createPlayIconAndText();
        audioFilePath = _audioFilePath;
        addPlayIconClickListener();
        content.setText(text);
    }
}
