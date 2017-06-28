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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.paddle.utils.FileUtils;

public class OpticalCharacterRecognizerActivity extends AppCompatActivity {

    private static final String TAG = "OCRecognizerActivity";
    private OpticalCharacterRecognizer recognizer;

    private static final String CONFIG_PATH = "vgg_attention_eng/config.bin";
    private static final String PARAMS_PATH = "vgg_attention_eng/vgg_attention_eng.weights";
    private static float[] MEANS = {127.5F};

    private static final int IMAGE_HEIGHT = 48;
    private static final int IMAGE_CHANNEL = 1;

    private static final int NUM_CLASSES = 97;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optical_character_recognizer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StaticTable table = StaticTable.create(NUM_CLASSES);
        recognizer = OpticalCharacterRecognizer.create(getAssets(),
                                                       CONFIG_PATH,
                                                       PARAMS_PATH,
                                                       MEANS,
                                                       table.getTable());

        final ImageView recogImageView = (ImageView) findViewById(R.id.recog_image);
        final TextView tv = (TextView) findViewById(R.id.recog_result);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.gallery_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Bitmap bitmap = FileUtils.getBitmapFromAssets(getAssets(), "images/eng_48x48.jpg");
                recogImageView.setImageBitmap(bitmap);

                recognizer.recognize(bitmap, IMAGE_HEIGHT, IMAGE_CHANNEL);
                String[] results = recognizer.analyze();
                String showStr = "";
                for (int i = 0; i < results.length; i++) {
                    showStr += i + ": " + results[i] + "\n";
                }

                tv.setText(showStr);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_optical_character_recognizer, menu);
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
}
