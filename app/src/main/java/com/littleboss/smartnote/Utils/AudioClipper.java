package com.littleboss.smartnote.Utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AudioClipper {
    public class WavHeader {

        //RITF标志
        public String mRitfWaveChunkID;
        //wav文件大小（总大小-8）
        public int mRitfWaveChunkSize;
        //wav格式
        public String mWaveFormat;

        //格式数据块ID：值为"fmt "(注意后面有个空格)
        public String mFmtChunk1ID;
        //格式数据块大小，一般为16
        public int mFmtChunkSize;
        //数据格式，一般为1，表示音频是pcm编码
        public short mAudioFormat;
        //声道数
        public short mNumChannel;
        //采样率
        public int mSampleRate;
        //每秒字节数
        public int mByteRate;
        //数据块对齐单位
        public short mBlockAlign;
        //采样位数
        public short mBitsPerSample;

        //data块，音频的真正数据块
        public String mDataChunkID;
        //音频实际数据大小
        public int mDataChunkSize;
    }

    private DataInputStream mDataInputStream;
    private DataOutputStream mDataOutputStream;
    private WavHeader mWavHeader;
    private int mDataSize = 0;
    private static final int BYTES_PER_READ = 1024;

    private int byteArray2Int(byte[] b) {
        int a = 0;
        for (int i = 0; i < 4; ++i) {
            int t = (int)b[i];
            if (t < 0)
                t += 256;
            a += t << (i << 3);
        }
        return a;
    }
    private byte[] int2ByteArray(int a) {
        byte[] b = new byte[4];
        b[0] = (byte)(a & 255);
        b[1] = (byte)((a >> 8) & 255);
        b[2] = (byte)((a >> 16) & 255);
        b[3] = (byte)((a >> 24) & 255);
        return b;
    }
    private short byteArray2Short(byte[] b) {
        int a = 0;
        for (int i = 0; i < 2; ++i) {
            int t = (int)b[i];
            if (t < 0)
                t += 256;
            a += t << (i << 3);
        }
        return (short)a;
    }
    private byte[] short2ByteArray(short a) {
        byte[] b = new byte[4];
        b[0] = (byte)(a & 255);
        b[1] = (byte)((a >> 8) & 255);
        return b;
    }

    private void readHeader() throws IOException {
        mWavHeader = new WavHeader();
        byte[] buffer = new byte[4];
        byte[] shortBuffer = new byte[2];
        int size;
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mRitfWaveChunkID = new String(buffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mRitfWaveChunkSize = byteArray2Int(buffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mWaveFormat = new String(buffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mFmtChunk1ID = new String(buffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mFmtChunkSize = byteArray2Int(buffer);
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.mAudioFormat = byteArray2Short(shortBuffer);
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.mNumChannel = byteArray2Short(shortBuffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mSampleRate = byteArray2Int(buffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mByteRate = byteArray2Int(buffer);
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.mBlockAlign = byteArray2Short(shortBuffer);
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.mBitsPerSample = byteArray2Short(shortBuffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mDataChunkID = new String(buffer);
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.mDataChunkSize = byteArray2Int(buffer);
    }
    private void writeHeader() throws IOException {
        mDataOutputStream.writeBytes(mWavHeader.mRitfWaveChunkID);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.mRitfWaveChunkSize), 0, 4);
        mDataOutputStream.writeBytes(mWavHeader.mWaveFormat);
        mDataOutputStream.writeBytes(mWavHeader.mFmtChunk1ID);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.mFmtChunkSize), 0, 4);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.mAudioFormat), 0, 2);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.mNumChannel), 0, 2);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.mSampleRate), 0, 4);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.mByteRate), 0, 4);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.mBlockAlign), 0, 2);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.mBitsPerSample), 0, 2);
        mDataOutputStream.writeBytes(mWavHeader.mDataChunkID);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.mDataChunkSize), 0, 4);

    }
    public int getSkipBytes(long suffixLengthMs) throws IOException {
        double duration = mWavHeader.mDataChunkSize * 1.0 / mWavHeader.mSampleRate / mWavHeader.mBlockAlign;
        Log.i("duration = ", String.valueOf(duration));
        int suffixBytes = (int) ((suffixLengthMs / 1000.0 / duration) * mWavHeader.mDataChunkSize);
        if (suffixBytes >= mDataInputStream.available())
            return 0;
        return mDataInputStream.available() - suffixBytes;

    }
    private void writeDataSize(String dst, int sizeCount) throws IOException {
        RandomAccessFile wavFile = new RandomAccessFile(dst, "rw");
        wavFile.seek(4);
        wavFile.write(int2ByteArray((int)(sizeCount + 44 - 8)), 0, 4);
        wavFile.seek(40);
        wavFile.write(int2ByteArray((int)sizeCount), 0, 4);
        wavFile.close();
    }
    public boolean audioClip(String src, String dst, long suffixLengthSeconds) {
        try {
            mDataInputStream = new DataInputStream(new FileInputStream(src));
            readHeader();
            byte[] buffer = new byte[BYTES_PER_READ];
            mDataOutputStream = new DataOutputStream(new FileOutputStream(dst));
            writeHeader();
            mDataInputStream.skipBytes(getSkipBytes(suffixLengthSeconds));
            int sizeCount = 0, size = -1;
            while (true) {
                size = mDataInputStream.read(buffer, 0, buffer.length);
                if (size < 0) {
                    mDataOutputStream.close();
                    writeDataSize(dst, sizeCount);
                    mDataInputStream.close();
                    return true;
                }
                mDataOutputStream.write(buffer, 0, size);
                sizeCount += size;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
