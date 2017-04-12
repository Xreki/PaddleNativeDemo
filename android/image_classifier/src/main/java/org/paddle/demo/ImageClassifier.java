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

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.paddle.utils.FileUtils;

public class ImageClassifier {

    // Used to load the 'image_classifier' library on application startup.
    static {
        System.loadLibrary("image_classifier");
    }

    private static final String TAG = "ImageClassifier";
    private static final String WORK_DIR = "paddle_demo/image_classifier/model";
    private String[] tables = null;
    private long gradientMachine = 0;
    private float[] means = null;
    private float[] probabilities = null;

    private ImageClassifier() {}

    public static ImageClassifier create(AssetManager assetManager,
                                         String assetConfig,
                                         String assetParams,
                                         float[] means) {
        ImageClassifier classifier = new ImageClassifier();

        classifier.means = means;

        // String params = classifier.prepare(assetManager, assetParams);
        String params = FileUtils.getSDPath() + "/" + WORK_DIR + "/resnet_50";
        Log.i(TAG, "config (in assets): " + assetConfig);
        Log.i(TAG, "params (in sd card): " + params);

        classifier.gradientMachine = init(assetManager, assetConfig, params);
        if (classifier.gradientMachine == 0) {
            Log.e(TAG, "Create ImageClassifier failure.");
            return null;
        } else {
            return classifier;
        }
    }

    public void recognize(Bitmap bitmap, int height, int width, int channel) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        if (gradientMachine == 0 || means.length != channel) {
            return;
        }

        Bitmap target = null;
        if (bitmap.getHeight() != height || bitmap.getWidth() != width) {
            target = Bitmap.createScaledBitmap(bitmap, width, height, false);
        } else {
            target = bitmap;
        }

        int[] pixels = new int[height * width];
        target.getPixels(pixels, 0, width, 0, 0, width, height);
        if (bitmap != target) {
            target.recycle();
        }

        byte[] rgbs = new byte[height * width * channel];
        for (int i = 0; i < height * width; i++) {
            int color = pixels[i];
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;

            if (channel == 3) {
                // chw
                rgbs[0 * height * width + i] = (byte) red;
                rgbs[1 * height * width + i] = (byte) green;
                rgbs[2 * height * width + i] = (byte) blue;
                // hwc
                // rgbs[i * channel + 0] = (byte) red;
                // rgbs[i * channel + 1] = (byte) green;
                // rgbs[i * channel + 2] = (byte) blue;
            } else {
                if ((red == green) && (green == blue)) {
                    rgbs[i] = (byte) red;
                } else {
                    rgbs[i] = (byte) (red * 0.11 + green * 0.59 + blue * 0.3);
                }
            }
        }

        probabilities = null;
        probabilities = inference(gradientMachine, means, rgbs, height, width, channel);
    }

    public void analyze() {
        if (probabilities == null) {
            return;
        }

        int maxid = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxid]) {
                maxid = i;
            }
        }
        Log.i(TAG, "type: " + maxid + ", probs: " + probabilities[maxid]);
    }

    private String prepare(AssetManager asset, String assetPath) {
        String workDir = FileUtils.getSDPath() + "/" + WORK_DIR;
        // String workDir = WORK_DIR;
        if (FileUtils.isDirExist(workDir, true) == -1) {
            Log.e(TAG, "Work directory (" + workDir + ") does not exist.");
            return null;
        }

        String zipPath = FileUtils.copyBigDataToExternal(asset, assetPath, workDir);
        if (zipPath != null) {
            return FileUtils.unzipFiles(zipPath, workDir);
        } else {
            return null;
        }
    }

    private static native long init(AssetManager assetManager, String config, String params);

    private native float[] inference(long gradientMachine,
                                     float[] means,
                                     byte[] pixels,
                                     int height,
                                     int width,
                                     int channel);

    private native int release(long gradientMachine);
}
