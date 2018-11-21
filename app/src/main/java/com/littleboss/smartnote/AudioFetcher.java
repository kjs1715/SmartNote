package com.littleboss.smartnote;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.littleboss.smartnote.Utils.AudioClipper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class AudioFetcher {
    private static  String audioPath = "data/data/com.littleboss.smartnote/resources/audios";
    static File audioFile, clippedAudioFile;
    static boolean isRecording = false;
    private static  Recorder recorder;
    public static String sep="/";
    /**
     * 开始录音，无参数.
     * 若当前已经在录音则会直接返回。
     * 在调用之前确认录音权限：ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
     */

    AudioFetcher(){}

    private static PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.VOICE_RECOGNITION,
                        AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO,
                        16000
                )
        );
    }

    static void startRecording() {
        if (isRecording)
            return;

        isRecording = true;

        String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + String.valueOf(System.currentTimeMillis());
        audioFile = new File(audioPath + "/" + curTime + ".wav");
        audioFile.getParentFile().mkdirs();
        Log.i("startRec path = ", audioFile.getAbsolutePath());
        recorder = OmRecorder.wav(
                new PullTransport.Default(
                        mic(),
                        new PullTransport.OnAudioChunkPulledListener() {
                            @Override
                            public void onAudioChunkPulled(AudioChunk audioChunk) {
                                Log.i("onAudioChunkPulled","");
                            }
                        }
                ),
                audioFile
        );
        recorder.startRecording();

    }

    /**
     * 停止录音并将已保存的录音的位置存储到数据库中.
     * 若当前没有在录音则会直接返回。
     */

    static String stopRecording(long saveMillis){

        if (!isRecording)
            return null;

        try {
            recorder.stopRecording();
        }
        catch (Exception e) {
            Log.i("error stopRecording", e.toString());
        }

        String result = "";
        if (saveMillis != 0) {
            String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + System.currentTimeMillis();
            //clippedAudioFile = new File(audioPath + "/last" +saveMillis + "MillisOf_" + audioFile.getName());
            clippedAudioFile = new File(audioPath + sep + curTime + "_cut" + ".wav");
            new AudioClipper().audioClip(
                    audioFile.getAbsolutePath(),
                    clippedAudioFile.getAbsolutePath(),
                    saveMillis
            );
            result = clippedAudioFile.getAbsolutePath();
        }
        // remove audioFile
        boolean __ = audioFile.delete();
        if(__)
            Log.i("del suc","delete old audioFile successfully");
        else
            Log.e("del fail","delete old audioFile failed");
        isRecording = false;
        return result;

    }

    static void stopAndDiscard(){
        stopRecording(0);
    }
}
