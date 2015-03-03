package io.github.jason1114.lovenote.firebase;

import com.firebase.client.Firebase;

/**
 * Created by Jason on 2015/2/27.
 */
public class FireBaseService {
    private static Firebase mBase;
    private static String FIRE_BASE_URL = "https://lovenote.firebaseio.com/";

    private FireBaseService() {
        // stop init
    }

    public static Firebase getInstance() {
        if (mBase != null) {
            return mBase;
        } else {
            return mBase = new Firebase(FIRE_BASE_URL);
        }
    }
}
