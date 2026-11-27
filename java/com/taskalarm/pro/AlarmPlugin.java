package com.taskalarm.pro;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONObject;

@CapacitorPlugin(name = "NativeAlarm")
public class AlarmPlugin extends Plugin {

    @PluginMethod
    public void schedule(PluginCall call) {
        try {
            AlarmStore.schedule(getContext(), new JSONObject(call.getData().toString()));
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        Integer id = call.getInt("id");
        if (id != null) {
            AlarmStore.cancel(getContext(), id);
            AlarmStore.cancel(getContext(), -id); // snooze slot
        }
        call.resolve();
    }

    /** Memeriksa & (bila force/ada yang kurang) membuka halaman izin: overlay lalu full-screen intent. */
    @PluginMethod
    public void ensureAccess(PluginCall call) {
        Context c = getContext();
        boolean overlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(c);
        boolean fsi = true;
        if (Build.VERSION.SDK_INT >= 34) {
            fsi = ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).canUseFullScreenIntent();
        }
        try {
            if (!overlay) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + c.getPackageName()));
                getActivity().startActivity(i);
            } else if (!fsi) {
                Intent i = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + c.getPackageName()));
                getActivity().startActivity(i);
            }
        } catch (Exception ignored) {}
        JSObject r = new JSObject();
        r.put("overlay", overlay);
        r.put("fullScreen", fsi);
        call.resolve(r);
    }
}
