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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFiles extends AsyncTask<String, Integer, Boolean>{
        
        private static Activity activity;
        private static String fileName;
        private static int rebootReason;
        private static ProgressDialog mProgressDialog;
        private static String tempFile;
        public static Boolean isSuccess = false;
        public static int lengthOfFile;
        
        @Override
        protected Boolean doInBackground(String... sUrl) {
            int count;
            try {
                URL url = new URL(sUrl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(tempFile);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int)(total*100/lengthOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected  void onProgressUpdate(Integer... args){
            mProgressDialog.setProgress(args[0]);
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            Utils.mountFilesystem(true);
            File temp = new File(tempFile);
            if(temp.exists() && temp.length() == lengthOfFile){
                final String path = Environment.getExternalStorageDirectory().getPath() + File.separator;
                Utils.mountFilesystem(true);
                RunCommands.execute(new String[]{"busybox mv " + tempFile + " " + path + fileName}, 0);
                Utils.mountFilesystem(false);
                if(result)
                    isSuccess = true;
                if(rebootReason != 0)
                    MainActivity.rebootDialog(rebootReason, activity, false);
            }
            else{
                wrongDownload(activity);
            }
        }
        
    public void requestDownload(String url, int dialogId, String filename, Activity activity){
        tempFile = Environment.getExternalStorageDirectory().getPath()+ File.separator + "temp.tmp";
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setMessage(activity.getString(R.string.downloading));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.show();
        this.activity = activity;
        this.rebootReason = dialogId;
        this.fileName = filename;
        this.execute(url);
    }

    private static void wrongDownload(final Activity activity){
        AlertDialog.Builder wrongDownload = new AlertDialog.Builder(activity);
        wrongDownload.setMessage(R.string.wrong_download)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        wrongDownload.show();
    }
    
    public static boolean requestInternetConnection(Activity activity){
        ConnectivityManager conMgr =  (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null || !i.isConnected() || !i.isAvailable()){
            Toast.makeText(activity, R.string.no_internet, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
