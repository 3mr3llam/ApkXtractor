package com.pharaohapp.apkxtractor;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.pharaohapp.apkxtractor.Models.Apk;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LibraryFragment#getInstance} factory method to
 * get an instance of this fragment.
 */
public class LibraryFragment extends Fragment implements  FetchLibraryTask.Listener, ApkListAdapter.OnItemListClick {

	private ProgressBar progressBar;
	private ArrayList<Apk> apkList;
	private ApkListAdapter mAdapter;
	RecyclerView mRecyclerView;

	LinearLayoutManager mLinearLayoutManager;

	// handling ad and adapter callback listener
	private InterstitialAd mInterstitialAd;
	private AdView mAdView;
	boolean isExtract;
	int index;
	View rootView;

	public LibraryFragment() {
		// Required empty public constructor
	}

	/**
	 * Returns a new instance of this fragment
	 */
	public static LibraryFragment getInstance() {
		return new LibraryFragment();

//		if (mInstance == null)
//			mInstance = new LibraryFragment();
//
//		return mInstance;
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_library, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		apkList = new ArrayList<>();

		progressBar = view.findViewById(R.id.progress);

		mRecyclerView = view.findViewById(R.id.apk_list_rv);

		mAdView = view.findViewById(R.id.adView);

		mLinearLayoutManager = new LinearLayoutManager(requireActivity());

		fetchLibrary();

		initAds();
	}

	public void fetchLibrary() {
		FetchLibraryTask getApkTask = new FetchLibraryTask(getContext());
		getApkTask.setListener(this);
		getApkTask.execute();
	}

	public ApkListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void onSuccess(ArrayList<Apk> apks) {

		apkList = apks;
		mAdapter = new ApkListAdapter(requireActivity(), apkList, true, this);

		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(mAdapter);

		mAdapter.notifyDataSetChanged();
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onMenuClickListener(String packageName, View view) {

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
