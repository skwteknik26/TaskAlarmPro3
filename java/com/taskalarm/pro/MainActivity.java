package com.taskalarm.pro;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AlarmPlugin.class);   // wajib sebelum super.onCreate
        registerPlugin(VoicePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
