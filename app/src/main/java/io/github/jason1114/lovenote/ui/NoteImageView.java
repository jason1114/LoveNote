package io.github.jason1114.lovenote.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.UserBean;
import io.github.jason1114.lovenote.utils.IWeiciyuanDrawable;


/**
 * User: qii
 * Date: 12-12-18
 * todo
 * this class and its child class need to be refactored
 */
public class NoteImageView extends FrameLayout implements IWeiciyuanDrawable {
    private Paint paint = new Paint();

    protected ImageView mImageView;
    private ProgressBar pb;
    private boolean parentPressState = true;

    public NoteImageView(Context context) {
        this(context, null);
    }

    public NoteImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(context);
    }

    protected void initLayout(Context context) {
//        gif = BitmapFactory.decodeResource(getResources(), R.drawable.ic_play_gif);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.timelineimageview_layout, this, true);
        mImageView = (ImageView) v.findViewById(R.id.imageview);
        mImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        pb = (ProgressBar) v.findViewById(R.id.imageview_pb);
        this.setForeground(getResources().getDrawable(R.drawable.timelineimageview_cover));
        this.setAddStatesFromChildren(true);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bm) {
        mImageView.setImageBitmap(bm);
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public void setProgress(int value, int max) {
        pb.setVisibility(View.VISIBLE);
        pb.setMax(max);
        pb.setProgress(value);
    }

    public ProgressBar getProgressBar() {
        return pb;
    }

    public void setGifFlag(boolean value) {
    }

    @Override
    public void checkVerified(UserBean user) {

    }

    @Override
    public void setPressesStateVisibility(boolean value) {
        if (parentPressState == value) {
            return;
        }
        setForeground(
                value ? getResources().getDrawable(R.drawable.timelineimageview_cover) : null);
        parentPressState = value;
    }
}


