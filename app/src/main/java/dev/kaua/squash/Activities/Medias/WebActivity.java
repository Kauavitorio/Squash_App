package dev.kaua.squash.Activities.Medias;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import dev.kaua.squash.Adapters.User.ProgressBarAnimation;
import dev.kaua.squash.Data.System.DtoSystem;
import dev.kaua.squash.LocalDataBase.DaoBrowser;
import dev.kaua.squash.R;
import dev.kaua.squash.Tools.ToastHelper;
import dev.kaua.squash.Tools.Warnings;

@SuppressLint({"SetJavaScriptEnabled", "UseCompatLoadingForDrawables"})
public class WebActivity extends AppCompatActivity {
    static final String TAG = "WebActivityLog";
    private WebView webView;
    private LinearProgressIndicator progressBar;
    private ImageView secure_status_web;
    private TextView status_web, url_web;
    static Uri url_start;
    private String url_active;
    private DaoBrowser daoBrowser;
    private ProgressBarAnimation anim;
    private static final String DOCS_URL = "https://docs.google.com/gview?embedded=true&url=";
    private static boolean FIRST_LOAD = true;

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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final String URL = request.getUrl().toString();
                if (URL.endsWith(".pdf")) {
                    Log.d(TAG, URL);
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(URL), "application/pdf");
                    try {
                        view.getContext().startActivity(intent);
                    } catch (Exception e) {
                        // User does not have a pdf viewer installed
                        webView.loadUrl(DOCS_URL + URL);
                    }
                } else
                    webView.loadUrl(URL);
                return true;
            }

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
                if(view.getTitle().contains(DtoSystem.Squash_Privacy_Policy)) {
                    status_web.setText(getString(R.string.squash_privacy_policy));

                    DtoSystem link = new DtoSystem(DtoSystem.Squash_Privacy_Policy);
                    daoBrowser.InsertLink(link, WebActivity.this);
                    FIRST_LOAD = false;
                }

                else{
                    String web_title = view.getTitle();
                    if(web_title.length() > 40)
                        web_title = web_title.substring(0, 38) + "…";
                    status_web.setText(web_title);
                }

                Log.e("Property", "Load -> " +  FIRST_LOAD);
                if(FIRST_LOAD){
                    FIRST_LOAD = false;
                    new FetchMetadataFromURL().execute();
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
        daoBrowser = new DaoBrowser(this);
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
                ToastHelper.toast(this, getString(R.string.url_copied), ToastHelper.SHORT_DURATION);
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class FetchMetadataFromURL extends AsyncTask<Void, Void, Void> {
        String websiteTitle, websiteDescription, ImgUrl;
        @Override
        protected void onPreExecute() { super.onPreExecute();}
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to website
                Document document = Jsoup.connect(url_active).get();
                Log.e("Property", "URL=> " + url_active);
                // Get the html document title
                websiteTitle = document.title();

                //Here It's just print whole property of URL
                Elements metaElems = document.select("meta");
                for (Element metaElem : metaElems) {
                    String property = metaElem.attr("property");
                    Log.e("Property", "Property -> " + property + " \n Value -> " + metaElem.attr("content"));
                }
                // Locate the content attribute
                websiteDescription = metaElems.attr("content");
                Elements metaOgImage = document.select("meta[property=og:image]");
                if (metaOgImage != null) {
                    ImgUrl = metaOgImage.first().attr("content");
                     Log.e("Property", "Image -> " + ImgUrl);
                }
            } catch (Exception e) {
                Log.w("Property", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df_date = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = df_date.format(Calendar.getInstance().getTime());
            if(websiteTitle != null){
                Log.e("Property", "Insert -> " +  websiteTitle);
                DtoSystem link = new DtoSystem(websiteTitle, url_active, formattedDate,
                        ImgUrl);
                daoBrowser.InsertLink(link, WebActivity.this);
            }

        }

    }

    @Override
    protected void onDestroy() {
        FIRST_LOAD = true;
        Log.d("MAIN_FRAGMENT_LOG", "Reset FIRST_LOAD");
        super.onDestroy();
    }
}