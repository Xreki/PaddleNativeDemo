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

package org.paddle.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TableReader {
    private static final String TAG = "TableReader";
    private String[] table = null;

    private TableReader() {}

    public static TableReader create(String sdcardPath) {
        if (sdcardPath == null || sdcardPath.isEmpty()) {
            Log.e(TAG, "Invalid table path.");
            return null;
        }

        TableReader reader = new TableReader();

        List<String> list = new ArrayList<String>();
        try {
            File file = new File(sdcardPath);
            if (file.exists() && file.isFile()) {
                InputStreamReader isr = new InputStreamReader(
                        new FileInputStream(file));
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    list.add(line);
                }
                br.close();
                isr.close();
            } else {
                Log.i(TAG, "cannot find file " + sdcardPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (list.size() > 0) {
            reader.table = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                reader.table[i] = list.get(i);
            }

            return reader;
        } else {
            return null;
        }
    }

    public String[] getTable() {
        return table;
    }
}
