package com.taskalarm.pro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        int id = i.getIntExtra("id", 0);
        JSONObject o = AlarmStore.get(c, id);
        if (o == null) return;
        Intent s = new Intent(c, AlarmService.class).setAction(AlarmService.ACTION_START).putExtra("id", id);
        ContextCompat.startForegroundService(c, s);
        String rep = o.optString("repeat", "");
        if (!rep.isEmpty()) { // jadwalkan kemunculan berikutnya
            try {
                o.put("at", AlarmStore.next(o.getLong("at"), rep));
                AlarmStore.schedule(c, o);
            } catch (Exception ignored) {}
        }
    }
}
