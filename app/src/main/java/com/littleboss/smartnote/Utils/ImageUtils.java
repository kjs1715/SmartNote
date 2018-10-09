package com.littleboss.smartnote.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.Html;


/**
 * @Author: Buzz Kim
 * @Date: 08/10/2018 9:55 PM
 * @param
 * @Description: Class for resize the image
 *
 */
public class ImageUtils {
    public static Bitmap resizeImage(Bitmap originalBitmap, int size) {
        // TODO: 08/10/2018 Modify the best size for the application 
        int height = originalBitmap.getHeight();
        int width = originalBitmap.getWidth();
        int newHeight = 800;
        int newWidth = 800;

        float scaleHeight = (float)newHeight / height;
        float scaleWidth = (float)newWidth / width;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleHeight, scaleWidth);
        Bitmap mBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);
        return mBitmap;
    }
}
