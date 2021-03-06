package com.littleboss.smartnote.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;


/**
 * @Author: Buzz Kim
 * @Date: 08/10/2018 9:55 PM
 * @param
 * @Description: Class for resize the image
 *
 */
public class ImageUtils {
    public static Bitmap resizeImage(Bitmap originalBitmap, int nWidth, int nHeight) {
        if(originalBitmap == null)
            return null;
        int height = originalBitmap.getHeight();
        int width = originalBitmap.getWidth();

        float scaleHeight = (float)nHeight / height;
        float scaleWidth = (float)nWidth / width;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap mBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);
        return mBitmap;
    }
}
