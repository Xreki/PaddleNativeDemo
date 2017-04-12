/* Copyright (c) 2016 PaddlePaddle Authors. All Rights Reserve.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

package org.paddle.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import org.paddle.utils.FileUtils;

public class ImageClassifierActivity extends AppCompatActivity {

    private static final String TAG = "ImageClassifierActivity";
    private ImageClassifier classifier;

    private static final String URL = "http://paddlepaddle.bj.bcebos.com/model_zoo/imagenet/resnet_50.tar.gz";
    private static final String CONFIG = "resnet_50/resnet_50.bin";
    private static final String PARAMS = "resnet_50/resnet_50.zip";
    private static float[] MEANS = {103.939F, 116.779F, 123.680F};

    private static final int IMAGE_HEIGHT = 224;
    private static final int IMAGE_WIDTH = 224;
    private static final int IMAGE_CHANNEL = 3;

    private ImageView recogImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paddle_demo_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recogImageView = (ImageView) findViewById(R.id.recog_image);

        classifier = ImageClassifier.create(getAssets(), CONFIG, PARAMS, MEANS);

        Bitmap bitmap = FileUtils.getBitmapFromAssets(getAssets(), "images/dog_400x400.jpg");
        recogImageView.setImageBitmap(bitmap);

        classifier.recognize(bitmap, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_CHANNEL);
        classifier.analyze();

        final TextView tv = (TextView) findViewById(R.id.sample_text);

        FloatingActionButton galleryFab = (FloatingActionButton) findViewById(R.id.gallery_fab);
        galleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Read a bitmap from gallery", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                tv.setText("Hello PaddlePaddle");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_paddle_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String download(String httpUrl, String dir, String filename) {
        if (httpUrl == null || httpUrl.length() == 0 || dir == null || dir.length() == 0) {
            return null;
        }

        String targetDir = FileUtils.getSDPath() + "/" + dir;
        if (FileUtils.isDirExist(targetDir, true) == -1) {
            Log.e(TAG, "Directory (" + targetDir + ") does not exists.");
            return null;
        }

        String targetPath = targetDir + "/" + filename + ".tar.gz";
        Log.i(TAG, "Download: " + httpUrl + " -> " + targetPath);

        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }

        try {
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            byte[] buffer = FileUtils.readInputStream(is);

            File file = new File(filename);
            FileOutputStream fs = new FileOutputStream(file);
            fs.write(buffer);

            if (fs != null) {
                fs.close();
            }
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Download failure.");
            return null;
        }

        return null;
    }
}
