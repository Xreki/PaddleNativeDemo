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

package org.paddle.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class ImageUtils {
    public static byte[] getRgbPixels(Bitmap img) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        int[] pixels = new int[imgWidth * imgHeight];
        img.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        byte[] imageData = new byte[imgWidth * imgHeight * 3];
        for (int i = 0; i < imgWidth * imgHeight; i++) {
            int color = pixels[i];
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;

            imageData[3 * i + 0] = (byte) red;
            imageData[3 * i + 1] = (byte) green;
            imageData[3 * i + 2] = (byte) blue;
        }

        return imageData;
    }

    /**
     * @brief Get the gray pixels of the whole bitmap
     */
    public static byte[] getGrayPixels(Bitmap img) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        return getGrayPixels(img, 0, imgWidth, 0, imgHeight);
    }

    /**
     * @brief Get the gray pixels of given region in the bitmap
     */
    public static byte[] getGrayPixels(Bitmap img,
            int left, int right, int top, int bottom) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        if (left < 0 || left > right || right > imgWidth) {
            return null;
        }

        if (top < 0 || top > bottom || bottom > imgHeight) {
            return null;
        }

        int[] pixels = new int[imgWidth * imgHeight];
        img.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        int wantedWidth = right - left;
        int wantedHeight = bottom - top;
        byte[] grayImageData = new byte[wantedWidth * wantedHeight];
        for (int i = 0; i < wantedHeight; i++) {
            for (int j = 0; j < wantedWidth; j++) {
                int color = pixels[(i + top) * imgWidth + (j + left)];
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                if ((red == green) && (green == blue)) {
                    grayImageData[i * wantedWidth + j] = (byte) red;
                } else {
                    grayImageData[i * wantedWidth + j] =
                            (byte) (red * 0.11 + green * 0.59 + blue * 0.3);
                            //(byte)(red * 0.3 + green * 0.59 + blue * 0.11);
                }
            }
        }

        return grayImageData;
    }

    public static Bitmap copy(Bitmap img) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        int[] pixels = new int[imgWidth * imgHeight];
        img.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        return Bitmap.createBitmap(
                pixels, imgWidth, imgHeight, Config.ARGB_8888);
    }

    /**
     * @brief Resize the bitmap
     */
    public static Bitmap resize(Bitmap img, int wantedHeight) {
        if (img == null || img.isRecycled() || wantedHeight < 5) {
            return null;
        }

        int dstHeight, dstWidth;

        dstHeight = wantedHeight;
        dstWidth = img.getWidth() * wantedHeight / img.getHeight();
        dstWidth = (dstWidth > wantedHeight) ? dstWidth : wantedHeight;

        return Bitmap.createScaledBitmap(img, dstWidth, dstHeight, false);
    }

    /**
     * @brief Resize the bitmap with the scale factor
     */
    public static Bitmap resize(Bitmap img, float scale) {
        if (img == null || img.isRecycled() || scale <= 0) {
            return null;
        }

        int dstHeight, dstWidth;

        dstHeight = (int)(scale * (float)img.getHeight());
        dstWidth = (int)(scale * (float)img.getWidth());

        return Bitmap.createScaledBitmap(img, dstWidth, dstHeight, false);
    }

    /**
     * @brief Get the sub image in the region
     */
    public static Bitmap cropImage(Bitmap img,
            int left, int right, int top, int bottom) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        if (left < 0 || left > right || right > imgWidth) {
            return null;
        }

        if (top < 0 || top > bottom || bottom > imgHeight) {
            return null;
        }

        int[] pixels = new int[imgWidth * imgHeight];
        img.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        int subWidth = right - left;
        int subHeight = bottom - top;
        int[] subPixels = new int[subHeight * subWidth];
        for (int i = 0; i < subHeight; i++) {
            for (int j = 0; j < subWidth; j++) {
                subPixels[i * subWidth + j] =
                    pixels[(i + top) * imgWidth + (j + left)];
            }
        }

        return Bitmap.createBitmap(
                subPixels, subWidth, subHeight, Config.ARGB_8888);
    }

    /**
     * @brief Draw a red rectangle in the bitmap 
     */
    public static Bitmap drawRect(Bitmap img,
            int left, int right, int top, int bottom) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        if (left < 0 || left > right || right > imgWidth) {
            return null;
        }

        if (top < 0 || top > bottom || bottom > imgHeight) {
            return null;
        }

        int[] pixels = new int[imgWidth * imgHeight];
        img.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        int drawWidth = right - left;
        int drawHeight = bottom - top;

        // Draw Top & Bottom
        for (int i = 0; i < drawWidth; i += 2) {
            pixels[top * imgWidth + (i + left)] = 0xFFFF0000;
            pixels[(bottom - 1) * imgWidth + (i + left)] = 0xFFFF0000;
        }

        // Draw Left & Right
        for (int i = 0; i < drawHeight; i += 2) {
            pixels[(i + top) * imgWidth + left] = 0xFFFF0000;
            pixels[(i + top) * imgWidth + right - 1] = 0xFFFF0000;
        }

        return Bitmap.createBitmap(
                pixels, imgWidth, imgHeight, Config.ARGB_8888);
    }

    public static Bitmap drawRects(Bitmap img, ArrayList<Rect> rects) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        if (rects.size() <= 0) {
            return copy(img);
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        int[] pixels = new int[imgWidth * imgHeight];
        img.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);

        for (int index = 0; index < rects.size(); index++) {
            int left = rects.get(index).left;
            int right = rects.get(index).right;

            if (left < 0 || left > right || right > imgWidth) {
                continue;
            }

            int top = rects.get(index).top;
            int bottom = rects.get(index).bottom;
            if (top < 0 || top > bottom || bottom > imgHeight) {
                continue;
            }

            int drawWidth = right - left;
            int drawHeight = bottom - top;

            int color = 0xFF000000;
            int stride = 1;
            if (index == 0) {
                color = 0xFFFF0000;
                stride = 2;
            } else {
                color = 0xFF00FF00;
                stride = 1;
            }

            // Draw Top & Bottom
            for (int i = 0; i < drawWidth; i += stride) {
                pixels[top * imgWidth + (i + left)] = color;
                pixels[(bottom - 1) * imgWidth + (i + left)] = color;
            }

            // Draw Left & Right
            for (int i = 0; i < drawHeight; i += stride) {
                pixels[(i + top) * imgWidth + left] = color;
                pixels[(i + top) * imgWidth + right - 1] = color;
            }
        }

        Bitmap result = Bitmap.createBitmap(pixels, imgWidth, imgHeight, Config.ARGB_8888);

        return result;
    }

    /**
     * @brief Rotate a bitmap
     */
    public static Bitmap rotateBitmap(Bitmap img, int degree) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        if (degree == 0) {
            return copy(img);
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(img, 0, 0,
                img.getWidth(), img.getHeight(), matrix, true);
    }

    public static Bitmap writeTexts(Bitmap img,
            ArrayList<Rect> positions, ArrayList<String> texts) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        if (positions.size() <= 0 || texts.size() <= 0) {
            return copy(img);
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        Bitmap result = Bitmap.createBitmap(imgWidth, imgHeight, Config.ARGB_8888);

        Canvas textedCanvas = new Canvas(result);
        textedCanvas.drawColor(Color.TRANSPARENT);

        Paint textedPaint = new Paint();
        Typeface font = Typeface.create("宋体", Typeface.BOLD);
        textedPaint.setColor(Color.RED);
        textedPaint.setTypeface(font);
        textedPaint.setTextSize(16);

        textedCanvas.drawBitmap(img, 0, 0, textedPaint);
        int minSize = (positions.size() < texts.size()) ?
            positions.size() : texts.size();
        for (int i = 0; i < minSize; i++) {
            textedCanvas.drawText(texts.get(i),
                    positions.get(i).left, positions.get(i).bottom, textedPaint);
        }

        textedCanvas.save(Canvas.ALL_SAVE_FLAG);
        textedCanvas.restore();

        return result;
    }

    public static Bitmap appendText(Bitmap img, String text) {
        if (img == null || img.isRecycled()) {
            return null;
        }

        if (text == null || text.length() <= 0) {
            return copy(img);
        }

        String[] rows = text.split("\n");

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        int resWidth = imgWidth;
        int resHeight = imgHeight + 32 * rows.length + 16;

        Bitmap result = Bitmap.createBitmap(resWidth, resHeight, Config.ARGB_8888);

        Canvas textedCanvas = new Canvas(result);
        textedCanvas.drawColor(Color.TRANSPARENT);

        Paint textedPaint = new Paint();
        Typeface font = Typeface.create("宋体", Typeface.BOLD);
        textedPaint.setColor(Color.RED);
        textedPaint.setTypeface(font);
        textedPaint.setTextSize(16);
        
        textedCanvas.drawBitmap(img, 0, 0, textedPaint);
        for (int i = 0; i < rows.length; i++) {
            textedCanvas.drawText(rows[i],
                    0, imgHeight + (i + 1) * 32, textedPaint);
        }

        textedCanvas.save(Canvas.ALL_SAVE_FLAG);
        textedCanvas.restore();

        return result;
    }

    public static void saveToJPG(Bitmap image, String path) {
        if (image == null || image.isRecycled()) {
            return;
        }

        if (path == null || path.length() <= 0) {
            return;
        }

        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            image.compress(CompressFormat.JPEG, 90, fos);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void saveToPNG(Bitmap image, String path) {
        if (image == null || image.isRecycled()) {
            return;
        }

        if (path == null || path.length() <= 0) {
            return;
        }

        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            image.compress(CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Bitmap combineToDisplay(Bitmap above, Bitmap below) {
        if ((above == null || above.isRecycled())
                && (below == null || below.isRecycled())) {
            return null;
        }
        if (above == null || above.isRecycled()) {
            return copy(below);
        }
        if (below == null || below.isRecycled()) {
            return copy(above);
        }

        int width1 = above.getWidth();
        int height1 = above.getHeight();
        int width2 = below.getWidth();
        int height2 = below.getHeight();

        int width = (width1 > width2) ? width1 : width2;
        int height = height1 + height2 + 1;

        int[] pixels = new int[width * height];

        int[] pixels1 = new int[width1 * height1];
        above.getPixels(pixels1, 0, width1, 0, 0, width1, height1);
        for (int i = 0; i < height1; i++) {
            for (int j = 0; j < width1; j++) {
                pixels[i * width + j] = pixels1[i * width1 + j];
            }
            for (int j = width1; j < width; j++) {
                pixels[i * width + j] = 0xFFFFFFFF;
            }
        }
        {
            // i = height1
            for (int j = 0 ; j < width; j++) {
                pixels[height1 * width + j] = 0xFFFF0000;
            }
        }

        int[] pixels2 = new int[width2 * height2];
        below.getPixels(pixels2, 0, width2, 0, 0, width2, height2);
        for (int i = height1 + 1; i < height; i++) {
            for (int j = 0; j < width2; j++) {
                pixels[i * width + j] = pixels2[(i - height1 - 1) * width2 + j];
            }
            for (int j = width2; j < width; j++) {
                pixels[i * width + j] = 0xFFFFFFFF;
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Config.ARGB_8888);
    }
}
