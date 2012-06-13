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

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Utils {
    
    private static final String MOUNT_SYSTEM_RW="mount -o rw,remount /system";
    private static final String MOUNT_SYSTEM_RO="mount -o ro,remount /system";
    protected static final String ROM_VERSION_PROPERTY = "ro.pa.version";
    
    public static String readFile(String file) {
    String text = "";
    String removedBadChars = "";
    try{
        FileInputStream f = new FileInputStream( file );
        FileChannel ch = f.getChannel( );
        ByteBuffer bb = ByteBuffer.allocateDirect( 8192 );
        byte[] barray = new byte[8192];

        int nRead, nGet;
        while ( (nRead=ch.read( bb )) != -1 ){
            if ( nRead == 0 )
                continue;
            bb.position( 0 );
            bb.limit( nRead );
            while( bb.hasRemaining() ){
                nGet = Math.min( bb.remaining( ), 8192 );
                bb.get( barray, 0, nGet );
                char[] theChars = new char[nGet];
                for (int i = 0; i < nGet;) {
                    theChars[i] = (char)(barray[i++]&0xff);
                }

                text += new String(theChars);

            }

            bb.clear( );
        }
        removedBadChars = text;
    }
    catch(Exception e){
        e.printStackTrace();
    }
	return removedBadChars;
    }
    
    public static double getRomVersion(){
        String mString = getProp(ROM_VERSION_PROPERTY);
        String fullVersion = mString.substring(nthOccurrence(mString, '-', 1)+1, nthOccurrence(mString, '-', 2)-1);
        return Double.parseDouble(fullVersion);
    }
    
    public static String getProp(String prop) {
        try {
            String output;
            Process p = Runtime.getRuntime().exec("getprop "+prop);
            p.waitFor();
            BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));
            output = input.readLine();
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static void mountFilesystem(boolean rw){
        String command = rw ? MOUNT_SYSTEM_RW : MOUNT_SYSTEM_RO;
        try{
            RunCommands.execute(new String[]{"su", command}, 0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static int nthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }
}
