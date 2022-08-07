package com.pharaohapp.apkxtractor;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.pharaohapp.apkxtractor.Models.Apk;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A Installed Apps fragment containing a simple view.
 */
public class UserInstalledAppsFragment extends Fragment implements GetApkTask.OnTaskListener, ApkListAdapter.OnItemListClick {

	private static final String TAG = "SystemInstalledAppsFragment";
	private static UserInstalledAppsFragment mInstance;

	public ProgressBar progressBar;
	private ArrayList<Apk> apkList;
	private ApkListAdapter mAdapter;
	RecyclerView mRecyclerView;

	LinearLayoutManager mLinearLayoutManager;

	LibraryFragment libraryFragment;

	PopupMenu popup;

	// handling ad and adapter callback listener
	private InterstitialAd mInterstitialAd;
	private AdView mAdView;
	boolean isExtract;
	int index;
	View rootView;

	public UserInstalledAppsFragment() {
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	/**
	 * Returns a new instance of this fragment
	 */
	public static UserInstalledAppsFragment getInstance() {
		return new UserInstalledAppsFragment();
//		if (mInstance == null)
//			mInstance = new UserInstalledAppsFragment();
//
//		return mInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_user_apps, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		apkList = new ArrayList<>();

		progressBar = view.findViewById(R.id.progress);

		mRecyclerView = view.findViewById(R.id.apk_list_rv);

		mAdView = view.findViewById(R.id.adView);

		mLinearLayoutManager = new LinearLayoutManager(requireActivity());

		loadApk();

		initAds();
	}

	private void loadApk() {
		GetApkTask getApkTask = new GetApkTask(getContext(), false);
		getApkTask.setOnTaskListener(this);
		getApkTask.execute();
	}

	public LibraryFragment getLibraryFragment() {
		return libraryFragment;
	}

	public void setLibraryFragment(LibraryFragment libraryFragment) {
		this.libraryFragment = libraryFragment;
	}

	@Override
	public void onFinish(ArrayList<Apk> apks) {

		apkList = apks;
		mAdapter = new ApkListAdapter(requireActivity(), apkList, false, this);

		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(mAdapter);

		mAdapter.notifyDataSetChanged();
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onMenuClickListener(final String packageName, View view) {
		//creating a popup menu
		popup = new PopupMenu(getContext(), view);
		//inflating menu from xml resource
		popup.inflate(R.menu.context_menu);
		//adding click listener
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_launch :
						try {
							startActivity(requireActivity().getPackageManager().getLaunchIntentForPackage(packageName));

						} catch (Exception e) {
							Toast.makeText(requireActivity(), R.string.cant_open_app, Toast.LENGTH_SHORT).show();
						}
						break;

					case R.id.action_playstore :

						openPlayStoreForApp(packageName);
						break;
				}
				return false;
			}
		});

		// TO MAKE THE MENU AWAY FROM THE SCREEN BORDERS
		popup.setGravity(Gravity.END);

		// if you want icon with menu items then write this try-catch block.
		try {
			Field[] fields = popup.getClass().getDeclaredFields();
			for (Field field : fields) {
				if ("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(popup);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper
							.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod(
							"setForceShowIcon", boolean.class);
					setForceIcons.invoke(menuPopupHelper, true);
					break;
				}
			}
		} catch (Exception e) {

		}

		//displaying the popup
		popup.show();
	}

	@Override
	public void onExtractListener(boolean isExtract, int index, View rootView) {
		this.isExtract = isExtract;
		this.index = index;
		this.rootView = rootView;

		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		} else {
			if (isExtract) {
				mAdapter.extractApk(index, rootView);
			} else {
				mAdapter.deleteApk(index, rootView);
			}
			Log.d("TAG", "The interstitial wasn't loaded yet.");
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private int getRateFlags() {
		int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
		return flags |= (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
				Intent.FLAG_ACTIVITY_NEW_DOCUMENT :
				Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;//noinspection deprecation
	}

	private void openPlayStoreForApp(String packageName) {
		Uri uri = Uri.parse("market://details?id=" + packageName);
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

		// To count with Play market backstack, After pressing back button,
		// to taken back to our application, we need to add following flags to intent.
		goToMarket.addFlags(getRateFlags());
		try {
			requireActivity().startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			try {
				requireActivity().startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
			} catch (ActivityNotFoundException ex) {
				Log.e("rateThisApp", "rateThisApp: R.string.error_no_app_found ");
			}
		}
	}

	private void initAds() {
		MobileAds.initialize(requireActivity(), new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {}
		});
		mInterstitialAd = new InterstitialAd(requireActivity());

		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		mInterstitialAd.setAdUnitId(getString(R.string.test_admob_interstitial));
		mInterstitialAd.loadAd(new AdRequest.Builder().build());

		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
			}

			@Override
			public void onAdFailedToLoad(LoadAdError adError) {
				// Code to be executed when an ad request fails.
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when the ad is displayed.
			}

			@Override
			public void onAdClicked() {
				// Code to be executed when the user clicks on an ad.
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when the interstitial ad is closed.
				mInterstitialAd.loadAd(new AdRequest.Builder().build());

				if (isExtract) {
					mAdapter.extractApk(index, rootView);
				} else {
					mAdapter.deleteApk(index, rootView);
				}
			}
		});
	}
}
