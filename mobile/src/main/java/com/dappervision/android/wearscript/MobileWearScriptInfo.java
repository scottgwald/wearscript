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

package com.dappervision.android.wearscript;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.dappervision.android.wearscript.controller.MobileScriptActivity;
import com.dappervision.wearscript.launcher.ApplicationInfo;
import com.dappervision.wearscript.launcher.WearScriptInfo;
import com.dappervision.wearscript.ui.SetupActivity;
import com.dappervision.wearscript.ui.StopActivity;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
public class MobileWearScriptInfo extends WearScriptInfo{
    private static final String EXTRA_NAME = "extra";

    /**
     * The application name.
     */
    private CharSequence title;
    /**
     * The intent used to start the application.
     */
    private Intent intent;


    public MobileWearScriptInfo() {

    }

    public MobileWearScriptInfo(String title) {
        this.title = title;
    }

    public MobileWearScriptInfo(Context context, String title, String filePath) {
        this.title = title;
        intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(context, MobileScriptActivity.class));
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_NAME, filePath);
    }

    public MobileWearScriptInfo playground(Context context) {
        MobileWearScriptInfo wsi = new MobileWearScriptInfo("Playground");
        wsi.setActivity(new ComponentName(context, MobileScriptActivity.class));
        return wsi;
    }

    public MobileWearScriptInfo stop(Context context) {
        MobileWearScriptInfo wsi = new MobileWearScriptInfo("Stop");
        wsi.setActivity(new ComponentName(context, StopActivity.class));
        return wsi;
    }

    public MobileWearScriptInfo setup(Context context) {
        MobileWearScriptInfo wsi = new MobileWearScriptInfo("Setup");
        wsi.setActivity(new ComponentName(context, SetupActivity.class));
        return wsi;
    }

    @Override
    public WearScriptInfo buildInfo(Context context, String name, String filePath) {
        return new MobileWearScriptInfo(context, name, filePath);
    }

    final void setActivity(ComponentName className) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
