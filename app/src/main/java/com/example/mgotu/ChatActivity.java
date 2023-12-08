package com.example.mgotu;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChatActivity extends AppCompatActivity {
    WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    SwipeRefreshLayout swipeRefreshLayout;
    String url = "https://ies.unitech-mo.ru/um";
    public final boolean isConnected = true;
    String[] permissions = {
            "android.permission.ACCESS_DOWNLOAD_MANAGER",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.CAMERA"};
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != ChatActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(getApplicationContext(), "Ошибка: Загрузки Фото", Toast.LENGTH_LONG).show();
    }

    @SuppressLint({"SetJavaScriptEnabled", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        requestPermissions(permissions, 80);
        CookieManager.getInstance().setAcceptCookie(true);


        webView = findViewById(R.id.web);
        swipeRefreshLayout = findViewById(R.id.swipe);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setSaveFormData (true);
        webView.setWebViewClient(new WebViewclient());
        webView.loadUrl(url);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.bottom_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
            if(id == R.id.bottom_journal) {
                startActivity(new Intent(getApplicationContext(), JournalActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
            if(id == R.id.bottom_raspis) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
            if(id == R.id.bottom_chat) {
                return true;
            }
            if(id == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
            return false;
        });


        swipeRefreshLayout.setEnabled(false); // Delete если надо свайп
        swipeRefreshLayout.setRefreshing(false); // Delete если надо свайп
        /*
        swipeRefreshLayout.setOnRefreshListener(() -> {
        swipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(() -> {
        swipeRefreshLayout.setRefreshing(false);
        webView.reload();
        },  3000);
        });
        */
        webView.setWebChromeClient(new WebChromeClient() {
            View fullscreen = null;
            @Override
            public void onHideCustomView()
            {
                fullscreen.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback)
            {
                webView.setVisibility(View.GONE);
                if(fullscreen != null)
                {
                    ((FrameLayout)getWindow().getDecorView()).removeView(fullscreen);
                }
                fullscreen = view;
                ((FrameLayout)getWindow().getDecorView()).addView(fullscreen, new FrameLayout.LayoutParams(-1, -1));
                fullscreen.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Ошибка: Открытия проводника", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
            request.setDescription("Скачивание...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Скачивание...", Toast.LENGTH_SHORT).show();
        });
    }
    public class WebViewclient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            CookieManager.getInstance().flush();
            if (isConnected) {
                return false;
            } else {
                webView.loadUrl("file:///android_asset/404.html");
                return true;
            }
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
            webView.loadUrl("file:///android_asset/404.html");
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            webView.loadUrl("javascript:document.open();document.close();");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);
    }
    @Override
    protected void onStop() {
        super.onStop();
        webView.clearHistory();
        webView.clearFormData();
        webView.clearCache(true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 80) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Права предоставлены", Toast.LENGTH_SHORT).show();
            }
            /* else {
                Toast.makeText(this,"Ошибка прав",Toast.LENGTH_SHORT).show();
            }*/
        }
    }
}
