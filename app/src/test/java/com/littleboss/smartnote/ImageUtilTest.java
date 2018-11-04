package com.littleboss.smartnote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.littleboss.smartnote.Utils.ImageUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ImageUtil;

import java.io.InputStream;

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
