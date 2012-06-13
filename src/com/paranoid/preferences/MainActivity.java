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
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity{
    
    protected static final String HTTP_HEADER = "http://paranoidandroid.d4net.org/";
    protected static final String ROM_VERSION_PROPERTY = "ro.pa.version";
    protected static final String DEVICE_NAME_PROPERTY = "ro.product.device";
    protected static String ROM_VERSION_OTA = "rom_version.ota";
    protected static String ROM_MIRRORS = "rom_mirrors.ota";
    protected static ArrayList<String[]> mServerMirrors = new ArrayList();
    protected static ProgressDialog mLoadingProgress;
    protected static boolean mServerTimeout = false;
    protected static double mLatestVersion;
    protected static String mDevice;
    protected static long mStart;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDevice = Utils.getProp(DEVICE_NAME_PROPERTY) + File.separator;
        ROM_VERSION_OTA = HTTP_HEADER+mDevice+ROM_VERSION_OTA;
        ROM_MIRRORS = HTTP_HEADER+mDevice+ROM_MIRRORS;
        mServerMirrors.clear();
        mLoadingProgress = ProgressDialog.show(MainActivity.this, null, getString(R.string.loading_info), false, false);
        final boolean mIsConnected = DownloadFiles.requestInternetConnection(this);
        new Thread(new Runnable(){
            public void run(){
                if(mIsConnected){
                    WebFields.OtaVersion mOtaVersion = new WebFields.OtaVersion();
                    mStart = System.currentTimeMillis();
                    while(mLatestVersion == 0){
                        mOtaVersion.getWebVersion();
                        if(System.currentTimeMillis() - mStart > 15000){
                            mToastHandler.sendEmptyMessage(1);
                            mServerTimeout = true;
                        }
                        if(mLatestVersion != 0 || mServerTimeout)
                            break;
                    }
                    WebFields.RomMirrors mMirrors = new WebFields.RomMirrors();
                    mStart = System.currentTimeMillis();
                    while(mServerMirrors.isEmpty()){
                        mMirrors.getMirrorList();
                        if(System.currentTimeMillis() - mStart > 15000){
                            mToastHandler.sendEmptyMessage(1);
                            mServerTimeout = true;
                        }
                        if(!mServerMirrors.isEmpty() || mServerTimeout)
                            break;
                    }
                    mLoadingProgress.dismiss();
                    if(!mServerTimeout){
                        if(mLatestVersion > Utils.getRomVersion(ROM_VERSION_PROPERTY)){
                            mDialogHandler.sendEmptyMessage(0);
                        }
                        else{
                            mToastHandler.sendEmptyMessage(0);
                        }
                    }
                } else
                    mLoadingProgress.dismiss();
            }
        }).start();
    }
    
   private CharSequence[] mMirrorNames(){
       String[] mTemp = new String[mServerMirrors.size()];
       for(int i=0; i<mTemp.length; i++){
           if(mServerMirrors.get(i)[1].equals("self_server")){
               mServerMirrors.set(i, new String[]{getString(R.string.default_mirror), HTTP_HEADER+mDevice+"paranoid"+mLatestVersion+".zip"});
           }
           mTemp[i] = mServerMirrors.get(i)[0];
       }
       return mTemp;
   }

   private Handler mToastHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    Toast.makeText(MainActivity.this, R.string.no_update_found, Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, R.string.timeout, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
   
   private Handler mDialogHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(getString(R.string.update_found_rom)+" (v"+mLatestVersion+")")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id){
                        final CharSequence[] items = mMirrorNames();
                        AlertDialog.Builder serverBuilder = new AlertDialog.Builder(MainActivity.this);
                        serverBuilder.setTitle(getString(R.string.select_mirror));
                        serverBuilder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                new DownloadFiles().requestDownload(mServerMirrors.get(item)[1], R.string.rom_downloaded, "temp.zip", MainActivity.this);
                            }
                        });
                        AlertDialog alert = serverBuilder.create();
                        alert.show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                    }
                });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    public static void rebootDialog(int dialog, final Activity activity, final boolean isTheme){
        AlertDialog.Builder rebootAlert = new AlertDialog.Builder(activity);
        rebootAlert.setMessage(dialog)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(activity.getString(R.string.reboot_alert)+"\n"+Environment.getExternalStorageDirectory().getPath()+"/temp.zip")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                RunCommands.execute(new String[]{
                                "busybox echo 'install_zip(\"/sdcard/temp.zip\");' > /cache/recovery/extendedcommand",
                                "busybox echo 'install_zip(\"/emmc/temp.zip\");' >> /cache/recovery/extendedcommand",
                                "reboot recovery"}, 0); 
                            }
                        });
                    AlertDialog alert = builder.create();
                    alert.show();
         }})
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        rebootAlert.show();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }
}