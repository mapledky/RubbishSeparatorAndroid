package com.maple.rubbishseparator.helper;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.maple.rubbishseparator.util.StoreState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressHelper {
    /*
   图片压缩
     */
    public static String compressFile(String file, int maxSize) {
        Bitmap bitmap = BitmapFactory.decodeFile(file);
        String file_state = StoreState.IMAGE_STATE + "compress_" + System.currentTimeMillis() + ".PNG";
        File file_compress = new File(file_state);
        if (!file_compress.exists()) {
            try {
                file_compress.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();

        if (width > 200) {
            float scanle = height / width;
            float new_width = 200;
            float new_height = new_width * scanle;
            Matrix matrix = new Matrix();
            matrix.postScale(200 / width, new_height / height);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, matrix, false);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;
        while (baos.toByteArray().length / 1024 > maxSize) { // 循环判断如果压缩后图片是否大于30kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }


        try {
            FileOutputStream fos = new FileOutputStream(file_compress);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
                return file_state;
            } catch (IOException e) {
                e.printStackTrace();
                return file_state;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return file_state;
        }
    }
}
