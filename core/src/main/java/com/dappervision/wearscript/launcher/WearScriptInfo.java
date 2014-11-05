package com.dappervision.wearscript.launcher;

import android.content.Context;
import android.content.Intent;

import com.dappervision.wearscript.Utils;

public abstract class WearScriptInfo {
    public abstract Intent getIntent();

    public abstract WearScriptInfo playground(Context context);

    public abstract WearScriptInfo stop(Context context);

    public abstract WearScriptInfo setup(Context context);

    public WearScriptInfo gistSync(Context context) {
        // TODO(brandyn): Instead of waiting 2 sec, we should retry until we get a response
        byte[] data = "<body style='width:640px; height:480px; overflow:hidden; margin:0' bgcolor='black'><center><h1 style='font-size:70px;color:#FAFAFA;font-family:monospace'>WearScript</h1><h1 style='font-size:40px;color:#FAFAFA;font-family:monospace'>Gist Sync<br><br>Docs @ wearscript.com</h1></center><script>function s() {setTimeout(function () {WS.say('syncing');WSRAW.gistSync()}, 2000)};window.onload=function () {WS.serverConnect('{{WSUrl}}', 's')}</script></body>".getBytes();
        String path = Utils.SaveData(data, "scripting/", false, "gist.html");
        return buildInfo(context, "Gist Sync", path);
    }

    public abstract WearScriptInfo buildInfo(Context context, String name, String filePath);

    public abstract CharSequence getTitle();

    public String toString() {
        return getTitle().toString();
    }

    public abstract int getId();
}
