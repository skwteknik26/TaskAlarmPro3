package com.taskalarm.pro;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.content.res.AssetFileDescriptor;
import android.media.*;
import android.net.Uri;
import android.os.*;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;

public class AlarmService extends Service {
    static final String ACTION_START = "start", ACTION_STOP = "stop", ACTION_SNOOZE = "snooze";
    static final String CH = "alarm_fullscreen", BROADCAST = "com.taskalarm.pro.ALARM_STOPPED";
    static final int NOTIF_ID = 7001, SNOOZE_MIN = 10;

    MediaPlayer mp; Vibrator vib; PowerManager.WakeLock wl;
    final Handler h = new Handler(Looper.getMainLooper());
    int curId;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent i, int f, int sid) {
        String a = (i == null || i.getAction() == null) ? ACTION_START : i.getAction();
        int id = i == null ? 0 : i.getIntExtra("id", 0);
        if (ACTION_STOP.equals(a)) { stopAll(); return START_NOT_STICKY; }
        if (ACTION_SNOOZE.equals(a)) {
            try {
                JSONObject o = AlarmStore.get(this, id);
                if (o != null) {
                    int sidn = id > 0 ? -id : id; // slot terpisah agar jadwal berulang tidak tertimpa
                    o.put("id", sidn);
                    o.put("at", System.currentTimeMillis() + SNOOZE_MIN * 60000L);
                    o.put("repeat", "");
                    AlarmStore.schedule(this, o);
                }
            } catch (Exception ignored) {}
            stopAll();
            return START_NOT_STICKY;
        }
        JSONObject o = AlarmStore.get(this, id);
        if (o == null) { stopSelf(); return START_NOT_STICKY; }
        curId = id;
        startAlarm(o);
        return START_NOT_STICKY;
    }

    PendingIntent svc(String action) {
        Intent s = new Intent(this, AlarmService.class).setAction(action).putExtra("id", curId);
        return PendingIntent.getService(this, action.hashCode() + curId, s,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    void startAlarm(JSONObject o) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "Alarm Layar Penuh", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(null, null); // suara diputar sendiri lewat MediaPlayer
            ch.enableVibration(false);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(ch);
        }
        Intent full = new Intent(this, AlarmActivity.class)
                .putExtra("id", curId).putExtra("title", o.optString("title")).putExtra("body", o.optString("body"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fpi = PendingIntent.getActivity(this, curId, full,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(this, CH)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(o.optString("title", "Pengingat"))
                .setContentText(o.optString("body"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fpi, true)
                .setContentIntent(fpi).setOngoing(true).setAutoCancel(false)
                .addAction(0, "Tunda " + SNOOZE_MIN + " mnt", svc(ACTION_SNOOZE))
                .addAction(0, "Matikan", svc(ACTION_STOP)).build();
        try {
            if (Build.VERSION.SDK_INT >= 29)
                startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            else startForeground(NOTIF_ID, n);
        } catch (Exception e) { nm.notify(NOTIF_ID, n); }

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        int dur = Math.max(10, o.optInt("dur", 60));
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "taskalarm:ring");
        wl.acquire((dur + 5) * 1000L);

        // tampilkan layar alarm langsung (butuh izin "tampil di atas aplikasi lain" di Android 10+)
        try { startActivity(full); } catch (Exception ignored) {}

        playSound(o.optString("sound", ""));
        if (o.optBoolean("vib", true)) {
            vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] p = {0, 700, 500};
            if (Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createWaveform(p, 0));
            else vib.vibrate(p, 0);
        }
        h.postDelayed(this::stopAll, dur * 1000L);
    }

    void playSound(String file) {
        try {
            mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            boolean ok = false;
            if (file != null && !file.isEmpty()) {
                try {
                    AssetFileDescriptor afd = getAssets().openFd("public/sounds/" + file);
                    mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close(); ok = true;
                } catch (Exception ignored) {}
            }
            if (!ok) {
                Uri u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (u == null) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                mp.setDataSource(this, u);
            }
            mp.setLooping(true);
            mp.prepare();
            mp.start();
        } catch (Exception e) { /* tetap getar/notifikasi walau suara gagal */ }
    }

    void stopAll() {
        h.removeCallbacksAndMessages(null);
        try { if (mp != null) { mp.stop(); mp.release(); mp = null; } } catch (Exception ignored) {}
        try { if (vib != null) vib.cancel(); } catch (Exception ignored) {}
        try { if (wl != null && wl.isHeld()) wl.release(); } catch (Exception ignored) {}
        JSONObject o = AlarmStore.get(this, curId);
        if (o != null && o.optString("repeat", "").isEmpty() && curId != 0) AlarmStore.remove(this, curId);
        sendBroadcast(new Intent(BROADCAST).setPackage(getPackageName()));
        stopForeground(true);
        stopSelf();
    }

    @Override public void onDestroy() { stopAll2(); super.onDestroy(); }
    void stopAll2() {
        try { if (mp != null) { mp.release(); mp = null; } } catch (Exception ignored) {}
        try { if (vib != null) vib.cancel(); } catch (Exception ignored) {}
    }
}
