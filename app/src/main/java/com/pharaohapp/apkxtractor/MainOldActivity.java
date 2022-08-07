package com.pharaohapp.apkxtractor;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.pharaohapp.apkxtractor.Models.Apk;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainOldActivity extends AppCompatActivity implements ApkListAdapter.OnContextItemClickListener {

	private ProgressBar progressBar;
	private ArrayList<Apk> apkList;

	private String contextItemPackageName;

	private ApkListAdapter mAdapter;
	RecyclerView mRecyclerView;

	public DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBar actionbar;

	NavigationView navigationView;
	DrawerNavListener navViewListener;

	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_old);

		setupToolbar();

		setupDrawerNav();

		//initAdmob
		//initAdmob();

		apkList = new ArrayList<>();

		progressBar = findViewById(R.id.progress);
		Utilities.checkPermission(this);

		mRecyclerView = findViewById(R.id.apk_list_rv);
		LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
		//mAdapter = new ApkListAdapter(this, apkList, false);

		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setHasFixedSize(true);
		//mRecyclerView.setAdapter(mAdapter);

		loadApk();
	}

	private void initAdmob() {
//		MobileAds.initialize(this, getString(R.string.app_admob_id));
//		mAdView = findViewById(R.id.adView);
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_launch :
				try {
					startActivity(getPackageManager().getLaunchIntentForPackage(contextItemPackageName));

				} catch (Exception e) {
					Snackbar.make(
							findViewById(R.id.container),
							R.string.cant_open_app, Snackbar.LENGTH_SHORT).show();
				}
				break;

			case R.id.action_playstore :

				try {
					startActivity( new Intent(Intent.ACTION_VIEW,
							Uri.parse("market://details?id=$contextItemPackageName")));

				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(
									"https://play.google.com/store/apps/details?id=$contextItemPackageName")
					));

				}

				break;
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return false;
		}

		return super.onOptionsItemSelected(item);
	}

	private void loadApk() {
		GetApkTask getApkTask = new GetApkTask(this);
		getApkTask.execute();
	}

	protected void setupDrawerNav() {
		mDrawerLayout = findViewById(R.id.drawer_layout);

		navigationView = findViewById(R.id.nav_view);
		navViewListener = new DrawerNavListener(this, true);
		navigationView.setNavigationItemSelectedListener(navViewListener);

		mDrawerToggle = new ActionBarDrawerToggle(this,
				mDrawerLayout,
				R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {

				super.onDrawerOpened(drawerView);

				if (actionbar != null)
					actionbar.setTitle(R.string.more);

				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);

				if (actionbar != null)
					actionbar.setTitle(R.string.app_name);

				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.addDrawerListener(mDrawerToggle);
	}


	private void setupToolbar() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.app_name);
		setSupportActionBar(toolbar);
		actionbar = getSupportActionBar();

		if (actionbar != null) {
			actionbar.setDisplayHomeAsUpEnabled(true);
			actionbar.setHomeButtonEnabled(true);
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClicked(String packageName) {
		contextItemPackageName = packageName;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == Utilities.STORAGE_PERMISSION_CODE) {

			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Utilities.makeAppDir();
			} else {
				Snackbar.make(findViewById(R.id.container),
						"Permission required to extract apk", Snackbar.LENGTH_LONG).show();
			}

		}
	}


	class GetApkTask extends AsyncTask<Void, Void, Void>{
		List<PackageInfo> allPackages;
		private WeakReference<Context> context;

		GetApkTask(Context context) {

			this.context = new WeakReference<>(context);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			Apk userApk;
			allPackages = context.get()
					.getPackageManager()
					.getInstalledPackages(PackageManager.GET_META_DATA);

			for(PackageInfo ai : allPackages) {
				ApplicationInfo applicationInfo = ai.applicationInfo;

				userApk = new Apk(
						applicationInfo,
						getPackageManager().getApplicationLabel(applicationInfo).toString(),
						ai.packageName,
						ai.versionName);

				apkList.add(userApk);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			//mAdapter.notifyDataSetChanged();
			progressBar.setVisibility(View.GONE);
			context.clear();
		}
	}
}
