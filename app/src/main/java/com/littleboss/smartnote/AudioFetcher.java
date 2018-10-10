package com.littleboss.smartnote;

import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioFetcher {
    static private String audioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio";
    static MediaRecorder mediaRecorder;
    static File audioFile;

    /**
     * 开始录音，无参数.
     */
    static void startRecording() {
        mediaRecorder = new MediaRecorder();
        String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFile = new File(audioPath + curTime + ".m4a");
        audioFile.getParentFile().mkdirs();
        try {
            audioFile.createNewFile();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(96000);
            mediaRecorder.setOutputFile(audioFile);
            mediaRecorder.prepare();
            mediaRecorder.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 停止录音并将已保存的录音的位置存储到数据库中.
     */
    static void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        NoteDatabase noteDatabase = NoteDatabase.getInstance();
        noteDatabase.setLatestAudioLocation(audioFile.getAbsolutePath());
    }
}
