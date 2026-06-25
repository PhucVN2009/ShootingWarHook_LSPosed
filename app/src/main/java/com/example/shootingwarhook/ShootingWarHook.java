package com.example.shootingwarhook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ShootingWarHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.garena.game.kgvn";
    private static final String TARGET_HOST = "shootingwar.lienquan.garena.vn";
    private static final String TARGET_PATH = "/api/app/param";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("ShootingWarHook: Loaded for " + lpparam.packageName);

        // Hook OkHttp3 if used, or WebView's response handling
        // Based on the HAR, it seems to be a WebView-based game or using standard networking.
        // We will hook common networking libraries or WebView client.
        
        // Hooking OkHttp3 Interceptor is a common way to modify responses.
        // However, since we don't know the exact networking library, 
        // a more generic way is to hook the JSON parsing or the response callback.
        
        // For this specific request, let's try to hook common JSON constructors 
        // or networking response handlers.
        
        try {
            findAndHookMethod("org.json.JSONObject", lpparam.classLoader, "toString", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String result = (String) param.getResult();
                    if (result != null && result.contains("BULLET_FIRE_RATE") && result.contains("BULLET_DAMAGE")) {
                        XposedBridge.log("ShootingWarHook: Found target JSON response, modifying...");
                        JSONObject json = new JSONObject(result);
                        
                        // Buffing stats as requested
                        json.put("BULLET_DAMAGE", 500);
                        json.put("BULLET_FIRE_RATE", 0.01);
                        json.put("SKILL_ITEM_MAX", 99);
                        json.put("ENEMY_SPAWN_RATE", 0.05);
                        
                        param.setResult(json.toString());
                        XposedBridge.log("ShootingWarHook: Modified JSON: " + json.toString());
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Error hooking JSONObject: " + e.getMessage());
        }

        // Alternative: Hook WebView's shouldInterceptRequest or similar if it is a pure H5 game
        try {
            findAndHookMethod("android.webkit.WebViewClient", lpparam.classLoader, "onPageFinished", 
                "android.webkit.WebView", "java.lang.String", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String url = (String) param.args[1];
                    if (url != null && url.contains(TARGET_HOST)) {
                        XposedBridge.log("ShootingWarHook: WebView loaded target host: " + url);
                    }
                }
            });
        } catch (Exception e) {
            // Ignore if class not found
        }
    }
}
