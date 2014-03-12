package com.johns4ta.news_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		// Initalize UI and set webview settings
		WebView wb = (WebView) findViewById(R.id.wbWebview);
		WebSettings wbSettings = wb.getSettings();
		wb.setWebViewClient(new WebViewClient());
		wbSettings.setJavaScriptEnabled(true);
		
		// Retrieve url from intent passed to webview activity
		Intent in = getIntent();
		String url = in.getStringExtra("URL");
		wb.loadUrl(url);
	}

	//Go back to newsfeed on back button press
	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, NewsFeed.class);
		startActivity(i);
		finish();
	}
}
