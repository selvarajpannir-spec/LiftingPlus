
package com.lifting.plus;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize WebView directly without needing a separate layout XML file
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        
        // Optimize settings for a smooth, offline local web application experience
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        
        // Point directly to the isolated asset path where your calculator lives
        webView.loadUrl("file:///android_asset/lifting_cal.html");

        setContentView(webView);
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
