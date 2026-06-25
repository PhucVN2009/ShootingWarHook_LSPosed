package com.example.shootingwarhook;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private EditText urlInput;
    private Button btnStart;
    private LinearLayout inputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tạo giao diện bằng code để đơn giản và tránh lỗi resource trong AIDE
        inputLayout = new LinearLayout(this);
        inputLayout.setOrientation(LinearLayout.VERTICAL);
        inputLayout.setPadding(20, 20, 20, 20);

        urlInput = new EditText(this);
        urlInput.setHint("Dán link sự kiện vào đây...");
        urlInput.setText(""); // Reset
        inputLayout.addView(urlInput);

        btnStart = new Button(this);
        btnStart.setText("BẮT ĐẦU HACK & CHƠI");
        inputLayout.addView(btnStart);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Giả lập User-Agent của game để web nhận diện đúng (tránh bị đẩy ra ngoài)
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/120.0.0.0 Mobile Safari/537.36 GarenaGame/KG VN");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Inject JavaScript ngay khi trang tải xong
                injectBuffScript(view);
            }
        });

        inputLayout.addView(webView, new LinearLayout.LayoutParams(-1, -1));
        setContentView(inputLayout);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlInput.getText().toString().trim();
                if (url.startsWith("http")) {
                    webView.loadUrl(url);
                    btnStart.setVisibility(View.GONE);
                    urlInput.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập URL hợp lệ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void injectBuffScript(WebView view) {
        // Mã JavaScript để ghi đè các tham số game ngay trên trình duyệt
        // Chúng ta hook vào hàm JSON.parse để sửa đổi dữ liệu trước khi game nhận được
        String js = "javascript:(function() {" +
                "   console.log('ShootingWar: Đang tiêm mã Buff...');" +
                "   var originalJSONParse = JSON.parse;" +
                "   JSON.parse = function(text) {" +
                "       var data = originalJSONParse(text);" +
                "       if (data && (data.BULLET_FIRE_RATE !== undefined || data.ENEMY_SPAWN_RATE !== undefined)) {" +
                "           console.log('ShootingWar: Phát hiện dữ liệu cấu hình, đang sửa đổi...');" +
                "           data.BULLET_DAMAGE = 9999;" +
                "           data.BULLET_FIRE_RATE = 0.001;" +
                "           data.SKILL_ITEM_MAX = 999;" +
                "           data.ENEMY_SPAWN_RATE = 0.001;" +
                "           data.ENEMY_MAX = 1;" +
                "           data.BOSS_SCALE_FACTOR = 0.1;" + // Thu nhỏ boss để dễ bắn
                "       }" +
                "       return data;" +
                "   };" +
                "   console.log('HỆ THỐNG BUFF ĐÃ SẴN SÀNG!');" +
                "})();";
        view.loadUrl(js);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
