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

public class WebFields {
    
    public static final class OtaVersion extends FetchOnlineData{
        public OtaVersion(){
            this.execute(MainActivity.ROM_VERSION_OTA);
            getWebVersion();
        }
        
        protected void getWebVersion(){
            try{
                MainActivity.mLatestVersion = Double.parseDouble(mTempContent[0].replace("rom_latest_version=", ""));
            } catch (NullPointerException e){
                // Nothing loaded yet
            } catch (NumberFormatException e){
                // We have no numbers or weird characters on OTA file
            }
        }
    }
    
    public static final class RomMirrors extends FetchOnlineData{
        public RomMirrors(){
            this.execute(MainActivity.ROM_MIRRORS);
            getMirrorList();
        }
        
        protected void getMirrorList(){
            try{
                for(int i=0; i < mTempContent.length; i++){
                    String mMirrorName = mTempContent[i].substring(0, mTempContent[i].lastIndexOf("="));
                    MainActivity.mServerMirrors.add(new String[]{mMirrorName, mTempContent[i].replace(mMirrorName+"=", "")});
                }
            } catch(NullPointerException e){
                // Nothing loaded yet
            }
        }
    }
    
}
