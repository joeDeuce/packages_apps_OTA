package com.paranoid.preferences;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Utils {
    
    private static final String MOUNT_SYSTEM_RW="mount -o rw,remount /system";
    private static final String MOUNT_SYSTEM_RO="mount -o ro,remount /system";
    
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
}
