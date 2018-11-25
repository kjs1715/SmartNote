package com.littleboss.smartnote;

import android.util.Log;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

public class MSSpeechRecognizer {

    private SpeechRecognizer speechRecognizer;
    private String recognizedText;
    public MSSpeechRecognizer() {

    }

    public void getRecognizedText(String wavFilePath, LBAudioView mLBAudioView) {
        SpeechConfig config = SpeechConfig.fromSubscription(
                "8b62e7f8404740b4987d4a60d79a1467",
                "westus"
        );
        Log.i("getRecognizedText : ", "filepath = " + wavFilePath + " mLBAudioView = " + mLBAudioView.toDataString());
        config.setSpeechRecognitionLanguage("zh-CN");
        AudioConfig audioInput = AudioConfig.fromWavFileInput(wavFilePath);
        speechRecognizer = new SpeechRecognizer(config, audioInput);

        recognizedText = "";

        speechRecognizer.recognizing.addEventListener((o, e) -> {
            Log.i("recognizing : ", "File = " + wavFilePath + " word = " + e.getResult().getText());
            mLBAudioView.setRecognizedText(recognizedText + e.getResult().getText());
        });

        speechRecognizer.recognized.addEventListener((o, e) -> {
            Log.i("recognized : ", "File = " + wavFilePath + " content = " + e.getResult().getText());
            recognizedText += e.getResult().getText();
            mLBAudioView.setRecognizedText(recognizedText);
        });

        speechRecognizer.startContinuousRecognitionAsync();
    }
}