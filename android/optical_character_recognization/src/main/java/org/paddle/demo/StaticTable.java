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

import android.util.Log;

public class StaticTable {
    private static final String TAG = "StaticTable";
    private String[] table = null;

    private StaticTable() {}

    public static StaticTable create(int numClasses) {
        if (numClasses <= 0) {
            Log.e(TAG, "Invalid numClasses.");
            return null;
        }

        StaticTable handle = new StaticTable();

        handle.table = new String[numClasses];

        int index = 0;
        handle.table[index++] = "!";
        handle.table[index++] = "\"";
        handle.table[index++] = "#";
        handle.table[index++] = "$";
        handle.table[index++] = "%";
        handle.table[index++] = "&";
        handle.table[index++] = "'";
        handle.table[index++] = "(";
        handle.table[index++] = ")";
        handle.table[index++] = "*";
        handle.table[index++] = "+";
        handle.table[index++] = ",";
        handle.table[index++] = "-";
        handle.table[index++] = ".";
        handle.table[index++] = "/";
        handle.table[index++] = "0";
        handle.table[index++] = "1";
        handle.table[index++] = "2";
        handle.table[index++] = "3";
        handle.table[index++] = "4";
        handle.table[index++] = "5";
        handle.table[index++] = "6";
        handle.table[index++] = "7";
        handle.table[index++] = "8";
        handle.table[index++] = "9";
        handle.table[index++] = ":";
        handle.table[index++] = ";";
        handle.table[index++] = "<";
        handle.table[index++] = "=";
        handle.table[index++] = ">";
        handle.table[index++] = "?";
        handle.table[index++] = "@";
        handle.table[index++] = "A";
        handle.table[index++] = "B";
        handle.table[index++] = "C";
        handle.table[index++] = "D";
        handle.table[index++] = "E";
        handle.table[index++] = "F";
        handle.table[index++] = "G";
        handle.table[index++] = "H";
        handle.table[index++] = "I";
        handle.table[index++] = "J";
        handle.table[index++] = "K";
        handle.table[index++] = "L";
        handle.table[index++] = "M";
        handle.table[index++] = "N";
        handle.table[index++] = "O";
        handle.table[index++] = "P";
        handle.table[index++] = "Q";
        handle.table[index++] = "R";
        handle.table[index++] = "S";
        handle.table[index++] = "T";
        handle.table[index++] = "U";
        handle.table[index++] = "V";
        handle.table[index++] = "W";
        handle.table[index++] = "X";
        handle.table[index++] = "Y";
        handle.table[index++] = "Z";
        handle.table[index++] = "[";
        handle.table[index++] = "\\";
        handle.table[index++] = "]";
        handle.table[index++] = "^";
        handle.table[index++] = "_";
        handle.table[index++] = "`";
        handle.table[index++] = "a";
        handle.table[index++] = "b";
        handle.table[index++] = "c";
        handle.table[index++] = "d";
        handle.table[index++] = "e";
        handle.table[index++] = "f";
        handle.table[index++] = "g";
        handle.table[index++] = "h";
        handle.table[index++] = "i";
        handle.table[index++] = "j";
        handle.table[index++] = "k";
        handle.table[index++] = "l";
        handle.table[index++] = "m";
        handle.table[index++] = "n";
        handle.table[index++] = "o";
        handle.table[index++] = "p";
        handle.table[index++] = "q";
        handle.table[index++] = "r";
        handle.table[index++] = "s";
        handle.table[index++] = "t";
        handle.table[index++] = "u";
        handle.table[index++] = "v";
        handle.table[index++] = "w";
        handle.table[index++] = "x";
        handle.table[index++] = "y";
        handle.table[index++] = "z";
        handle.table[index++] = "{";
        handle.table[index++] = "|";
        handle.table[index++] = "}";
        handle.table[index++] = "~";
        handle.table[index++] = " ";

        return handle;
    }

    public String[] getTable() {
        return table;
    }
}