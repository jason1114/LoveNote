package io.github.jason1114.lovenote.main;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.json.JSONObject;

import io.github.jason1114.lovenote.network.HttpMethod;
import io.github.jason1114.lovenote.network.HttpUtility;

public class UpdateCheckService extends Service {

    private static final String URL_RELEASE_DESCRIBE = "https://raw.githubusercontent.com/jason1114/LoveNote/master/release/release.json";

    public UpdateCheckService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // version check is only done once
        try {
            String content = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, URL_RELEASE_DESCRIBE, null);
            JSONObject json = new JSONObject(content);
        } catch (Exception ignored){
            // version checking is a background task, it's ok if it fails.
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
