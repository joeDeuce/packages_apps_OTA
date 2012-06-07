package com.paranoid.preferences;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.util.ByteArrayBuffer;

public class FetchOnlineData extends AsyncTask<String, String, String>{
    
    private String[] mTempContent;
    
    @Override
    protected String doInBackground(String... paramss) {
        try{
            String output = "";
            URL url = new URL(paramss[0]);
            File file = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+paramss[1]);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current;
            while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.close();

            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            int i = 0;
            while ((line = in.readLine()) != null) {
                    output += line + "\n";
                    i++;
            }
            in.close();
            file.delete();
            mTempContent = output.split("\n");
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    protected void getWebVersion(){
        try{
            MainActivity.mLatestVersion = Double.parseDouble(mTempContent[0].replace("rom_latest_version=", ""));
        } catch(Exception e){
            // Nothing loaded yet
        }
    }
    
    protected void getMirrorList(Activity activity, double mLatestVersion){
        try{
            for(int i=0; i < mTempContent.length; i++){
                String mMirrorName = mTempContent[i].substring(0, mTempContent[i].lastIndexOf("="));
                if(!mMirrorName.contains("=") && !mMirrorName.contains("rom_latest_version")){
                    MainActivity.mServerMirrors.add(new String[]{mMirrorName, mTempContent[i].replace(mMirrorName+"=", "")});
                }
            }
            mTempContent = null;
        } catch(Exception e){
           // Nothing loaded yet
        }
    }
}
