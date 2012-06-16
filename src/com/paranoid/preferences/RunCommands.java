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

import android.content.Context;
import android.os.PowerManager;
import java.io.DataOutputStream;
import java.io.IOException;

public class RunCommands implements Runnable{
    	
    private static String [] mCommand = null;
    private static Context mContext;
            
    public RunCommands(String[] command, Context context) {
        RunCommands.mCommand = command;
        RunCommands.mContext = context;
    }
    
    public static boolean execute(String[] command, Context context) {
        Process proc;       
        try {        
            proc = Runtime.getRuntime().exec("su");            
            DataOutputStream os = new DataOutputStream(proc.getOutputStream());            
            for (String tmpCmd : command) {
                os.writeBytes(tmpCmd+"\n");
            }
            os.flush();       
            os.close();	             
            proc.waitFor();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            pm.reboot("recovery");
            return true;
        } catch (IOException e) {                
            e.printStackTrace();
            return false;           
        } catch (InterruptedException e) {        
            e.printStackTrace();
            return false;
        } 
    }

    public void run() {
        execute(mCommand, mContext);	
    }
    
}
