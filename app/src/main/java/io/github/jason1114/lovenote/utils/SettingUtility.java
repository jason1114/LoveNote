package io.github.jason1114.lovenote.utils;

import android.content.Context;

/**
 * User: qii
 * Date: 12-11-28
 */
public class SettingUtility {

    private static final String FIRSTSTART = "firststart";
    private static final String LAST_FOUND_WEIBO_ACCOUNT_LINK = "last_found_weibo_account_link";
    private static final String CLICK_TO_TOP_TIP = "click_to_top_tip";

    private SettingUtility() {

    }

    public static void setDefaultAccountId(String id) {
        SettingHelper.setEditor(getContext(), "id", id);
    }

    public static String getDefaultAccountId() {
        return SettingHelper.getSharedPreferences(getContext(), "id", "");
    }

    private static Context getContext() {
        return GlobalContext.getInstance();
    }

    public static boolean firstStart() {
        boolean value = SettingHelper.getSharedPreferences(getContext(), FIRSTSTART, true);
        if (value) {
            SettingHelper.setEditor(getContext(), FIRSTSTART, false);
        }
        return value;
    }

    public static boolean getEnableBigAvatar() {
//        return SettingHelper
//                .getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_AVATAR, false);
        return false;
    }

    public static boolean isEnablePic() {
//        return !SettingHelper
//                .getSharedPreferences(getContext(), SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC,
//                        false);
        return false;
    }

    public static boolean getEnableBigPic() {
//        return SettingHelper
//                .getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_PIC, false);
        return false;
    }
    public static int getFontSize() {
//        String value = SettingHelper
//                .getSharedPreferences(getContext(), SettingActivity.FONT_SIZE, "15");
        return 15;
    }
    public static String getMsgCount() {
//        String value = SettingHelper
//                .getSharedPreferences(getContext(), SettingActivity.MSG_COUNT, "3");
//
//        switch (Integer.valueOf(value)) {
//            case 1:
//                return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);
//
//            case 2:
//                return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_50);
//
//            case 3:
//                if (Utility.isConnected(getContext())) {
//                    if (Utility.isWifi(getContext())) {
//                        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_50);
//                    } else {
//                        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);
//                    }
//                }
//        }
//        return String.valueOf(AppConfig.DEFAULT_MSG_COUNT_25);
        return "25";
    }

}
