package io.github.jason1114.lovenote.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

import io.github.jason1114.lovenote.BuildConfig;
import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.network.HttpMethod;
import io.github.jason1114.lovenote.network.HttpUtility;
import io.github.jason1114.lovenote.utils.AppLogger;
import io.github.jason1114.lovenote.utils.GlobalContext;
import io.github.jason1114.lovenote.utils.MyAsyncTask;

public class UpdateCheckService extends Service {

    private static final String URL_RELEASE_DESCRIBE =
            "https://raw.githubusercontent.com/jason1114/LoveNote/master/release/release.json";
    private static final String URL_APK_DOWNLOAD_PREFIX =
            "https://raw.githubusercontent.com/jason1114/LoveNote/master/release/";
    private static final String FIELD_VERSION_NAME = "version_name";
    private static final String FIELD_RELEASE_INFO = "release_info";
    private static final String FIELD_APK = "apk";

    private CheckUpdateTask task = new CheckUpdateTask();
    private DownloadManager dm;
    private long enqueue;

    private BroadcastReceiver receiver = new DownloadSuccessReceiver();

    public UpdateCheckService() {
    }



    @Override
    public void onCreate() {
        super.onCreate();
        // version check is only done once
        task.execute(null, null, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void openApkToInstall(String uriString) {
        Intent intentToInstall = new Intent(Intent.ACTION_VIEW);
        intentToInstall
                .setDataAndType(Uri.
                        parse(uriString), "application/vnd.android.package-archive");
        intentToInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentToInstall);
    }

    public void openApkToInstall(File apkFile) {
        apkFile.setReadable(true, false);
        Intent intentToInstall = new Intent(Intent.ACTION_VIEW);
        intentToInstall
                .setDataAndType(Uri.
                        fromFile(apkFile), "application/vnd.android.package-archive");
        intentToInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentToInstall);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class CheckUpdateTask extends MyAsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            try {
                String content = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, URL_RELEASE_DESCRIBE, null);
                JSONObject json = new JSONObject(content);
                Iterator<String> it = json.keys();
                int newestVersion = BuildConfig.VERSION_CODE;
                while (it.hasNext()) {
                    int versionCode = Integer.valueOf(it.next());
                    if (versionCode > newestVersion) {
                        newestVersion = versionCode;
                    }
                }
                if (newestVersion > BuildConfig.VERSION_CODE) {
                    // then we got a new version to download
                    JSONObject newVersionObj = json.getJSONObject(String.valueOf(newestVersion));
                    final String releaseInfo = newVersionObj.getString(FIELD_RELEASE_INFO);
                    final String versionName = newVersionObj.getString(FIELD_VERSION_NAME);
                    final String apk = newVersionObj.getString(FIELD_APK);
                    File downloadDir =
                            Environment.
                                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    final File distFile = new File(downloadDir, apk);
                    final Activity activity = GlobalContext.getInstance().getActivity();

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateDialogPositiveButtonClickCallback
                                    clickCallback = new UpdateDialogPositiveButtonClickCallback(apk,distFile);
                            new AlertDialog.Builder(activity)
                                    .setTitle(getString(R.string.new_version_detected) + " " + versionName)
                                    .setMessage(releaseInfo)
                                    .setPositiveButton(R.string.update_now,clickCallback)
                                    .setNegativeButton(R.string.remind_me_later, null)
                                    .show();
                        }
                    });
                }
            } catch (Exception ignored){
                AppLogger.e("Error in checking new version", ignored);
                // version checking is a background task, it's ok if it fails.
            }
            return null;
        }
    }

    class UpdateDialogPositiveButtonClickCallback implements DialogInterface.OnClickListener {

        private File distFile;
        private String apk;

        public UpdateDialogPositiveButtonClickCallback(String apk, File distFile) {
            this.apk = apk;
            this.distFile = distFile;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (distFile.exists()) {
                openApkToInstall(distFile);
                return;
            }
            Toast.makeText(GlobalContext.getInstance(), R.string.downloading_apk_now, Toast.LENGTH_LONG)
                    .show();
            registerReceiver(receiver, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(URL_APK_DOWNLOAD_PREFIX+apk));

            request.
                    setNotificationVisibility
                            (DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apk);
            enqueue = dm.enqueue(request);
        }
    }

    class DownloadSuccessReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enqueue);
                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c
                            .getInt(columnIndex)) {

                        String uriString = c
                                .getString(c
                                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        openApkToInstall(uriString);
                    }
                }
            }
        }
    };
}
