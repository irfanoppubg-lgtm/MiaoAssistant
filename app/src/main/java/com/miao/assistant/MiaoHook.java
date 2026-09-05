package com.miao.assistant;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MiaoHook implements IXposedHookLoadPackage {

    private static final String TAG = "MiaoHook";
    private static final String QQ_PACKAGE = "com.tencent.mobileqq";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(QQ_PACKAGE)) return;
        Log.i(TAG, "🐱 喵喵助手已加载！");
        hookQQSendMessage(lpparam);
        hookTextView(lpparam);
    }

    private void hookQQSendMessage(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> clazz = XposedHelpers.findClass(
                "com.tencent.mobileqq.aio.msglist.holder.component.TextSendMsgComponent",
                lpparam.classLoader
            );
            XposedHelpers.findAndHookMethod(clazz, "sendTextMessage", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String original = (String) param.args[0];
                        if (original != null && !original.isEmpty()) {
                            param.args[0] = MiaoProcessor.process(original);
                        }
                    }
                }
            );
            Log.i(TAG, "✅ QQ 9.x Hook成功");
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ QQ 9.x Hook跳过");
        }

        try {
            Class<?> clazz = XposedHelpers.findClass(
                "com.tencent.mobileqq.activity.aio.helper.AIOMessageHelper",
                lpparam.classLoader
            );
            XposedHelpers.findAndHookMethod(clazz, "sendTextMessage",
                android.content.Context.class, Object.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String original = (String) param.args[2];
                        if (original != null && !original.isEmpty()) {
                            param.args[2] = MiaoProcessor.process(original);
                        }
                    }
                }
            );
            Log.i(TAG, "✅ QQ 8.x Hook成功");
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ QQ 8.x Hook跳过");
        }
    }

    private void hookTextView(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("android.widget.TextView",
                lpparam.classLoader, "setText", CharSequence.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        CharSequence cs = (CharSequence) param.args[0];
                        if (cs == null) return;
                        String text = cs.toString();
                        if (text.isEmpty() || text.length() > 500) return;
                        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                        for (StackTraceElement e : stack) {
                            if (e.getClassName().contains("mobileqq") ||
                                e.getClassName().contains("tencent")) {
                                String processed = MiaoProcessor.process(text);
                                if (!processed.equals(text)) {
                                    param.args[0] = processed;
                                }
                                break;
                            }
                        }
                    }
                }
            );
            Log.i(TAG, "✅ TextView兜底Hook成功");
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ TextView Hook跳过");
        }
    }
}
