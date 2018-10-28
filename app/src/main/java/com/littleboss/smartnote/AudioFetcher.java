package com.littleboss.smartnote;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaRecorder;

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
    static File audioFile;
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

        /*
        mediaRecorder = new MediaRecorder();
        String curTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFile = new File(audioPath + "/" + curTime + ".m4a");
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
        */


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
    static String stopRecording() {
        if (!isRecording)
            return null;

        /*
        mediaRecorder.stop();
        mediaRecorder.release();
        */


        try {
            recorder.stopRecording();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String latestAudioLocation = audioFile.getAbsolutePath();

        NoteDatabase noteDatabase = NoteDatabase.getInstance();
        noteDatabase.setLatestAudioLocation(latestAudioLocation);
        isRecording = false;
        return latestAudioLocation;
    }
}
