package com.lifting.plus;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the app's core WebView container
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        
        // Optimize configuration for offline local data processing
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);

        // 1. CREATE THE BRIDGE: Links JavaScript actions to native Android services
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void triggerPrint() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        if (printManager != null) {
                            // Formats the current WebView contents into a printable page layout
                            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("LiftingPlus Document");
                            String jobName = "LiftingPlus Export";
                            
                            // Launches the standard system print menu (with "Save as PDF" features)
                            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                        }
                    }
                });
            }
        }, "AndroidPrint");

        // 2. INJECT THE ROUTER: Reroutes standard window.print() commands to our bridge
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // When the HTML page finishes loading, map window.print to our custom trigger
                view.evaluateJavascript("window.print = function() { AndroidPrint.triggerPrint(); };", null);
            }
        });
        
        // Point to the isolated calculator asset folder path
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
