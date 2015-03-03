package io.github.jason1114.lovenote.network;


import java.util.Map;

import io.github.jason1114.lovenote.file.FileDownloaderHttpHelper;

public class HttpUtility {

    private static HttpUtility httpUtility = new HttpUtility();

    private HttpUtility() {
    }

    public static HttpUtility getInstance() {
        return httpUtility;
    }

    public String executeNormalTask(HttpMethod httpMethod, String url, Map<String, String> param)
            throws Exception {
        return new JavaHttpUtility().executeNormalTask(httpMethod, url, param);
    }

    public boolean executeDownloadTask(String url, String path,
                                       FileDownloaderHttpHelper.DownloadListener downloadListener) {
        return !Thread.currentThread().isInterrupted() && new JavaHttpUtility()
                .doGetSaveFile(url, path, downloadListener);
    }
}

