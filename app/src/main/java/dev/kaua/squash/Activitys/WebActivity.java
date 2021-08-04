package dev.kaua.squash.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dev.kaua.squash.Adapters.ProgressBarAnimation;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

@SuppressLint({"SetJavaScriptEnabled", "UseCompatLoadingForDrawables"})
public class WebActivity extends AppCompatActivity {
    private WebView webView;
    private LinearProgressIndicator progressBar;
    private ImageView secure_status_web;
    private TextView status_web, url_web;
    static Uri url_start;
    private String url_active;
    private ProgressBarAnimation anim;
    private static final String DOCS_URL = "https://docs.google.com/gview?embedded=true&url=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        Ids();

        Toolbar toolbar = findViewById(R.id.toolbar_web);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        url_start = getIntent().getData();
        if(url_start.toString().endsWith(".pdf")) webView.loadUrl(DOCS_URL + url_start.toString());
        else webView.loadUrl(url_start.toString());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        url_web.setText(url_start.toString().replace("https://", "").replace("http://", ""));
        url_active = url_start.toString();
        status_web.setText(getString(R.string.loading));

        //  Check If page is loading or not
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                status_web.setText(getString(R.string.loading));
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getSettings().setLoadsImagesAutomatically(true);
                url_active = url;
                progressBar.setVisibility(View.GONE);

                SetUrlLink(url);

                if(view.getTitle().replace("_", " ").replace(".pdf", "").equals(getString(R.string.squash_privacy_policy)))
                    status_web.setText(getString(R.string.squash_privacy_policy));
                else{
                    String web_title = view.getTitle();
                    if(web_title.length() > 40)
                        web_title = web_title.substring(0, 38) + "…";
                    status_web.setText(web_title);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress)
            {
                progressBar.setVisibility(View.VISIBLE);

                anim = null;
                anim = new ProgressBarAnimation(progressBar, progressBar.getProgress(), progress);
                anim.setDuration(500);
                progressBar.startAnimation(anim);

                // update the progressBar
                Log.d("WebProgress", "Current progress -> " + progress);

                if(progress  == 100) {
                    anim = null;
                    anim = new ProgressBarAnimation(progressBar, progressBar.getProgress(), progress);
                    anim.setDuration(500);
                    progressBar.startAnimation(anim);
                    new Handler().postDelayed(() -> progressBar.setVisibility(View.GONE),500);
                }
            }
        });

        // SecurityLevel Click
        url_web.setOnClickListener(v -> ShowSecurityLevel());
        status_web.setOnClickListener(v -> ShowSecurityLevel());

    }

    private void SetUrlLink(String url) {
        int index = url.lastIndexOf('/');
        String url_replace = url.substring(0, index).replace("https://", "").replace("http://", "");

        if(url_replace.length() > 43)
            url_replace = url_replace.substring(0, 38) + "…";

        url_web.setText(url_replace);

        secure_status_web.setVisibility(View.VISIBLE);
        if(url_active.startsWith("https://")) secure_status_web.setImageDrawable(getDrawable(R.drawable.ic_secure_lock));
        else secure_status_web.setImageDrawable(getDrawable(R.drawable.ic_not_secure_lock));
    }

    private void Ids() {
        progressBar = findViewById(R.id.progress_web);
        status_web = findViewById(R.id.status_web);
        secure_status_web = findViewById(R.id.secure_status_web);
        url_web = findViewById(R.id.url_web);
        webView = findViewById(R.id.webView);

        progressBar.setVisibility(View.GONE);
    }

    private void ShowSecurityLevel() {
        if (url_active.startsWith("https://")) Warnings.Base_Sheet_Alert(this, getString(R.string.secure_website), true);
        else Warnings.Base_Sheet_Alert(this, getString(R.string.not_secure_website), true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.report_webSite:
                ToastHelper.toast(this, getString(R.string.under_development), 0);
                return true;
            case R.id.refresh_web:
                webView.reload();
                return true;
            case R.id.open_with_web:
                Uri uri = Uri.parse(url_active);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.copy_link_web:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("URL_" + url_active, url_active);
                clipboard.setPrimaryClip(clip);
                ToastHelper.toast(this, getString(R.string.url_copied), 0);
                return true;
        }
        return false;
    }
}