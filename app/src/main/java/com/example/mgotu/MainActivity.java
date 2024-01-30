package com.example.mgotu;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    SwipeRefreshLayout swipeRefreshLayout;
    BottomNavigationView bottomNavigation;
    String url = "https://ies.unitech-mo.ru/schedule";
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
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
            else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
            } else
            Toast.makeText(getApplicationContext(), "Ошибка: Загрузки Фото", Toast.LENGTH_LONG).show();
    }

    @SuppressLint({"SetJavaScriptEnabled", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(permissions, 80);
        CookieManager.getInstance().setAcceptCookie(true);
        WeatherAPI weatherAPI = new WeatherAPI(getApplication());
        weatherAPI.getWeatherDetails();


        webView = findViewById(R.id.web);
        swipeRefreshLayout = findViewById(R.id.swipe);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setSaveFormData(true);
        webView.setWebViewClient(new WebViewclient());
        webView.loadUrl(url);


        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.bottom_raspis);
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.bottom_news:
                    webView.getSettings().setSupportZoom(false);
                    webView.getSettings().setBuiltInZoomControls(false);
                    webView.loadUrl("https://ies.unitech-mo.ru/");
                    break;
                case R.id.bottom_journal:
                    webView.getSettings().setSupportZoom(true);
                    webView.getSettings().setBuiltInZoomControls(true);
                    webView.loadUrl("https://ies.unitech-mo.ru/studentplan");
                    break;
                case R.id.bottom_raspis:
                    webView.getSettings().setSupportZoom(false);
                    webView.getSettings().setBuiltInZoomControls(false);
                    webView.loadUrl("https://ies.unitech-mo.ru/schedule");
                    break;
                case R.id.bottom_chat:
                    webView.getSettings().setSupportZoom(false);
                    webView.getSettings().setBuiltInZoomControls(false);
                    webView.loadUrl("https://ies.unitech-mo.ru/um");
                    break;
                case R.id.bottom_profile:
                    webView.getSettings().setSupportZoom(false);
                    webView.getSettings().setBuiltInZoomControls(false);
                    webView.loadUrl("https://ies.unitech-mo.ru/user");
                    break;
                default:
            }
            return true;
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
            public void onHideCustomView() {
                fullscreen.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                webView.setVisibility(View.GONE);
                if (fullscreen != null) {
                    ((FrameLayout) getWindow().getDecorView()).removeView(fullscreen);
                }
                fullscreen = view;
                ((FrameLayout) getWindow().getDecorView()).addView(fullscreen, new FrameLayout.LayoutParams(-1, -1));
                fullscreen.setVisibility(View.VISIBLE);
            }
            public boolean onShowFileChooser(WebView WebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Ошибка: Проводника", Toast.LENGTH_LONG).show();
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
            CookieManager.getInstance().flush();
            view.loadUrl(request.getUrl().toString());
            return false;
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
            if (error.getErrorCode() == WebViewClient.ERROR_HOST_LOOKUP || error.getErrorCode() == WebViewClient.ERROR_CONNECT || error.getErrorCode() == WebViewClient.ERROR_IO || error.getErrorCode() == WebViewClient.ERROR_REDIRECT_LOOP) {
                webView.loadUrl("file:///android_asset/404.html");
            } else {
                super.onReceivedError(view, request, error);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url){
            view.loadUrl("javascript:getValue()");
            webView.loadUrl("javascript:(function() { " +
                    "document.getElementsByClassName('fl_left user_session_name')[0].style.display='none';" +
                    "document.getElementsByClassName('student_plan_info')[0].style.display='none';" +
                    "document.getElementsByClassName('current_karantine fl_right')[0].style.display='none';" +
                    "})()");
            webView.evaluateJavascript("javascript:(function() { " +
                    "var element = document.querySelector('a[href=\"/\"]'); " +
                    "element.parentNode.removeChild(element); " +
                    "})()", null);
            webView.loadUrl("javascript:(function() { " +
                    "if (document.getElementById('footer')) {" +
                    "    document.getElementById('footer').remove();" +
                    "    document.getElementById('intro').remove();" +
                    "}" +
                    "})()");
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 80) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Права предоставлены", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        webView.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        webView.clearHistory();
        webView.clearCache(true);
    }
}