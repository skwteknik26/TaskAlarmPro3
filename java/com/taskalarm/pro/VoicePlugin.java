package com.taskalarm.pro;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.ArrayList;

@CapacitorPlugin(name = "VoiceInput")
public class VoicePlugin extends Plugin {

    @PluginMethod
    public void listen(PluginCall call) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, call.getString("lang", "id-ID"));
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan bicara…");
        try { startActivityForResult(call, i, "onResult"); }
        catch (ActivityNotFoundException e) { call.reject("Layanan suara tidak tersedia"); }
    }

    @ActivityCallback
    private void onResult(PluginCall call, ActivityResult r) {
        if (call == null) return;
        if (r.getResultCode() == Activity.RESULT_OK && r.getData() != null) {
            ArrayList<String> l = r.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (l != null && !l.isEmpty()) {
                JSObject o = new JSObject();
                o.put("text", l.get(0));
                call.resolve(o);
                return;
            }
        }
        call.reject("cancelled");
    }
}
