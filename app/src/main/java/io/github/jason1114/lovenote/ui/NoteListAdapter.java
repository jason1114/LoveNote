package io.github.jason1114.lovenote.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.MessageBean;
import io.github.jason1114.lovenote.bean.UserBean;
import io.github.jason1114.lovenote.main.NoteListFragment;
import io.github.jason1114.lovenote.utils.BitmapDownloader;
import io.github.jason1114.lovenote.utils.IWeiciyuanDrawable;
import io.github.jason1114.lovenote.utils.SettingUtility;
import io.github.jason1114.lovenote.utils.ThemeUtility;
import io.github.jason1114.lovenote.utils.Utility;
import io.github.jason1114.lovenote.utils.ViewUtility;

/**
 * Created by Jason on 2015/2/25.
 */
public class NoteListAdapter extends BaseAdapter {
    List<MessageBean> mData = new ArrayList<>();
    Fragment mFragment;
    LayoutInflater mInflater;
    ListView mListView;

    protected int checkedBG;
    protected int defaultBG;
//    private LongSparseArray<Integer> msgHeights = new LongSparseArray<>();
//    private LongSparseArray<Integer> msgWidths = new LongSparseArray<>();
    private Map<String, Integer> msgHeights = new HashMap<>(),msgWidths = new HashMap<>();


    private WeakHashMap<ViewHolder, Drawable> bg = new WeakHashMap<>();

    public NoteListAdapter(Fragment fragment, ListView listView) {
        mFragment = fragment;
        mListView = listView;
        mInflater = fragment.getActivity().getLayoutInflater();
        defaultBG = fragment.getResources().getColor(R.color.transparent);
        checkedBG = ThemeUtility.getColor(R.attr.listview_checked_color);
    }


    public void addMessage(MessageBean message) {
        long timestamp = message.getMills();
        if (mData.size() == 0) {
            mData.add(message);
            return;
        }
        for (int i=0; i<mData.size(); i++) {
            if (timestamp>=mData.get(i).getMills()) {
                mData.add(i, message);
                break;
            }
        }
    }

    public void removeMessage(MessageBean message) {
        for (int i=0; i<mData.size(); i++) {
            if (message.getId().equals(mData.get(i).getId())) {
                mData.remove(i);
            }
        }
    }

    public void changeMessage (MessageBean message) {
        for (int i=0; i<mData.size(); i++) {
            if (message.getId().equals(mData.get(i).getId())) {
                mData.set(i, message);
            }
        }
    }

    public static class ViewHolder {
        TextView username;
        TextView content;
        TimeTextView time;
        IWeiciyuanDrawable avatar;
        IWeiciyuanDrawable content_pic;
        GridLayout content_pic_multi;
        ViewGroup listview_root;
    }

    //weibo image widgets and its forward weibo image widgets are the same
    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = ViewUtility.findViewById(convertView, R.id.username);
        TextPaint tp = holder.username.getPaint();
        if (tp != null) {
            tp.setFakeBoldText(true);
        }
        holder.content = ViewUtility.findViewById(convertView, R.id.content);
        holder.content_pic = (IWeiciyuanDrawable)ViewUtility.findViewById(convertView, R.id.content_pic);
        holder.content_pic_multi = ViewUtility.findViewById(convertView, R.id.content_pic_multi);
        holder.time = ViewUtility.findViewById(convertView, R.id.time);
        holder.avatar = (TimeLineAvatarImageView) convertView.findViewById(R.id.avatar);
        holder.listview_root = ViewUtility.findViewById(convertView, R.id.listview_root);
        return holder;
    }

    private void configViewFont(ViewHolder holder) {
        int prefFontSizeSp = SettingUtility.getFontSize();
        float currentWidgetTextSizePx;

        currentWidgetTextSizePx = holder.time.getTextSize();

        if (Utility.sp2px(prefFontSizeSp - 3) != currentWidgetTextSizePx) {
            holder.time.setTextSize(prefFontSizeSp - 3);
        }

        currentWidgetTextSizePx = holder.content.getTextSize();

        if (Utility.sp2px(prefFontSizeSp) != currentWidgetTextSizePx) {
            holder.content.setTextSize(prefFontSizeSp);
            holder.username.setTextSize(prefFontSizeSp);
        }
    }

    protected void buildAvatar(IWeiciyuanDrawable view, int position, final UserBean user) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
//                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
//                intent.putExtra("user", user);
//                getActivity().startActivity(intent);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                UserDialog dialog = new UserDialog(user);
//                dialog.show(fragment.getFragmentManager(), "");
                return true;
            }
        });
        view.checkVerified(user);
        buildAvatar(view.getImageView(), position, user);
    }

    protected void buildAvatar(ImageView view, int position, final UserBean user) {
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            view.setVisibility(View.VISIBLE);
            BitmapDownloader.getInstance()
                    .downloadAvatar(view, user, (NoteListFragment) mFragment);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    protected void bindViewData(final ViewHolder holder, int position) {
        Drawable drawable = bg.get(holder);
        if (drawable != null) {
            holder.listview_root.setBackgroundDrawable(drawable);
        } else {
            drawable = holder.listview_root.getBackground();
            bg.put(holder, drawable);
        }

        if (mListView.getCheckedItemPosition() == position + mListView.getHeaderViewsCount()) {
            holder.listview_root.setBackgroundColor(checkedBG);
        }

        final MessageBean msg = mData.get(position);

        UserBean user = msg.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getRemark())) {
                holder.username.setText(new StringBuilder(user.getScreen_name()).append("(")
                        .append(user.getRemark()).append(")").toString());
            } else {
                holder.username.setText(user.getScreen_name());
            }
            buildAvatar(holder.avatar, position, user);
        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(msg.getListViewSpannableString())) {
            boolean haveCachedHeight = msgHeights.get(msg.getId()) != null;
            ViewGroup.LayoutParams layoutParams = holder.content.getLayoutParams();
            if (haveCachedHeight) {
                layoutParams.height = msgHeights.get(msg.getId());
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            boolean haveCachedWidth = msgWidths.get(msg.getId()) != null;
            if (haveCachedWidth) {
                layoutParams.width = msgWidths.get(msg.getId());
            } else {
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            holder.content.requestLayout();
            holder.content.setText(msg.getListViewSpannableString());
            if (!haveCachedHeight) {
                msgHeights.put(msg.getId(), layoutParams.height);
            }

            if (!haveCachedWidth) {
                msgWidths.put(msg.getId(), layoutParams.width);
            }
        } else {
//            TimeLineUtility.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }

        holder.time.setTime(msg.getMills());

        holder.content_pic.setVisibility(View.GONE);
        holder.content_pic_multi.setVisibility(View.GONE);

        if (msg.havePicture()) {
            if (msg.isMultiPics()) {
//                buildMultiPic(msg, holder.content_pic_multi);
            } else {
//                buildPic(msg, holder.content_pic, position);
            }
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null
                || convertView.getTag(R.drawable.ic_launcher) == null) {
            View view = mInflater.inflate(R.layout.note_listview_item_layout, parent, false);
            convertView = view;
            holder = buildHolder(convertView);
            convertView.setTag(R.drawable.ic_launcher, holder);
        } else {
            holder = (ViewHolder) convertView
                    .getTag(R.drawable.ic_launcher);
        }
        configViewFont(holder);
        bindViewData(holder, position);
        return convertView;
    }
}
