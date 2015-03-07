package io.github.jason1114.lovenote.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;


import java.util.ArrayList;
import java.util.Iterator;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.db.AccountDBTask;
import io.github.jason1114.lovenote.file.FileLocationMethod;
import io.github.jason1114.lovenote.main.AccountActivity;
import io.github.jason1114.lovenote.main.MainNotesActivity;
import io.github.jason1114.lovenote.utils.AppEventAction;
import io.github.jason1114.lovenote.utils.BitmapDownloader;
import io.github.jason1114.lovenote.utils.GlobalContext;
import io.github.jason1114.lovenote.utils.Utility;

/**
 * User: qii
 * Date: 13-1-22
 *
 * left sliding menu
 *
 * homepage
 * logout
 */
public class LeftMenuFragment extends AbstractAppFragment {

    private Layout layout;

    private int currentIndex = -1;

//    private int mentionsWeiboUnreadCount = 0;
//    private int mentionsCommentUnreadCount = 0;
//    private int commentsToMeUnreadCount = 0;

    public int commentsTabIndex = -1;
    public int mentionsTabIndex = -1;
    public int searchTabIndex = -1;

    private boolean firstStart = true;

    private SparseArray<Fragment> rightFragments = new SparseArray<Fragment>();

    public static final int HOME_INDEX = 0;
    public static final int MENTIONS_INDEX = 1;
    public static final int COMMENTS_INDEX = 2;
    public static final int DM_INDEX = 3;
    public static final int FAV_INDEX = 4;
    public static final int SEARCH_INDEX = 5;
    public static final int PROFILE_INDEX = 6;
    public static final int LOGOUT_INDEX = 7;
    public static final int SETTING_INDEX = 8;

    public static LeftMenuFragment newInstance() {
        LeftMenuFragment fragment = new LeftMenuFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentIndex", currentIndex);
        outState.putBoolean("firstStart", firstStart);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("currentIndex");
//            mentionsWeiboUnreadCount = savedInstanceState.getInt("mentionsWeiboUnreadCount");
//            mentionsCommentUnreadCount = savedInstanceState.getInt("mentionsCommentUnreadCount");
//            commentsToMeUnreadCount = savedInstanceState.getInt("commentsToMeUnreadCount");
//            commentsTabIndex = savedInstanceState.getInt("commentsTabIndex");
//            mentionsTabIndex = savedInstanceState.getInt("mentionsTabIndex");
//            searchTabIndex = savedInstanceState.getInt("searchTabIndex");
            firstStart = savedInstanceState.getBoolean("firstStart");
        } else {
            // mention to me, comments that mention to me, comments to me
//            readUnreadCountFromDB();
        }
        if (currentIndex == -1) {
            currentIndex = HOME_INDEX;
        }

        rightFragments.append(HOME_INDEX,
                ((MainNotesActivity) getActivity()).getNoteListFragment());

        switchCategory(currentIndex);

        layout.nickname.setText(GlobalContext.getInstance().getCurrentAccountName());
        layout.avatar.setAdapter(new AvatarAdapter(layout.avatar));
    }

    public void switchCategory(int position) {
        switch (position) {
            case HOME_INDEX:
                showHomePage(true);
                break;
        }
        drawButtonsBackground(position);

        buildUnreadCount();

        firstStart = false;
    }

    private void buildUnreadCount() {
//        setMentionWeiboUnreadCount(mentionsWeiboUnreadCount);
//        setMentionCommentUnreadCount(mentionsCommentUnreadCount);
//        setCommentUnreadCount(commentsToMeUnreadCount);
    }

    private void showAccountSwitchPage() {
        Intent intent = AccountActivity.newIntent();
        startActivity(intent);
        getActivity().finish();
    }

//    private void showSettingPage() {
//        startActivity(new Intent(getActivity(), SettingActivity.class));
//    }

    private boolean showHomePage(boolean reset) {
        if (currentIndex == HOME_INDEX && !reset) {
            ((MainNotesActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }

        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        currentIndex = HOME_INDEX;

        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == HOME_INDEX) {
                        showHomePageImp();
                    }
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                    new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showHomePageImp();
        }

        ((MainNotesActivity) getActivity()).getSlidingMenu().showContent();

        return false;
    }

    private void showHomePageImp() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

