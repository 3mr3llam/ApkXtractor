package com.pharaohapp.apkxtractor;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_privacy);

		WebView webView = findViewById(R.id.privacy_view);
		webView.loadUrl("file:///android_asset/privacy_policy.html");
		webView.requestFocus();
	}
}
