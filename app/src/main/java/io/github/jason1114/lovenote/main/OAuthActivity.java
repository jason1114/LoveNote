package io.github.jason1114.lovenote.main;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.bean.UserBean;
import io.github.jason1114.lovenote.db.AccountDBTask;
import io.github.jason1114.lovenote.network.HttpMethod;
import io.github.jason1114.lovenote.network.HttpUtility;
import io.github.jason1114.lovenote.ui.AbstractAppActivity;
import io.github.jason1114.lovenote.utils.AppLogger;
import io.github.jason1114.lovenote.utils.MyAsyncTask;
import io.github.jason1114.lovenote.utils.OAuthDao;
import io.github.jason1114.lovenote.utils.URLHelper;
import io.github.jason1114.lovenote.utils.Utility;

/**
 * User: qii
 * Date: 12-7-28
 */
public class OAuthActivity extends AbstractAppActivity {

    private WebView webView;
    private MenuItem refreshItem;
    private GetAccessTokenTask taskToGetAccessToken;

    private String codeLastTime;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauthactivity_layout);
        taskToGetAccessToken = new GetAccessTokenTask(this);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.login));
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WeiboWebViewClient());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        webView.clearCache(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_oauthactivity, menu);
        refreshItem = menu.findItem(R.id.menu_refresh);
        refresh();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = AccountActivity.newIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void refresh() {
        webView.clearView();
        webView.loadUrl("about:blank");
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
        webView.loadUrl(getWeiboOAuthUrl());
    }

    private void completeRefresh() {
        if (refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }

    private String getWeiboOAuthUrl() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("client_id", URLHelper.APP_KEY);
        parameters.put("response_type", "code");
        parameters.put("redirect_uri", URLHelper.DIRECT_URL);
        parameters.put("display", "mobile");
        return URLHelper.URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
//                + "&scope=friendships_groups_read,friendships_groups_write";
    }

    private static Map<String,String> getAccessTokenParameter(String code) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("client_id", URLHelper.APP_KEY);
        parameters.put("client_secret", URLHelper.APP_SECRET);
        parameters.put("redirect_uri", URLHelper.DIRECT_URL);
        parameters.put("grant_type", "authorization_code");
        parameters.put("code", code);
        return parameters;
    }

    private class WeiboWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.startsWith(URLHelper.DIRECT_URL)) {
                handleRedirectUrl(view, url);
                view.stopLoading();
                return;
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            new SinaWeiboErrorDialog().show(getSupportFragmentManager(), "");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!url.equals("about:blank")) {
                completeRefresh();
            }
        }
    }

    private void handleAccessTokenUrl(String code) {
        try {
            if (code != null && !code.equals(codeLastTime)) {
                taskToGetAccessToken.execute(code);
                codeLastTime = code;
            }
        } catch (Exception e) {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void handleRedirectUrl(WebView view, String url) {
        Bundle values = Utility.parseUrl(url);
        String error = values.getString("error");
        String error_code = values.getString("error_code");
        String code = values.getString("code");
        Intent intent = new Intent();
        intent.putExtras(values);

        if (error == null && error_code == null && !TextUtils.isEmpty(code)) {
            handleAccessTokenUrl(code);
        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private static class OAuthTask extends MyAsyncTask<String, UserBean, DBResult> {

        private Exception e;
        private ProgressFragment progressFragment = ProgressFragment.newInstance();
        private WeakReference<OAuthActivity> oAuthActivityWeakReference;

        private OAuthTask(OAuthActivity activity) {
            oAuthActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            progressFragment.setAsyncTask(this);
            OAuthActivity activity = oAuthActivityWeakReference.get();
            if (activity != null) {
                progressFragment.show(activity.getSupportFragmentManager(), "");
            }
        }

        @Override
        protected DBResult doInBackground(String... params) {
            String token = params[0];
            long expiresInSeconds = Long.valueOf(params[1]);

            try {
                UserBean user = new OAuthDao(token).getOAuthUserInfo();
                AccountBean account = new AccountBean();
                account.setAccessToken(token);
                account.setExpiresTime(System.currentTimeMillis() + expiresInSeconds * 1000);
                account.setInfo(user);
                AppLogger
                        .e("token expires in " + Utility.calcTokenExpiresInDays(account) + " days");
                return AccountDBTask.addOrUpdateAccount(account, false);
            } catch (Exception e) {
                AppLogger.e(e.getLocalizedMessage());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(DBResult dbResult) {
            super.onCancelled(dbResult);
            if (progressFragment != null) {
                progressFragment.dismissAllowingStateLoss();
            }

            OAuthActivity activity = oAuthActivityWeakReference.get();
            if (activity == null) {
                return;
            }

            if (e != null) {
                Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            activity.webView.loadUrl(activity.getWeiboOAuthUrl());
        }

        @Override
        protected void onPostExecute(DBResult dbResult) {
            if (progressFragment.isVisible()) {
                progressFragment.dismissAllowingStateLoss();
            }
            OAuthActivity activity = oAuthActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            switch (dbResult) {
                case add_successfuly:
                    Toast.makeText(activity, activity.getString(R.string.login_success),
                            Toast.LENGTH_SHORT).show();
                    break;
                case update_successfully:
                    Toast.makeText(activity, activity.getString(R.string.update_account_success),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            activity.finish();
        }
    }

    private static class GetAccessTokenTask extends MyAsyncTask<String, UserBean, Bundle>{
        private WeakReference<OAuthActivity> oAuthActivityWeakReference;

        private GetAccessTokenTask(OAuthActivity activity) {
            oAuthActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Bundle doInBackground(String... params){
            String code = params[0];
            Map<String,String> postParams = getAccessTokenParameter(code);
            try {
                String requestUrl = URLHelper.URL_OAUTH2_ACCESS_TOKEN + "?" + Utility.encodeUrl(postParams);
                String content = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, requestUrl, postParams);
                JSONObject json = new JSONObject(content);
                String accessToken = json.getString("access_token");
                String expiresTime = json.getString("expires_in");
                Bundle values = new Bundle();
                values.putString("access_token", accessToken);
                values.putString("expires_in", expiresTime);
                Intent intent = new Intent();
                intent.putExtras(values);
                oAuthActivityWeakReference.get().setResult(RESULT_OK, intent);
                new OAuthTask(oAuthActivityWeakReference.get()).execute(accessToken, expiresTime);
                return values;
            } catch (Exception e) {
                AppLogger.e(e.getLocalizedMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            super.onPostExecute(bundle);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            webView.stopLoading();
        }
    }

    public static class ProgressFragment extends DialogFragment {

        MyAsyncTask asyncTask = null;

        public static ProgressFragment newInstance() {
            ProgressFragment frag = new ProgressFragment();
            frag.setRetainInstance(true);
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.oauthing));
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (asyncTask != null) {
                asyncTask.cancel(true);
            }
            super.onCancel(dialog);
        }

        void setAsyncTask(MyAsyncTask task) {
            asyncTask = task;
        }
    }

    public static class SinaWeiboErrorDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.sina_server_error)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            return builder.create();
        }
    }

    public static enum DBResult {
        add_successfuly, update_successfully
    }
}