//        ft.hide(rightFragments.get(MENTIONS_INDEX));
//        ft.hide(rightFragments.get(COMMENTS_INDEX));
//        ft.hide(rightFragments.get(SEARCH_INDEX));
//        ft.hide(rightFragments.get(DM_INDEX));
//        ft.hide(rightFragments.get(FAV_INDEX));
//        ft.hide(rightFragments.get(PROFILE_INDEX));

        NoteListFragment fragment = (NoteListFragment) rightFragments.get(HOME_INDEX);
        ft.show(fragment);
        ft.commit();
//        fragment.buildActionBarNav();
    }



    public int getCurrentIndex() {
        return currentIndex;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final ScrollView view = (ScrollView) inflater
                .inflate(R.layout.slidingdrawer_contents, container, false);

        layout = new Layout();

        layout.avatar = (Spinner) view.findViewById(R.id.avatar);
        layout.nickname = (TextView) view.findViewById(R.id.nickname);

        layout.home = (LinearLayout) view.findViewById(R.id.btn_home);
        layout.logout = (Button) view.findViewById(R.id.btn_logout);
        layout.homeCount = (TextView) view.findViewById(R.id.tv_home_count);

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.home.setOnClickListener(onClickListener);
        layout.logout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_home:
                    showHomePage(false);
                    drawButtonsBackground(HOME_INDEX);
                    break;
                case R.id.btn_logout:
                    showAccountSwitchPage();
                    break;
            }
        }
    };

    private void drawButtonsBackground(int position) {
        layout.home.setBackgroundResource(R.drawable.btn_drawer_menu);
        switch (position) {
            case HOME_INDEX:
                layout.home.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case LOGOUT_INDEX:
                layout.logout.setBackgroundResource(R.color.ics_blue_semi);
                break;
        }
    }

    private SlidingMenu getSlidingMenu() {
        return ((MainNotesActivity) getActivity()).getSlidingMenu();
    }

    private void setTitle(int res) {
        ((MainNotesActivity) getActivity()).setTitle(res);
    }

    private void setTitle(String title) {
        ((MainNotesActivity) getActivity()).setTitle(title);
    }

    public void setHomeUnreadCount(int count) {
        if (count > 0) {
            layout.homeCount.setVisibility(View.VISIBLE);
            layout.homeCount.setText(String.valueOf(count));
        } else {
            layout.homeCount.setVisibility(View.GONE);
        }
    }


    private class AvatarAdapter extends BaseAdapter {

        ArrayList<AccountBean> data = new ArrayList<AccountBean>();
        int count = 0;

        public AvatarAdapter(Spinner spinner) {
            data.addAll(AccountDBTask.getAccountList());
            if (data.size() == 1) {
                count = 1;
            } else {
                count = data.size() - 1;
            }
            Iterator<AccountBean> iterator = data.iterator();
            while (iterator.hasNext()) {
                AccountBean accountBean = iterator.next();
                if (accountBean.getUid()
                        .equals(GlobalContext.getInstance().getAccountBean().getUid())) {
                    iterator.remove();
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater(null)
                    .inflate(R.layout.slidingdrawer_avatar, parent, false);
            ImageView iv = (ImageView) view.findViewById(R.id.avatar);
            BitmapDownloader.getInstance().display(iv, -1, -1,
                    GlobalContext.getInstance().getAccountBean().getInfo().getAvatar_large(),
                    FileLocationMethod.avatar_large);

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater(null)
                    .inflate(R.layout.slidingdrawer_avatar_dropdown, parent, false);
            TextView nickname = (TextView) view.findViewById(R.id.nickname);
            ImageView avatar = (ImageView) view.findViewById(R.id.avatar);

            if (data.size() > 0) {
                final AccountBean accountBean = data.get(position);
                BitmapDownloader.getInstance()
                        .display(avatar, -1, -1, accountBean.getInfo().getAvatar_large(),
                                FileLocationMethod.avatar_large);

                nickname.setText(accountBean.getUserNick());

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent start = MainNotesActivity.newIntent(accountBean);
                        getActivity().startActivity(start);
                        getActivity().finish();
                    }
                });
            } else {
                avatar.setVisibility(View.GONE);
                nickname.setTextColor(getResources().getColor(R.color.gray));
                nickname.setText(getString(R.string.dont_have_other_account));
            }
            return view;
        }
    }

    private class Layout {
        Spinner avatar;
        TextView nickname;
        LinearLayout home;
        TextView homeCount;
        Button logout;
    }
}