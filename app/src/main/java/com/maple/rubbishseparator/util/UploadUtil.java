package com.maple.rubbishseparator.util;


import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class UploadUtil {
    public interface uploadBackListener {
        void success(String message);
    }

    public void setListener(uploadBackListener listener) {
        uploadBackListener = listener;
    }

    public uploadBackListener uploadBackListener;

    public void uploadHttpClient(String url, String state, String filename, String user_id) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        File filAbs = new File(state);

        if (filAbs.exists()) {
            MultipartEntity muti = new MultipartEntity();
            FileBody fileBody = new FileBody(filAbs);
            muti.addPart("file", fileBody);
            try {
                muti.addPart("filename", new StringBody(filename));
                muti.addPart("Id", new StringBody(user_id));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post.setEntity(muti);
            try {
                HttpResponse response = client.execute(post);
                uploadBackListener.success(response.toString());

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }


}

