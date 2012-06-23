/*
 * Copyright (C) 2012 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paranoid.preferences;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class FetchOnlineData extends AsyncTask<Integer, String, String>{
    
    protected static final String HTTP_HEADER = "http://paranoidandroid.d4net.org/";
    protected static final String DEVICE_NAME_PROPERTY = "ro.cm.device";
    protected static String ROM_VERSION_OTA = "rom_version.ota";
    protected static String ROM_MIRRORS = "rom_mirrors.ota";
    protected static String mDevice;
    protected String[] mTempContent;
    
    @Override
    protected String doInBackground(Integer... paramss) {
        mDevice = Utils.getProp(DEVICE_NAME_PROPERTY) + File.separator;
        try {
            String temp = "";
            URL url = new URL(HTTP_HEADER + mDevice + (paramss[0] == 0 ? ROM_VERSION_OTA : ROM_MIRRORS));
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                temp += str + "\n";
            }
            mTempContent = temp.split("\n");
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
