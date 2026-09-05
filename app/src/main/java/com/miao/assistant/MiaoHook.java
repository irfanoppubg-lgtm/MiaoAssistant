package com.miao.assistant;

import android.util.Log;

public class MiaoHook {

    private static final String TAG = "MiaoHook";

    public static void init() {
        Log.i(TAG, "🐱 喵喵助手已加载！");
    }

    public static String processMessage(String original) {
        return MiaoProcessor.process(original);
    }
}
