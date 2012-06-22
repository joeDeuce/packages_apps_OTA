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

package com.paranoid.preferences.scheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.paranoid.preferences.*;

public class SchedulerService extends Service {
    
    private static final int NOTIFICATION_ID = 42;
    private WebFields.OtaVersion mOtaVersion;
    private boolean mServerTimeout = false;
    private double mLatestVersion;
    private long mStart;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStart = System.currentTimeMillis();
        final boolean mIsConnected = DownloadFiles.requestInternetConnection(getApplicationContext(), false);
        new Thread(new Runnable(){
            public void run(){
                if(mIsConnected){
                    mOtaVersion = new WebFields.OtaVersion();
                    while(mOtaVersion.version == 0){
                        mOtaVersion.getWebVersion();
                        if(System.currentTimeMillis() - mStart > 15000){
                            mServerTimeout = true;
                        }
                        if(mOtaVersion.version != 0 || mServerTimeout)
                            break;
                    }
                    if(!mServerTimeout){
                        if(mOtaVersion.version > Utils.getRomVersion()){
                            mLatestVersion = mOtaVersion.version;
                            mNotificationHandler.sendEmptyMessage(0);
                        }
                    }
                }
            }
        }).start();
        return Service.START_STICKY;
    }
    
    private Handler mNotificationHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            int icon = R.drawable.ic_launcher;
            CharSequence tickerText = getString(R.string.app_name);
            Notification notification = new Notification(icon, tickerText, mStart);
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_ALL;
            CharSequence contentTitle = getString(R.string.update_found_notification);
            CharSequence contentText = getString(R.string.update_found_notification_summary)+mLatestVersion;
            Intent notificationIntent = new Intent(SchedulerService.this, MainActivity.class);
            notificationIntent.putExtra("update", true);
            PendingIntent contentIntent = PendingIntent.getActivity(SchedulerService.this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT + PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
