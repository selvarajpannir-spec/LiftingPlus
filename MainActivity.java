package com.lifting.plus;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

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
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // JavaScript → Android bridge
        // JS calls: AndroidBridge.savePDF(base64String, "filename.pdf")
        webView.addJavascriptInterface(new Object() {

            @JavascriptInterface
            public void savePDF(String base64Data, String fileName) {
                try {
                    // Strip the data:application/pdf;base64, prefix if present
                    String pureBase64 = base64Data;
                    if (base64Data.contains(",")) {
                        pureBase64 = base64Data.split(",")[1];
                    }
                    byte[] pdfBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                    Uri savedUri = null;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Android 10+ — use MediaStore (no permission needed)
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        savedUri = getContentResolver().insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                        if (savedUri != null) {
                            try (OutputStream os = getContentResolver().openOutputStream(savedUri)) {
                                if (os != null) os.write(pdfBytes);
                            }
                        }
                    } else {
                        // Android 9 and below — write to Downloads folder directly
                        File downloadsDir = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS);
                        File outFile = new File(downloadsDir, fileName);
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            fos.write(pdfBytes);
                        }
                        savedUri = Uri.fromFile(outFile);
                    }

                    // Open the PDF with the device's default viewer
                    final Uri uriToOpen = savedUri;
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "PDF saved to Downloads: " + fileName,
                                Toast.LENGTH_LONG).show();

                        if (uriToOpen != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uriToOpen, "application/pdf");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_ACTIVITY_NO_HISTORY);
                            try {
                                startActivity(intent);
                            } catch (Exception ignored) {
                                // No PDF viewer installed — file is still saved
                            }
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "PDF save failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                    );
                }
            }

            @JavascriptInterface
            public void showToast(String message) {
                runOnUiThread(() ->
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show()
                );
            }

        }, "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // No window.print override needed — HTML handles export via jsPDF + AndroidBridge
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
