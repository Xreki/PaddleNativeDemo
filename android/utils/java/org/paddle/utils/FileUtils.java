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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Check whether the directory (dirPath) exists.
     * If exist, then return the number of files it contains;
     *  else, create an empty directory according to the second parameter (toCreated).
     **/
    public static int isDirExist(String dirPath, boolean toCreated) {
        if (dirPath == null || dirPath.length() == 0) {
            return -1;
        }

        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            if (toCreated) {
                if (file.mkdirs()) {
                    return 0;
                } else {
                    Log.e(TAG, "Create directory (" + dirPath + ") failure.");
                }
            }
            return -1;
        } else {
            String[] files = file.list();
            return files.length;
        }
    }

    public static boolean isFileExist(String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the root path of device's SD card
     */
    public static String getSDPath() {
        // Check whether SD card exists.
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            File sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        } else {
            return null;
        }
    }

    public static Bitmap getBitmapFromExternal(String path) {
        if (path == null || path.length() <= 0) {
            return null;
        }

        Bitmap image = null;

        if (isFileExist(path)) {
            try {
                File file = new File(path);
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                image = BitmapFactory.decodeStream(bis);

                if (bis != null) {
                    bis.close();
                    bis = null;
                }
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return image;
    }

    public static Bitmap getBitmapFromAssets(AssetManager asset, String path) {
        if (asset == null || path == null || path.length() <= 0) {
            return null;
        }

        Bitmap image = null;

        try {
            BufferedInputStream bis = new BufferedInputStream(asset.open(path));
            image = BitmapFactory.decodeStream(bis);

            if (bis != null) {
                bis.close();
                bis = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return image;
    }

    public static byte[] readInputStream(InputStream is) {
        if (is == null) {
            return null;
        }
        
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            if (bos != null) {
                bos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        return bos.toByteArray();
    }
    
    public static String copyBigDataToExternal(
            AssetManager asset, String assetPath, String sdDir) {
        if (assetPath == null || sdDir == null) {
            return null;
        }
        
        int start = assetPath.lastIndexOf('/');
        if (start < 0) {
            start = 0;
        }
        
        int end = assetPath.lastIndexOf('.');
        if (end < 0) {
            end = assetPath.length();
        }
        
        String filename = assetPath.substring(start, end);
        String sdPath = sdDir + "/" + filename + ".zip";
        Log.i(TAG, "filename = " + filename + ", sdPath = " + sdPath);
        
        try {
            InputStream is = asset.open(assetPath);
            OutputStream os = new FileOutputStream(sdPath);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();

            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sdPath;
    }
    
    /**
     * Unzip a zip file.
     **/
    public static String unzipFiles(String zipPath, String unzipDir) {
        if (zipPath == null || zipPath.length() == 0
                || unzipDir == null || unzipDir.length() == 0) {
            return null;
        }

        File file = new File(zipPath);
        if (!file.exists()) {
            Log.w(TAG, "The input .zip file (" + zipPath + ") does not exist.");
            return null;
        }

        File dirFile = new File(unzipDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        String unzipPath = null;
        try {
            @SuppressWarnings("resource")
            ZipFile zipFile = new ZipFile(file);
            for (@SuppressWarnings("rawtypes")
            Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                String zipEntryName = entry.getName();
                //Log.i(TAG, zipEntryName);
                
                InputStream is = zipFile.getInputStream(entry);
                byte[] buffer = readInputStream(is);
                
                String outPath = (unzipDir + "/" + zipEntryName).replaceAll("\\*", "/");
                if (unzipPath != null) {
                    unzipPath = outPath;
                }

                File outFile = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!outFile.exists()) {
                    outFile.mkdirs();
                }
                
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                
                OutputStream os = new FileOutputStream(outPath);
                os.write(buffer);
                
                if (is != null) {
                    is.close();
                }

                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return unzipPath;
    }
}