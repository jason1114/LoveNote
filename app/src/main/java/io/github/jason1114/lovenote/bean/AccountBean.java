package io.github.jason1114.lovenote.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import io.github.jason1114.lovenote.utils.ObjectToStringUtility;


/**
 * User: Jiang Qi
 * Date: 12-7-30
 */
public class AccountBean implements Parcelable {

    private String accessToken;
    private long expiresTime;
    private UserBean info;

    public String getUid() {
        return (info != null ? info.getId() : "");
    }

    public String getUserNick() {
        return (info != null ? info.getScreen_name() : "");
    }

    public String getAvatarUrl() {
        return (info != null ? info.getProfile_image_url() : "");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }

    public UserBean getInfo() {
        return info;
    }

    public void setInfo(UserBean info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accessToken);
        dest.writeLong(expiresTime);
        dest.writeParcelable(info, flags);
    }

    public static final Creator<AccountBean> CREATOR =
            new Creator<AccountBean>() {
                public AccountBean createFromParcel(Parcel in) {
                    AccountBean accountBean = new AccountBean();
                    accountBean.accessToken = in.readString();
                    accountBean.expiresTime = in.readLong();
                    accountBean.info = in.readParcelable(UserBean.class.getClassLoader());

                    return accountBean;
                }

                public AccountBean[] newArray(int size) {
                    return new AccountBean[size];
                }
            };

    @Override
    public boolean equals(Object o) {

        return o instanceof AccountBean
                && !TextUtils.isEmpty(((AccountBean) o).getUid())
                && ((AccountBean) o).getUid().equalsIgnoreCase(getUid());
    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }
}
