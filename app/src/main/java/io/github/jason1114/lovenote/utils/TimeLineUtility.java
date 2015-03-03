package io.github.jason1114.lovenote.utils;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import io.github.jason1114.lovenote.bean.MessageBean;
import io.github.jason1114.lovenote.bean.UserBean;

/**
 * User: qii
 * Date: 12-8-29
 * build emotions and clickable string in other threads except UI thread, improve listview scroll
 * performance
 */
public class TimeLineUtility {

    private TimeLineUtility() {
    }

//    public static void addLinks(TextView view) {
//        CharSequence content = view.getText();
//        view.setText(convertNormalStringToSpannableString(content.toString()));
//        if (view.getLinksClickable()) {
//            view.setMovementMethod(LongClickableLinkMovementMethod.getInstance());
//        }
//    }

//    public static SpannableString convertNormalStringToSpannableString(String txt) {
//        //hack to fix android imagespan bug,see http://stackoverflow.com/questions/3253148/imagespan-is-cut-off-incorrectly-aligned
//        //if string only contains emotion tags,add a empty char to the end
//        String hackTxt;
//        if (txt.startsWith("[") && txt.endsWith("]")) {
//            hackTxt = txt + " ";
//        } else {
//            hackTxt = txt;
//        }
//        SpannableString value = SpannableString.valueOf(hackTxt);
//        Linkify.addLinks(value, WeiboPatterns.MENTION_URL, WeiboPatterns.MENTION_SCHEME);
//        Linkify.addLinks(value, WeiboPatterns.WEB_URL, WeiboPatterns.WEB_SCHEME);
//        Linkify.addLinks(value, WeiboPatterns.TOPIC_URL, WeiboPatterns.TOPIC_SCHEME);
//
//        URLSpan[] urlSpans = value.getSpans(0, value.length(), URLSpan.class);
//        MyURLSpan weiboSpan = null;
//        for (URLSpan urlSpan : urlSpans) {
//            weiboSpan = new MyURLSpan(urlSpan.getURL());
//            int start = value.getSpanStart(urlSpan);
//            int end = value.getSpanEnd(urlSpan);
//            value.removeSpan(urlSpan);
//            value.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//
//        TimeLineUtility.addEmotions(value);
//        return value;
//    }

//    public static void addJustHighLightLinks(MessageBean bean) {
//        bean.setListViewSpannableString(convertNormalStringToSpannableString(bean.getText()));
//        bean.getSourceString();
//
//        if (bean.getRetweeted_status() != null) {
//            bean.getRetweeted_status().setListViewSpannableString(
//                    buildOriWeiboSpannalString(bean.getRetweeted_status()));
//            bean.getRetweeted_status().getSourceString();
//        }
//    }

//    private static SpannableString buildOriWeiboSpannalString(MessageBean oriMsg) {
//        String name = "";
//        UserBean oriUser = oriMsg.getUser();
//        if (oriUser != null) {
//            name = oriUser.getScreen_name();
//            if (TextUtils.isEmpty(name)) {
//                name = oriUser.getId();
//            }
//        }
//
//        SpannableString value;
//
//        if (!TextUtils.isEmpty(name)) {
////            value = TimeLineUtility
////                    .convertNormalStringToSpannableString("@" + name + "：" + oriMsg.getText());
//        } else {
////            value = TimeLineUtility.convertNormalStringToSpannableString(oriMsg.getText());
//        }
//        return value;
//    }
}
