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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    
    public static final String UPDATE_INTENT = "com.paranoid.preferences.scheduler.UPDATE_ALARM_MANAGER";
    private Intent mIntent;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;
    private int mRefreshInterval;

    @Override 
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mIntent = new Intent(context, SchedulerService.class);
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mPendingIntent = PendingIntent.getService(context, 0, mIntent, 0);
        mRefreshInterval = sharedPreferences.getInt("interval", 86400);
        if (UPDATE_INTENT.equals(intent.getAction())){
            mAlarmManager.cancel(mPendingIntent);
            startScheduler(context);
        }
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startScheduler(context);
	}
    }
    
    public void startScheduler(Context context){
        if(mRefreshInterval != 0){
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), mRefreshInterval * 1000, mPendingIntent);
        } else
            context.stopService(mIntent);
    }
    
    
    
}