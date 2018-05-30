package com.ckt.testauxiliarytool.cameratest.common;

/**
 * Created by D22433 on 2017/9/6.
 */

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;

import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Created by Cc on 2017/8/21.
 * <p>
 * Saves a JPEG {@link Image} into the specified {@link File}.
 */

public class ImageSaver implements Runnable {

    private Context mContext;

    private final WeakReference<Image> mWeakImage;

    /**
     * The file we save the image into.
     */
    private final File mFile;

    public ImageSaver(Context context, Image image, File file) {
        mContext = context;
        mWeakImage = new WeakReference<>(image);
        mFile = file;
    }

    @Override
    public void run() {
        FileOutputStream output = null;
        Image image = null;
        try {
            image = mWeakImage.get();
            if (image == null) return;
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            output = new FileOutputStream(mFile);
            output.write(bytes);
            output.flush();

            Uri uri = Uri.fromFile(mFile);
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            LogUtil.i(IConstValue.TAG, "Saved: " + mFile);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (image != null) {
                //close image or throws maxImages (2) has already been acquired, call #close
                // before acquiring more err
                image.close();
            }

            if (mContext != null) {
                mContext = null;
            }

        }
    }
}
