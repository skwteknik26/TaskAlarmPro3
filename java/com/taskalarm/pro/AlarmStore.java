package com.taskalarm.pro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Map;

public class AlarmStore {
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("native_alarms", Context.MODE_PRIVATE); }

    static void put(Context c, JSONObject o) {
        try { sp(c).edit().putString("a" + o.getInt("id"), o.toString()).apply(); } catch (Exception ignored) {}
    }

    static JSONObject get(Context c, int id) {
        try { String s = sp(c).getString("a" + id, null); return s == null ? null : new JSONObject(s); }
        catch (Exception e) { return null; }
    }

    static void remove(Context c, int id) { sp(c).edit().remove("a" + id).apply(); }

    static PendingIntent pi(Context c, int id) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction("com.taskalarm.pro.ALARM").putExtra("id", id);
        i.setData(Uri.parse("alarm://" + id));
        return PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static long next(long at, String rep) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(at);
        if ("week".equals(rep)) cal.add(Calendar.DAY_OF_YEAR, 7);
        else if ("month".equals(rep)) cal.add(Calendar.MONTH, 1);
        else if ("year".equals(rep)) cal.add(Calendar.YEAR, 1);
        else cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTimeInMillis();
    }

    static void schedule(Context c, JSONObject o) {
        try {
            int id = o.getInt("id");
            long at = o.getLong("at");
            String rep = o.optString("repeat", "");
            long now = System.currentTimeMillis();
            while (at <= now && !rep.isEmpty()) at = next(at, rep);
            if (at <= now) return;
            o.put("at", at);
            put(c, o);
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            Intent show = new Intent(c, MainActivity.class);
            PendingIntent showPi = PendingIntent.getActivity(c, id, show,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            // setAlarmClock: exact, works in Doze, and gets background-start allowance
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(at, showPi), pi(c, id));
        } catch (Exception ignored) {}
    }

    static void cancel(Context c, int id) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi(c, id));
        remove(c, id);
    }

    static void rescheduleAll(Context c) {
        for (Map.Entry<String, ?> e : sp(c).getAll().entrySet()) {
            try { schedule(c, new JSONObject(String.valueOf(e.getValue()))); } catch (Exception ignored) {}
        }
    }
}
