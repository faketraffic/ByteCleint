package me.bytebase.byteclient.util;

import me.bytebase.byteclient.features.settings.Bind;

public class KeyboardUtil {
    public static String getKeyName(int key) {
        String str = new Bind(key).toString().toUpperCase();
        str = str.replace("KEY.KEYBOARD", "").replace(".", " ");
        return str;
    }
}
