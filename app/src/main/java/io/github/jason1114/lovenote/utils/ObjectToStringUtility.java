package io.github.jason1114.lovenote.utils;

import io.github.jason1114.lovenote.bean.AccountBean;
import io.github.jason1114.lovenote.bean.MessageBean;
import io.github.jason1114.lovenote.bean.MessageListBean;
import io.github.jason1114.lovenote.bean.UserBean;

/**
 * User: qii
 * Date: 13-3-29
 */
public class ObjectToStringUtility {

    public static String toString(AccountBean account) {
        return account.getUserNick();
    }

    public static String toString(UserBean bean) {
        return "user id=" + bean.getId()
                + "," + "name=" + bean.getScreen_name();
    }

    public static String toString(MessageBean msg) {
        UserBean userBean = msg.getUser();
        String username = (userBean != null ? userBean.getScreen_name() : "user is null");
        return String.format("%s @%s:%s", TimeUtility.getListTime(msg.getMills()), username,
                msg.getText());
    }

    public static String toString(MessageListBean listBean) {
        StringBuilder builder = new StringBuilder();
        for (MessageBean data : listBean.getItemList()) {
            builder.append(data.toString());
        }
        return builder.toString();
    }
}
