package com.littleboss.smartnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.littleboss.smartnote.Utils.ImageUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.test.InstrumentationRegistry;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ImageUtilTest {
    @Test
    public void startTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Bitmap resultBefore;
        resultBefore = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.ic_fab_star);
        Bitmap resultAfter = ImageUtils.resizeImage(resultBefore, 0, 0);
        assertNotEquals(resultBefore, resultAfter);
    }
}
