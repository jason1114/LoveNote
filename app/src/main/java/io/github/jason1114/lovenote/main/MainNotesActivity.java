package io.github.jason1114.lovenote.main;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;

import io.github.jason1114.lovenote.R;
import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.bean.UserBean;
import io.github.jason1114.lovenote.main.LeftMenuFragment;
import io.github.jason1114.lovenote.ui.MainNotesParentActivity;
import io.github.jason1114.lovenote.main.NoteListFragment;
import io.github.jason1114.lovenote.utils.AppEventAction;
import io.github.jason1114.lovenote.utils.BundleArgsConstants;
import io.github.jason1114.lovenote.utils.GlobalContext;
import io.github.jason1114.lovenote.utils.SettingUtility;
import io.github.jason1114.lovenote.utils.Utility;


/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public class MainNotesActivity extends MainNotesParentActivity {

    public static final int REQUEST_CODE_UPDATE_FRIENDS_TIMELINE_COMMENT_REPOST_COUNT = 0;
    public static final int REQUEST_CODE_UPDATE_MENTIONS_WEIBO_TIMELINE_COMMENT_REPOST_COUNT = 1;
    public static final int REQUEST_CODE_UPDATE_MY_FAV_TIMELINE_COMMENT_REPOST_COUNT = 2;

    private AccountBean accountBean;

    private ScrollableListFragment currentFragment;


    public static interface ScrollableListFragment {
        public void scrollToTop();
    }

    public static Intent newIntent() {
        return new Intent(GlobalContext.getInstance(), MainNotesActivity.class);
    }

    public static Intent newIntent(AccountBean accountBean) {
        Intent intent = newIntent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
        return intent;
    }

    /*
      notification bar
     */
//    public static Intent newIntent(AccountBean accountBean, MessageListBean mentionsWeiboData,
//            CommentListBean mentionsCommentData, CommentListBean commentsToMeData,
//            UnreadBean unreadBean) {
//        Intent intent = newIntent();
//        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
//        intent.putExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA, mentionsWeiboData);
//        intent.putExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA, mentionsCommentData);
//        intent.putExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA, commentsToMeData);
//        intent.putExtra(BundleArgsConstants.UNREAD_EXTRA, unreadBean);
//        return intent;
//    }

    public String getToken() {
        return accountBean.getAccessToken();
    }

//    public void setTitle(String title) {
//        if (TextUtils.isEmpty(title)) {
//            titleText.setVisibility(View.GONE);
//        } else {
//            titleText.setText(title);
//            titleText.setVisibility(View.VISIBLE);
//        }
//    }
//
//    public void setTitle(int res) {
//        setTitle(getString(res));
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // the activity was killed before, so the state is saved via #onSaveInstanceState(Bundle)
            accountBean = savedInstanceState.getParcelable(BundleArgsConstants.ACCOUNT_EXTRA);
        } else {
            // the activity first run, it must has a intent with an account
            Intent intent = getIntent();
            accountBean = intent
                    .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        }

        if (accountBean == null) {
            accountBean = GlobalContext.getInstance().getAccountBean();
        }

