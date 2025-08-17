package com.example.watrack;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Enable Cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient());

        // Load WhatsApp Web (It will auto-login if the user already logged in from desktop)
        webView.loadUrl("https://web.whatsapp.com");

        // Inject JavaScript to track online status
        webView.evaluateJavascript(
                "(function() {" +
                        "   setInterval(() => {" +
                        "       let status = document.querySelector('span[title=\"online\"]');" +
                        "       if (status) {" +
                        "           console.log('User is Online');" +
                        "       } else {" +
                        "           console.log('User is Offline');" +
                        "       }" +
                        "   }, 3000);" +
                        "})();",
                null
        );
    }
}
