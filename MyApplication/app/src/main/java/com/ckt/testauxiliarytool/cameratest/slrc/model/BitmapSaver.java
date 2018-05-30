package com.ckt.testauxiliarytool.cameratest.slrc.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import com.ckt.testauxiliarytool.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Cc on 2017/11/28.
 */

public class BitmapSaver implements Runnable {

    private Context mContext;

    private WeakReference<Bitmap> mWeakBitmap;

    /**
     * The file we save the image into.
     */
    private final File mFile;

    public BitmapSaver(Context context, Bitmap bitmap, File file) {
        mContext = context;
        mWeakBitmap = new WeakReference<>(bitmap);
        mFile = file;
    }

    public BitmapSaver(Context context, byte[] bytes, File file) {
        mContext = context;
        mWeakBitmap = new WeakReference<>(BitmapFactory.decodeByteArray(bytes, 0, bytes
                .length));
        mFile = file;
    }

    public BitmapSaver preRotate(float degrees) {
        Bitmap bitmap = mWeakBitmap.get();
        if (bitmap == null) return this;
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight
                (), matrix, true);

        if (bitmap != tempBitmap) {
            bitmap.recycle();
            mWeakBitmap = new WeakReference<>(tempBitmap);
        }

        return this;
    }

    @Override
    public void run() {
        FileOutputStream output = null;
        Bitmap bitmap = mWeakBitmap.get();
        if (bitmap == null) return;
        try {
            output = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

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

            bitmap.recycle();

            if (mContext != null) {
                mContext = null;
            }
        }
    }


}
