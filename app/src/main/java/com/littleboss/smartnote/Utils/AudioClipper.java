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
        private String mRitfWaveChunkID;
        public void setRitfWaveChunkID(String __) { mRitfWaveChunkID = __; }
        public String getRitfWaveChunkID() { return mRitfWaveChunkID; }
        //wav文件大小（总大小-8）
        private int mRitfWaveChunkSize;
        public void setRitfWaveChunkSize(int __) { mRitfWaveChunkSize = __; }
        public int getRitfWaveChunkSize() { return mRitfWaveChunkSize; }
        //wav格式
        private String mWaveFormat;
        public void setWaveFormat(String __) { mWaveFormat = __; }
        public String getWaveFormat() { return mWaveFormat; }
        //格式数据块ID：值为"fmt "(注意后面有个空格)
        private String mFmtChunk1ID;
        public void setFmtChunk1ID(String __) { mFmtChunk1ID = __; }
        public String getFmtChunk1ID() { return mFmtChunk1ID; }
        //格式数据块大小，一般为16
        private int mFmtChunkSize;
        public void setFmtChunkSize(int __) { mFmtChunkSize = __; }
        public int getFmtChunkSize() { return mFmtChunkSize; }
        //数据格式，一般为1，表示音频是pcm编码
        private short mAudioFormat;
        public void setAudioFormat(short __) { mAudioFormat = __; }
        public short getAudioFormat() { return mAudioFormat; }
        //声道数
        private short mNumChannel;
        public void setNumChannel(short __) { mNumChannel = __; }
        public short getNumChannel() { return mNumChannel; }
        //采样率
        private int mSampleRate;
        public void setSampleRate(int __) { mSampleRate = __; }
        public int getSampleRate() { return mSampleRate; }
        //每秒字节数
        private int mByteRate;
        public void setByteRate(int __) { mByteRate = __; }
        public int getByteRate() { return mByteRate; }
        //数据块对齐单位
        private short mBlockAlign;
        public void setBlockAlign(short __) { mBlockAlign = __; }
        public short getBlockAlign() { return mBlockAlign; }
        //采样位数
        private short mBitsPerSample;
        public void setBitsPerSample(short __) { mBitsPerSample = __; }
        public short getBitsPerSample() { return mBitsPerSample; }
        //data块，音频的真正数据块
        private String mDataChunkID;
        public void setDataChunkID(String __) { mDataChunkID = __; }
        public String getDataChunkID() { return mDataChunkID; }
        //音频实际数据大小
        private int mDataChunkSize;
        public void setDataChunkSize(int __) { mDataChunkSize = __; }
        public int getDataChunkSize() { return mDataChunkSize; }
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
        mWavHeader.setRitfWaveChunkID(new String(buffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setRitfWaveChunkSize(byteArray2Int(buffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setWaveFormat(new String(buffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setFmtChunk1ID(new String(buffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setFmtChunkSize(byteArray2Int(buffer));
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.setAudioFormat(byteArray2Short(shortBuffer));
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.setNumChannel(byteArray2Short(shortBuffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setSampleRate(byteArray2Int(buffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setByteRate(byteArray2Int(buffer));
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.setBlockAlign(byteArray2Short(shortBuffer));
        size = mDataInputStream.read(shortBuffer);
        assert(size == 2);
        mWavHeader.setBitsPerSample(byteArray2Short(shortBuffer));

        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setDataChunkID(new String(buffer));
        size = mDataInputStream.read(buffer);
        assert(size == 4);
        mWavHeader.setDataChunkSize(byteArray2Int(buffer));
    }
    private void writeHeader() throws IOException {
        mDataOutputStream.writeBytes(mWavHeader.getRitfWaveChunkID());
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.getRitfWaveChunkSize()), 0, 4);
        mDataOutputStream.writeBytes(mWavHeader.getWaveFormat());
        mDataOutputStream.writeBytes(mWavHeader.getFmtChunk1ID());
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.getFmtChunkSize()), 0, 4);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.getAudioFormat()), 0, 2);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.getNumChannel()), 0, 2);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.getSampleRate()), 0, 4);
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.getByteRate()), 0, 4);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.getBlockAlign()), 0, 2);
        mDataOutputStream.write(short2ByteArray((short) mWavHeader.getBitsPerSample()), 0, 2);
        mDataOutputStream.writeBytes(mWavHeader.getDataChunkID());
        mDataOutputStream.write(int2ByteArray((int) mWavHeader.getDataChunkSize()), 0, 4);

    }
    public int getSkipBytes(long suffixLengthMs) throws IOException {
        double duration = mWavHeader.getDataChunkSize() * 1.0 / mWavHeader.getSampleRate() / mWavHeader.getBlockAlign();
        Log.i("duration = ", String.valueOf(duration));

        int suffixBytes = (int) ((suffixLengthMs / 1000.0 / duration) * mWavHeader.getDataChunkSize());
        if (suffixBytes >= mDataInputStream.available())
            return 0;
        return mDataInputStream.available() - suffixBytes;

    }
    private void writeDataSize(String dst, int sizeCount) throws IOException {
        final RandomAccessFile wavFile = new RandomAccessFile(dst, "rw");
        try {
            wavFile.seek(4);
            wavFile.write(int2ByteArray((int) (sizeCount + 44 - 8)), 0, 4);
            wavFile.seek(40);
            wavFile.write(int2ByteArray((int) sizeCount), 0, 4);
        }
        finally {
            wavFile.close();
        }
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
