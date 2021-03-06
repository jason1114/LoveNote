package io.github.jason1114.lovenote.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * User: qii
 * Date: 14-6-10
 */
public abstract class AbstractWorker<Params, Progress, Result>
        extends MyAsyncTask<Params, Progress, Result>
        implements IPictureWorker {

    protected boolean isMySelf(ImageView view) {
        if (view != null) {
            IPictureWorker bitmapDownloaderTask = getBitmapDownloaderTask(view);
            if (this == bitmapDownloaderTask) {
                return true;
            }
        }
        return false;
    }

    private IPictureWorker getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PictureBitmapDrawable) {
                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
}
