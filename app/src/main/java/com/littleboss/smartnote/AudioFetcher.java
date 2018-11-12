package com.littleboss.smartnote;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.littleboss.smartnote.Utils.AudioClipper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class AudioFetcher {
    static private String audioPath = "data/data/com.littleboss.smartnote/resources/audios";
    static MediaRecorder mediaRecorder;
    static File audioFile, clippedAudioFile;
    static boolean isRecording = false;
    static private Recorder recorder;
    /**
     * 开始录音，无参数.
     * 若当前已经在录音则会直接返回。
     * 在调用之前确认录音权限：ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
     */

    static private PullableSource mic() {
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

        String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFile = new File(audioPath + "/" + curTime + ".wav");
        audioFile.getParentFile().mkdirs();
        recorder = OmRecorder.wav(
                new PullTransport.Default(
                        mic(),
                        new PullTransport.OnAudioChunkPulledListener() {
                            @Override
                            public void onAudioChunkPulled(AudioChunk audioChunk) {

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
            e.printStackTrace();
        }


        String result = "";
        if (saveMillis != 0) {
//            String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            clippedAudioFile = new File(audioPath + "/last" +saveMillis + "MillisOf_" + audioFile.getName());
            new AudioClipper().audioClip(
                    audioFile.getAbsolutePath(),
                    clippedAudioFile.getAbsolutePath(),
                    saveMillis
            );
            System.out.println(String.format("audioClipped(%s,%s,%d)",audioFile.getAbsolutePath(),
                    clippedAudioFile.getAbsolutePath(),
                    saveMillis));
            result = clippedAudioFile.getAbsolutePath();
        }
        // remove audioFile
        audioFile.delete();
        isRecording = false;
        return result;
    }

    static void stopAndDiscard(){
        stopRecording(0);
    }
}
