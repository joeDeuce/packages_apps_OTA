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

import java.util.ArrayList;

public class WebFields {
    
    private static final int VERSION = 0;
    private static final int MIRRORS = 1;
    
    public static final class OtaVersion extends FetchOnlineData{
        public double version = 0;
        
        public OtaVersion(){
            this.execute(VERSION);
        }
        
        public void getWebVersion(){
            try{
                version = Double.parseDouble(mTempContent[0].replace("rom_latest_version=", ""));
            } catch (NullPointerException e){
                // Nothing loaded yet
            } catch (NumberFormatException e){
                // We have no numbers or weird characters on OTA file
            }
        }
        
        public void release(){
            this.cancel(true);
            version = 0;
        }
    }
    
    public static final class RomMirrors extends FetchOnlineData{
        public ArrayList<String[]> mirrors = new ArrayList();
        
        public RomMirrors(){
            this.execute(MIRRORS);
        }
        
        public void getMirrorList(){
            try{
                for(int i=0; i < mTempContent.length; i++){
                    String mMirrorName = mTempContent[i].substring(0, mTempContent[i].lastIndexOf("="));
                    mirrors.add(new String[]{mMirrorName, mTempContent[i].replace(mMirrorName+"=", "")});
                }
            } catch(NullPointerException e){
                // Nothing loaded yet
            } catch (StringIndexOutOfBoundsException e){
                // Good luck with this one
            }
        }
        
        public void release(){
            this.cancel(true);
            mirrors.clear();
        }
    }
    
}
