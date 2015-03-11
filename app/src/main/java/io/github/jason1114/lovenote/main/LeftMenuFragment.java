package io.github.jason1114.lovenote.main;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.db.AccountDBTask;
import io.github.jason1114.lovenote.file.FileLocationMethod;
import io.github.jason1114.lovenote.ui.AbstractAppFragment;
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

    private Page currentIndex = Page.values()[0];

//    private int mentionsWeiboUnreadCount = 0;
//    private int mentionsCommentUnreadCount = 0;
//    private int commentsToMeUnreadCount = 0;

    public int commentsTabIndex = -1;
    public int mentionsTabIndex = -1;
    public int searchTabIndex = -1;

    private boolean firstStart = true;

    private SparseArray<Fragment> rightFragments = new SparseArray<Fragment>();
    public static enum Page {
        HOME_INDEX,
        RELATION_INDEX,
        LOGOUT_INDEX
    }
    private static Map<Page,Integer> pageIndex;
    static {
        pageIndex = new HashMap<>();
        Page[] pages = Page.values();
        for (int i=0;i<pages.length;i++) {
            pageIndex.put(pages[i], i);
        }
    }

    public static LeftMenuFragment newInstance() {
        LeftMenuFragment fragment = new LeftMenuFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("currentIndex", currentIndex);
        outState.putBoolean("firstStart", firstStart);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentIndex = (Page) savedInstanceState.getSerializable("currentIndex");
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

        rightFragments.append(pageIndex.get(Page.HOME_INDEX),
                ((MainNotesActivity) getActivity()).getNoteListFragment());
        rightFragments.append(pageIndex.get(Page.RELATION_INDEX),
                ((MainNotesActivity) getActivity()).getRelationFragment());

        switchCategory(currentIndex);

        layout.nickname.setText(GlobalContext.getInstance().getCurrentAccountName());
        layout.avatar.setAdapter(new AvatarAdapter(layout.avatar));
    }

    public void switchCategory(Page position) {
        showPage(position,true);
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

    private void showPage(final Page target, boolean reset) {
        if (currentIndex == target && !reset) {
            ((MainNotesActivity) getActivity()).getSlidingMenu().showContent();
            return;
        }
        //sliding menu is open, so make the touch mode fullscreen
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        currentIndex = target;
        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == target) {
                        showCurrentPageAndHideOthers(target);
                    }
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                    new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showCurrentPageAndHideOthers(target);
        }
        ((MainNotesActivity) getActivity()).getSlidingMenu().showContent();
    }


    private void showCurrentPageAndHideOthers(Page current) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Page[] pages = Page.values();
        for (int i=0; i< pages.length; i++) {
            Fragment fragment = rightFragments.get(pageIndex.get(pages[i]));
            if (pages[i] != current && fragment != null) {
                ft.hide(fragment);
            }
        }
        ft.show(rightFragments.get(pageIndex.get(current)));
        ft.commit();
    }



    public Page getCurrentIndex() {
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
        layout.homeCount = (TextView) view.findViewById(R.id.tv_home_count);

        layout.relation = (Button) view.findViewById(R.id.btn_relation);
        layout.logout = (Button) view.findViewById(R.id.btn_logout);

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.home.setOnClickListener(onClickListener);
        layout.logout.setOnClickListener(onClickListener);
        layout.relation.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_home:
                    switchCategory(Page.HOME_INDEX);
                    break;
                case R.id.btn_relation:
                    switchCategory(Page.RELATION_INDEX);
                    break;
                case R.id.btn_logout:
                    showAccountSwitchPage();
                    break;
            }
        }
    };

    private void drawButtonsBackground(Page position) {
        layout.home.setBackgroundResource(R.drawable.btn_drawer_menu);
        layout.relation.setBackgroundResource(R.drawable.btn_drawer_menu);
        layout.logout.setBackgroundResource(R.drawable.btn_drawer_menu);
        switch (position) {
            case HOME_INDEX:
                layout.home.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case RELATION_INDEX:
                layout.relation.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case LOGOUT_INDEX:
                layout.logout.setBackgroundResource(R.color.ics_blue_semi);
                break;
        }
    }

    private SlidingMenu getSlidingMenu() {
        return ((MainNotesActivity) getActivity()).getSlidingMenu();
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

        Button relation;
        Button logout;
    }
}