//        GlobalContext.getInstance().setGroup(null);
        GlobalContext.getInstance().setAccountBean(accountBean);
        SettingUtility.setDefaultAccountId(accountBean.getUid());

        buildInterface(savedInstanceState);
    }

    //build phone ui or table ui
    private void buildInterface(Bundle savedInstanceState) {
        getActionBar().setTitle(GlobalContext.getInstance().getCurrentAccountName());
        getWindow().setBackgroundDrawable(null);
        // phone and pad will apply different 'menu_right' layout
        // the phone edition layout has no element with id 'menu_frame'
        setContentView(R.layout.menu_right);
        boolean phone = findViewById(R.id.menu_frame) == null;
        // set up sliding menu
        if (phone) {
            // default phone view has no 'menu_frame', set 'menu_frame' as behind content view
            buildPhoneInterface(savedInstanceState);
        } else {
            buildPadInterface(savedInstanceState);
        }

        if (savedInstanceState == null) {
            // get a fragment transaction, add all fragments then hide all of them
            // including friend fragment
            initFragments();
            FragmentTransaction secondFragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            secondFragmentTransaction
                    .replace(R.id.menu_frame, getMenuFragment(), LeftMenuFragment.class.getName());
            getSlidingMenu().showContent();
            secondFragmentTransaction.commit();
        }
        configSlidingMenu(phone);
    }

    //init fragments
    private void initFragments() {
        Fragment noteList = getNoteListFragment();
        Fragment relation = getRelationFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        addAndHide(fragmentTransaction, noteList);
        addAndHide(fragmentTransaction, relation);
        if (!fragmentTransaction.isEmpty()) {
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void addAndHide(FragmentTransaction fragmentTransaction, Fragment fragment) {
        if (!fragment.isAdded()) {
            fragmentTransaction
                    .add(R.id.menu_right_fl, fragment, fragment.getClass().getName());
            fragmentTransaction.hide(fragment);
        }
    }

    //configure left menu
    private void configSlidingMenu(boolean phone) {
        SlidingMenu slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow_slidingmenu);
        if (phone) {
            slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        } else {
            slidingMenu.setBehindOffset(Utility.getScreenWidth());
        }

        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setOnPageScrollListener(new SlidingMenu.OnPageScrollListener() {
            @Override
            public void onPageScroll() {
//                LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
//                (getFriendsTimeLineFragment()).clearActionMode();
//                (getFavFragment()).clearActionMode();
//                (getCommentsTimeLineFragment()).clearActionMode();
//                (getMentionsTimeLineFragment()).clearActionMode();
//                (getMyProfileFragment()).clearActionMode();
//
//                if (GlobalContext.getInstance().getAccountBean().isBlack_magic()) {
//                    (getSearchFragment()).clearActionMode();
//                    (getDMFragment()).clearActionMode();
//                }
            }
        });

        slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                LocalBroadcastManager.getInstance(MainNotesActivity.this)
                        .sendBroadcast(new Intent(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
            }
        });
    }



    private void buildPhoneInterface(Bundle savedInstanceState) {
        setBehindContentView(R.layout.menu_frame);
        getSlidingMenu().setSlidingEnabled(true);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        // icon '< ' in the actionbar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getSlidingMenu().setMode(SlidingMenu.LEFT);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    }

    private void buildPadInterface(Bundle savedInstanceState) {
        View v = new View(this);
        setBehindContentView(v);
        getSlidingMenu().setSlidingEnabled(false);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }

    private void scrollCurrentListViewToTop() {
        if (this.currentFragment != null) {
            this.currentFragment.scrollToTop();
        }
    }

    public void setCurrentFragment(ScrollableListFragment fragment) {
        this.currentFragment = fragment;
    }

    public NoteListFragment getNoteListFragment() {
        NoteListFragment fragment = ((NoteListFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        NoteListFragment.class.getName()));
        if (fragment == null) {
            fragment = NoteListFragment.newInstance(getAccount(), getToken());
        }
        return fragment;
    }

    public RelationFragment getRelationFragment() {
        RelationFragment fragment = (RelationFragment)getSupportFragmentManager()
                .findFragmentByTag(RelationFragment.class.getName());
        if (fragment == null) {
            fragment = RelationFragment.newInstance(getAccount());
        }
        return fragment;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        DatabaseManager.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AccountBean intentAccountBean = intent
                .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        if (intentAccountBean == null) {
            return;
        }

        if (accountBean.equals(intentAccountBean)) {
            accountBean = intentAccountBean;
            GlobalContext.getInstance().setAccountBean(accountBean);
            setIntent(intent);
        } else {
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalContext.getInstance().getBitmapCache().evictAll();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSlidingMenu().showMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public UserBean getUser() {
        return accountBean.getInfo();
    }

    public AccountBean getAccount() {
        return accountBean;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public LeftMenuFragment getMenuFragment() {
        LeftMenuFragment fragment = ((LeftMenuFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        LeftMenuFragment.class.getName()));
        if (fragment == null) {
            fragment = LeftMenuFragment.newInstance();
        }
        return fragment;
    }
}