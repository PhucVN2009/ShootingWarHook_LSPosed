package com.example.shootingwarhook;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

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

        XposedBridge.log("ShootingWarHook: Đã nạp chế độ SUPER BUFF vào Liên Quân!");

        try {
            // Hook sâu vào JSONObject để thay đổi toàn bộ cấu hình game khi nhận từ server
            findAndHookConstructor("org.json.JSONObject", lpparam.classLoader, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String jsonString = (String) param.args[0];
                    if (jsonString != null && (jsonString.contains("BULLET_FIRE_RATE") || jsonString.contains("ENEMY_SPAWN_RATE"))) {
                        XposedBridge.log("ShootingWarHook: Phát hiện dữ liệu cấu hình game!");
                        
                        JSONObject json = new JSONObject(jsonString);
                        
                        // --- SUPER BUFF LOGIC (Giống như trong video) ---
                        
                        // 1. Tấn công: Sát thương cực đại và tốc độ bắn như tia laser
                        json.put("BULLET_DAMAGE", 9999); 
                        json.put("BULLET_FIRE_RATE", 0.001); // Tốc độ bắn nhanh nhất có thể
                        
                        // 2. Vật phẩm & Kỹ năng: Kích hoạt liên tục, không giới hạn
                        json.put("SKILL_ITEM_MAX", 999);
                        json.put("SKILL_COOLDOWN", 0); // Nếu có tham số này, kỹ năng sẽ không có hồi chiêu
                        
                        // 3. Kẻ địch: Giảm tối đa tỉ lệ xuất hiện để vượt màn nhanh
                        json.put("ENEMY_SPAWN_RATE", 0.001);
                        json.put("ENEMY_MAX", 1); // Chỉ cho phép 1 quái xuất hiện cùng lúc
                        
                        // 4. Tốc độ di chuyển của nhân vật (Nếu có trong param)
                        if (json.has("PLAYER_MOVE_SPEED")) {
                            json.put("PLAYER_MOVE_SPEED", 5.0); // Tăng tốc độ chạy
                        }

                        XposedBridge.log("ShootingWarHook: Đã áp dụng SUPER BUFF thành công!");
                        
                        param.args[0] = json.toString();
                        showToast("Kích hoạt SUPER BUFF: Tốc độ x999, Sát thương x999!");
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("ShootingWarHook: Lỗi khi thực hiện Super Buff: " + e.getMessage());
        }
    }

    private void showToast(final String message) {
        if (hasShownToast) return;
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
                    XposedBridge.log("ShootingWarHook: Toast Error: " + e.getMessage());
                }
            }
        });
    }
}
