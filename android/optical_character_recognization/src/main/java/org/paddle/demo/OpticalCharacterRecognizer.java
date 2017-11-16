/*
 *  Copyright (c) 2016 PaddlePaddle Authors. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.paddle.demo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.paddle.utils.FileUtils;

public class OpticalCharacterRecognizer {

    // Used to load the 'optical_character_recognizer' library on application startup.
    static {
        System.loadLibrary("optical_character_recognizer");
    }

    private static final String TAG = "OCRecognizer";
    private static final String WORK_DIR = "paddle_demo/optical_character_recognizer";
    private long gradientMachine = 0;
    private float[] means = null;
    private String[] table = null;
    private float[] probs = null;

    private OpticalCharacterRecognizer() {}

    public static OpticalCharacterRecognizer create(AssetManager assetManager,
                                                    String assetMergedModel,
                                                    float[] means,
                                                    String[] table) {
        OpticalCharacterRecognizer recognizer = new OpticalCharacterRecognizer();

        recognizer.means = means;
        recognizer.table = table;

        recognizer.gradientMachine = init(assetManager, assetMergedModel);
        if (recognizer.gradientMachine == 0) {
            Log.e(TAG, "Create OpticalCharacterRecognizer failure.");
            return null;
        } else {
            return recognizer;
        }
    }

    public void recognize(Bitmap bitmap, int height, int channel) {
        if (bitmap == null || bitmap.isRecycled()) {
            Log.e(TAG, "Invalid bitmap.");
            return;
        }

        if (gradientMachine == 0 || means.length != channel) {
            Log.e(TAG, "Invalid gradientMachine or means.");
            return;
        }

        Bitmap target = null;
        int width = bitmap.getWidth();
        if (bitmap.getHeight() != height) {
            width = bitmap.getWidth() * height / bitmap.getHeight();
            width = (width > height) ? width : height;
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
            } else {
                if ((red == green) && (green == blue)) {
                    rgbs[i] = (byte) red;
                } else {
                    rgbs[i] = (byte) (red * 0.11 + green * 0.59 + blue * 0.3);
                }
            }
        }

        probs = null;
        probs = inference(gradientMachine, means, rgbs, height, width, channel);
    }

    public String analyze() {
        if (probs == null || table == null) {
            return null;
        }

        int numClasses = table.length;
        int sequenceLength = probs.length / numClasses;
        Log.i(TAG, "numClasses: " + numClasses + ", sequenceLength: " + sequenceLength);

        int[] labels = new int[sequenceLength];
        for (int i = 0; i < sequenceLength; i++) {
            int maxid = 0;
            float maxProbs = 0;
            for (int j = 0; j < numClasses; j++) {
                if (probs[i * numClasses + j] > maxProbs) {
                    maxProbs = probs[i * numClasses + j];
                    maxid = j;
                }
            }
            Log.i(TAG, "i: " + i + ", maxid: " + maxid + ", maxProbs: " + maxProbs + ", " + table[maxid]);
            labels[i] = maxid;
        }

        String result = "";
        int blank = numClasses - 1;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] != blank) {
                result += table[labels[i]];
            }
        }
        Log.i(TAG, "result: " + result);

        return result;
    }

    private static native long init(AssetManager assetManager, String mergedModel);

    private native float[] inference(long gradientMachine,
                                     float[] means,
                                     byte[] pixels,
                                     int height,
                                     int width,
                                     int channel);

    private native int release(long gradientMachine);
}
