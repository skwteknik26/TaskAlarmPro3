package com.taskalarm.pro;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.*;
import androidx.core.content.ContextCompat;

public class AlarmActivity extends Activity {
    BroadcastReceiver r;
    int id;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true); }
        else getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        id = getIntent().getIntExtra("id", 0);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#111827"));
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView icon = tv("⏰", 64, Color.WHITE, false);
        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("hh:mm a");
        clock.setTextSize(56); clock.setTextColor(Color.WHITE); clock.setGravity(Gravity.CENTER);
        TextView title = tv(getIntent().getStringExtra("title"), 26, Color.WHITE, true);
        TextView body = tv(getIntent().getStringExtra("body"), 16, Color.parseColor("#9CA3AF"), false);

        Button stop = btn("Matikan", "#DC2626");
        Button snooze = btn("Tunda " + AlarmService.SNOOZE_MIN + " menit", "#374151");
        stop.setOnClickListener(v -> act(AlarmService.ACTION_STOP));
        snooze.setOnClickListener(v -> act(AlarmService.ACTION_SNOOZE));

        root.addView(icon); root.addView(clock); root.addView(title); root.addView(body);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, (int) (64 * getResources().getDisplayMetrics().density));
        lp.topMargin = pad;
        root.addView(stop, lp); root.addView(snooze, lp);
        setContentView(root);

        r = new BroadcastReceiver() { @Override public void onReceive(Context c, Intent i) { finish(); } };
        ContextCompat.registerReceiver(this, r, new IntentFilter(AlarmService.BROADCAST), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    void act(String action) {
        startService(new Intent(this, AlarmService.class).setAction(action).putExtra("id", id));
        finish();
    }

    TextView tv(String t, int sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(t == null ? "" : t); v.setTextSize(sp); v.setTextColor(color); v.setGravity(Gravity.CENTER);
        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setPadding(0, 12, 0, 12);
        return v;
    }

    Button btn(String t, String color) {
        Button b = new Button(this);
        b.setText(t); b.setTextColor(Color.WHITE); b.setTextSize(18); b.setAllCaps(false);
        b.setBackgroundColor(Color.parseColor(color));
        return b;
    }

    @Override protected void onDestroy() {
        try { unregisterReceiver(r); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
