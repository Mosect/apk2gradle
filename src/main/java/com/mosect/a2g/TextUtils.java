package com.mosect.a2g;

public final class TextUtils {

    private TextUtils() {
    }

    public static boolean empty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    public static boolean notEmpty(CharSequence cs) {
        return !empty(cs);
    }
}
