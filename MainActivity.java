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

        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        
        // Base WebApp offline requirements
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);

        // Turn off desktop viewport emulation so print modules use natural fluid scaling
        webSettings.setUseWideViewPort(false);
        webSettings.setLoadWithOverviewMode(false);

        // JavaScript Native Bridge
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void triggerPrint() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        if (printManager != null) {
                            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("LiftingPlus Document");
                            String jobName = "LiftingPlus Export";
                            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                        }
                    }
                });
            }
        }, "AndroidPrint");

        // UI Router & Style Injector
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                // 1. Map standard window print requests to the native interface
                view.evaluateJavascript("window.print = function() { AndroidPrint.triggerPrint(); };", null);

                // 2. Inject a robust print-media stylesheet immediately when the page finishes loading.
                // This strips out complex side-by-side flex/grid rule constraints during printing 
                // and forces elements to stack sequentially with crisp alignments.
                String printLayoutFixCss = "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "style.innerHTML = '@media print { " +
                        "html, body, main, .container, .row, [class*=\"col-\"] { " +
                        "  width: 100% !important; " +
                        "  max-width: 100% !important; " +
                        "  display: block !important; " +
                        "  float: none !important; " +
                        "  padding: 4mm !important; " +
                        "  margin: 0 !important; " +
                        "  box-sizing: border-box !important; " +
                        "} " +
                        "div, section, .card, .box { " +
                        "  display: block !important; " +
                        "  width: 100% !important; " +
                        "  max-width: 100% !important; " +
                        "  float: none !important; " +
                        "  page-break-inside: avoid !important; " +
                        "  box-sizing: border-box !important; " +
                        "} " +
                        "img, canvas, svg, table { " +
                        "  max-width: 100% !important; " +
                        "  height: auto !important; " +
                        "  display: block !important; " +
                        "  margin: 0 auto !important; " +
                        "} " +
                        "}';" +
                        "document.head.appendChild(style);";

                view.evaluateJavascript(printLayoutFixCss, null);
            }
        });
        
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
