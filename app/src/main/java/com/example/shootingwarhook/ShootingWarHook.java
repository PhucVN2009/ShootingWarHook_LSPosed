package com.example.shootingwarhook;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ShootingWarHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.garena.game.kgvn";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // Luôn log để biết module đã được nạp vào tiến trình nào
        XposedBridge.log("ShootingWarHook: Đang kiểm tra tiến trình: " + lpparam.processName);

        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("ShootingWarHook: Đã nạp thành công vào Liên Quân Mobile!");

        // Kỹ thuật 1: Hook vào Constructor của JSONObject (Hiệu quả nhất khi game nhận dữ liệu từ server)
        try {
            findAndHookConstructor("org.json.JSONObject", lpparam.classLoader, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String jsonString = (String) param.args[0];
                    if (jsonString != null && jsonString.contains("BULLET_FIRE_RATE")) {
                        XposedBridge.log("ShootingWarHook: Phát hiện dữ liệu API /api/app/param trong Constructor!");
                        
                        JSONObject json = new JSONObject(jsonString);
                        modifyJson(json);
                        
                        // Ghi đè tham số đầu vào của Constructor
                        param.args[0] = json.toString();
                        XposedBridge.log("ShootingWarHook: Đã sửa đổi dữ liệu đầu vào thành công.");
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Lỗi khi hook Constructor JSONObject: " + e.getMessage());
        }

        // Kỹ thuật 2: Hook vào toString của JSONObject (Dự phòng nếu game sử dụng lại object cũ)
        try {
            findAndHookMethod("org.json.JSONObject", lpparam.classLoader, "toString", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String result = (String) param.getResult();
                    if (result != null && result.contains("BULLET_FIRE_RATE")) {
                        XposedBridge.log("ShootingWarHook: Phát hiện dữ liệu API trong toString()!");
                        
                        JSONObject json = new JSONObject(result);
                        modifyJson(json);
                        
                        param.setResult(json.toString());
                        XposedBridge.log("ShootingWarHook: Đã sửa đổi kết quả toString() thành công.");
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Lỗi khi hook toString JSONObject: " + e.getMessage());
        }
    }

    private void modifyJson(JSONObject json) {
        try {
            json.put("BULLET_DAMAGE", 500);
            json.put("BULLET_FIRE_RATE", 0.01);
            json.put("SKILL_ITEM_MAX", 99);
            json.put("ENEMY_SPAWN_RATE", 0.05);
            
            // Log chi tiết để người dùng kiểm tra trong LSPosed Manager
            XposedBridge.log("ShootingWarHook: [BUFF] DAMAGE=500, RATE=0.01, SKILL=99, SPAWN=0.05");
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Lỗi khi thực hiện buff: " + e.getMessage());
        }
    }
}
