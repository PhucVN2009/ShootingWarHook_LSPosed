package com.example.shootingwarhook;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ShootingWarHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.garena.game.kgvn";
    private boolean hasShownToast = false;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("ShootingWarHook: Đã nạp vào Liên Quân Mobile!");

        // Hook vào Constructor của JSONObject
        try {
            findAndHookConstructor("org.json.JSONObject", lpparam.classLoader, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String jsonString = (String) param.args[0];
                    if (jsonString != null && jsonString.contains("BULLET_FIRE_RATE")) {
                        XposedBridge.log("ShootingWarHook: Phát hiện dữ liệu API trong Constructor!");
                        
                        JSONObject json = new JSONObject(jsonString);
                        modifyJson(json);
                        
                        param.args[0] = json.toString();
                        showToast("Đã Hook & Buff Shooting War thành công!");
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Lỗi hook: " + e.getMessage());
        }
    }

    private void modifyJson(JSONObject json) {
        try {
            json.put("BULLET_DAMAGE", 500);
            json.put("BULLET_FIRE_RATE", 0.01);
            json.put("SKILL_ITEM_MAX", 99);
            json.put("ENEMY_SPAWN_RATE", 0.05);
            XposedBridge.log("ShootingWarHook: Đã thay đổi chỉ số thành công.");
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Lỗi buff: " + e.getMessage());
        }
    }

    private void showToast(final String message) {
        if (hasShownToast) return; // Chỉ hiện 1 lần để tránh làm phiền
        
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AndroidAppHelper.currentApplication();
                    if (context != null) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        hasShownToast = true;
                    }
                } catch (Exception e) {
                    XposedBridge.log("ShootingWarHook: Không thể hiển thị Toast: " + e.getMessage());
                }
            }
        });
    }
}
