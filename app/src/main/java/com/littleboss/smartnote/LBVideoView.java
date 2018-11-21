package com.littleboss.smartnote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;

import com.littleboss.smartnote.Utils.ImageUtils;


public class LBVideoView extends LBImageView {

    public LBVideoView(Context context) {
        this(context, null);
    }
    public LBVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LBVideoView(String content,Context context) {
        this(context);
        parseImageSize(content);
        setContent(this.filePath);
    }

    @Override
    public String toDataString() {
        return "<video>"+ getFilePath()+ "+" + this.width + "+" + this.height + "</video>";
    }

    @Override
    public ViewType getViewType() {
        return ViewType.VIDEO;
    }

    @Override
    public void setContent(String filepath)
    {
        getImage();
        if(this.image==null)
            return;
        Bitmap mImage;
        if(this.width!=-1)
            mImage = ImageUtils.resizeImage(this.image, this.width, this.height);
        else
            mImage=this.image;
        setImage(mImage);
    }

    @Override
    public void getImage() {
        try {
            System.out.println(filePath);
            this.image = getVideoThumbnail(this.filePath);
            System.out.println("Successfully decode");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getVideoThumbnailNoPlayer(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private Bitmap getVideoThumbnail(String filePath) {
        /**
         * 被迫改的。。。
         * 必须要一种static方法通过filePath获得bitmap，但是这个函数是non-static的
         * 所以把可以当做static方法的部分提出来，叫做getVideoThumbnailNoPlayer(String filePath)
         * 这里也就直接用getVideoThumbnailNoPlayer的接口，不重复代码了
         * */
        Bitmap bitmap = getVideoThumbnailNoPlayer(filePath);

        Bitmap playerbmp = BitmapFactory.decodeResource(getResources(), R.drawable.play);

        return mergeBitmap(bitmap,playerbmp);
    }

    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     * @param backBitmap 在底部的位图
     * @param frontBitmap 盖在上面的位图
     * @return
     */
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {

        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        int W=backBitmap.getWidth();
        int H=backBitmap.getHeight();
        int w=Math.min(H,W);
        int h=w;
        Rect dstRect  = new Rect((W-w)/2, (H-h)/2, (W+w)/2, (H+h)/2);
        Rect srcRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap,  srcRect,dstRect, null);
        return bitmap;
    }

}
