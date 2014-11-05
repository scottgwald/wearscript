/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.dappervision.glass.wearscript;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.dappervision.glass.wearscript.controller.GlassScriptActivity;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.launcher.ApplicationInfo;
import com.dappervision.wearscript.launcher.WearScriptInfo;
import com.dappervision.wearscript.ui.SetupActivity;
import com.dappervision.wearscript.ui.StopActivity;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
public class GlassWearScriptInfo extends WearScriptInfo {
    private static final String EXTRA_NAME = "extra";
    /**
     * When set to true, indicates that the icon has been resized.
     */
    boolean filtered;
    /**
     * The application name.
     */
    private CharSequence title;
    /**
     * The intent used to start the application.
     */
    private Intent intent;
    /**
     * The application icon.
     */
    private Drawable icon;

    private GlassWearScriptInfo(String title) {
        this.title = title;
    }

    public GlassWearScriptInfo(Context context, String title, String filePath) {
        this.title = title;
        intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(context, GlassScriptActivity.class));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_NAME, filePath);
    }

    public GlassWearScriptInfo() {

    }

    public GlassWearScriptInfo playground(Context context) {
        GlassWearScriptInfo wsi = new GlassWearScriptInfo("Playground");
        wsi.setActivity(new ComponentName(context, GlassScriptActivity.class), Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return wsi;
    }

    public WearScriptInfo gistSync(Context context) {
        // TODO(brandyn): Instead of waiting 2 sec, we should retry until we get a response
        byte[] data = "<body style='width:640px; height:480px; overflow:hidden; margin:0' bgcolor='black'><center><h1 style='font-size:70px;color:#FAFAFA;font-family:monospace'>WearScript</h1><h1 style='font-size:40px;color:#FAFAFA;font-family:monospace'>Gist Sync<br><br>Docs @ wearscript.com</h1></center><script>function s() {setTimeout(function () {WS.say('syncing');WSRAW.gistSync()}, 2000)};window.onload=function () {WS.serverConnect('{{WSUrl}}', 's')}</script></body>".getBytes();
        String path = Utils.SaveData(data, "scripting/", false, "gist.html");
        return buildInfo(context, "Gist Sync", path);
    }

    @Override
    public WearScriptInfo buildInfo(Context context, String name, String filePath) {
        return new GlassWearScriptInfo(context, name, filePath);
    }

    public GlassWearScriptInfo stop(Context context) {
        GlassWearScriptInfo wsi = new GlassWearScriptInfo("Stop");
        wsi.setActivity(new ComponentName(context, StopActivity.class), Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return wsi;
    }

    public GlassWearScriptInfo setup(Context context) {
        GlassWearScriptInfo wsi = new GlassWearScriptInfo("Setup");
        wsi.setActivity(new ComponentName(context, SetupActivity.class), Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return wsi;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     *
     * @param className   the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationInfo)) {
            return false;
        }

        ApplicationInfo that = (ApplicationInfo) o;
        return title.equals(that.title) &&
                intent.getComponent().getClassName().equals(
                        that.intent.getComponent().getClassName());
    }

    @Override
    public int hashCode() {
        int result;
        result = (title != null ? title.hashCode() : 0);
        final String name = intent.getComponent().getClassName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public Intent getIntent() {
        return intent;
    }

    public CharSequence getTitle() {
        return title;
    }

    public int getId() {
        return title.hashCode();
    }
}
