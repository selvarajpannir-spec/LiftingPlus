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
        
        // Base WebApp requirements
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);

        // FIX 1: Enable wide responsive viewports (Forces layout engine to mimic mobile Chrome browser scaling)
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // JavaScript Bridge for interception
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void triggerPrint() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // FIX 2: Inject CSS rules immediately before generating the print document adapter
                        // This scales grids, removes clipped double-margins, and forces a crisp page alignment
                        String injectCss = "var style = document.createElement('style');" +
                                "style.type = 'text/css';" +
                                "style.innerHTML = '@media print { " +
                                "html, body { width: 100% !important; margin: 0 !important; padding: 10mm !important; -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; } " +
                                "*, *:before, *:after { box-sizing: border-box !important; } " +
                                "img, canvas, svg, table { max-width: 100% !important; height: auto !important; page-break-inside: avoid !important; } " +
                                ".container, main, div { max-width: 100% !important; width: 100% !important; } " +
                                "}';" +
                                "document.head.appendChild(style);";

                        webView.evaluateJavascript(injectCss, null);

                        // Give the DOM a split second to calculate the style adjustment before sending to spooler
                        webView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                                if (printManager != null) {
                                    PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("LiftingPlus Document");
                                    String jobName = "LiftingPlus Export";
                                    printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                                }
                            }
                        }, 150);
                    }
                });
            }
        }, "AndroidPrint");

        // Set up the router routing window.print() to our native engine
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.evaluateJavascript("window.print = function() { AndroidPrint.triggerPrint(); };", null);
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
                                                       
