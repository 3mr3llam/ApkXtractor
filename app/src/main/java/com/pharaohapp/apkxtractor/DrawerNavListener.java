package com.pharaohapp.apkxtractor;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.navigation.NavigationView;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class DrawerNavListener implements NavigationView.OnNavigationItemSelectedListener {

	private WeakReference<Context> context;
	private boolean newActivity;

	private AlertDialog dialog;
	private AlertDialog.Builder alert;
	private WebView wv;

	DrawerNavListener(Context context, boolean newActivity) {
		this.context = new WeakReference<>(context);
		this.newActivity = newActivity;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
		// set item as selected to persist highlight
		if (newActivity) {
			//menuItem.setChecked(false);
			newActivity = false;
		}

		if (menuItem.getItemId() == R.id.action_rate) {
			rateThisApp();

		} else if (menuItem.getItemId() == R.id.action_feedback) {
			openFeedback();

		} else if (menuItem.getItemId() == R.id.action_privacy) {
			Intent i = new Intent(context.get(), PrivacyActivity.class);
			context.get().startActivity(i);

		} //else if (menuItem.getItemId() == R.id.action_about) {
//			showAboutDialog();
//		}

		// close drawer when item is tapped
		//((MainActivity)context.get()).mDrawerLayout.closeDrawers();
		return true;
	}

	private void showAboutDialog() {

		alert = new AlertDialog.Builder(context.get());

		wv = new WebView(context.get());
		wv.loadUrl("file:///android_asset/about_app.html");
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);

				return true;
			}
		});

		alert.setView(wv);


//		alert.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int id) {
//				dialog.dismiss();
//			}
//		});

		dialog = alert.create();
		dialog.show();


		//LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
		//		LinearLayout.LayoutParams.MATCH_PARENT,
		//		LinearLayout.LayoutParams.WRAP_CONTENT); //create a new one
		//layoutParams.weight = 1.0f;
		//layoutParams.gravity = Gravity.CENTER; //this is layout_gravity
		//dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setLayoutParams(layoutParams);
	}


	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private int getRateFlags() {
		int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
		return flags |= (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
				Intent.FLAG_ACTIVITY_NEW_DOCUMENT :
				Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;//noinspection deprecation
	}

	private void rateThisApp() {
		Uri uri = Uri.parse("market://details?id=" + context.get().getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

		// To count with Play market backstack, After pressing back button,
		// to taken back to our application, we need to add following flags to intent.
		goToMarket.addFlags(getRateFlags());
		try {
			context.get().startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			try {
				context.get().startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id=" + context.get().getPackageName())));
			} catch (ActivityNotFoundException ex) {
				Log.e("rateThisApp", "rateThisApp: R.string.error_no_app_found ");
			}
		}
	}

	private void openFeedback() {
		Intent localIntent = new Intent(Intent.ACTION_SEND);
		//Todo : Remember to change this email
		localIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"iacoder@live.com"});
		localIntent.putExtra(Intent.EXTRA_CC, "");
		String appVersion;
		try {
			appVersion = context.get().getPackageManager().getPackageInfo(context.get().getPackageName(), 0).versionName;
			localIntent.putExtra(Intent.EXTRA_SUBJECT, context.get().getString(R.string.feedback_for_app));
			localIntent.putExtra(Intent.EXTRA_TEXT,
					context.get().getString(R.string.feedback_device_os) + " " +
							context.get().getString(R.string.feedback_device_version) + " " + Build.VERSION.RELEASE +
							context.get().getString(R.string.feedback_device_brand) + " " + Build.BRAND +
							context.get().getString(R.string.feedback_device_model) + " " + Build.MODEL +
							context.get().getString(R.string.feedback_device_manufacturer) + " " + Build.MANUFACTURER +
							context.get().getString(R.string.feedback_app_version) + " " + appVersion );

			localIntent.setType("message/rfc822");
			context.get().startActivity(Intent.createChooser(localIntent, context.get().getString(R.string.choose_email_client)));
		} catch (Exception e) {
			Log.d("OpenFeedback", e.getMessage());
		}
	}
}

