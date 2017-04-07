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

public class ImageClassifier {

    // Used to load the 'image_classifier' library on application startup.
    static {
        System.loadLibrary("image_classifier");
    }

    private static final String TAG = "ImageClassifier";

    private ImageClassifier() {}

    public static ImageClassifier create() {
        ImageClassifier classifier = new ImageClassifier();
        return classifier;
    }

    public void recognize(Bitmap bitmap) {

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
