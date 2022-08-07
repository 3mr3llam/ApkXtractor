package com.pharaohapp.apkxtractor;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class BackButtonsFragment extends Fragment {

	public BackButtonsFragment() {
		// Required empty public constructor
	}


	public static BackButtonsFragment getInstance() {
		return new BackButtonsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_back_buttons, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView rate = view.findViewById(R.id.rate_app);
		final TextView feedback = view.findViewById(R.id.feedback);
		TextView privacy = view.findViewById(R.id.privacy);
		TextView ourApps = view.findViewById(R.id.our_apps);

		rate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				rateThisApp();
			}
		});

		feedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openFeedback();
			}
		});

		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openPrivacyPolicy();
			}
		});

		ourApps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openDeveloperPage();
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private int getRateFlags() {
		int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
		return flags |= (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
				Intent.FLAG_ACTIVITY_NEW_DOCUMENT :
				Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;//noinspection deprecation
	}

	private void rateThisApp() {
		Uri uri = Uri.parse("market://details?id=" + requireActivity().getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

		// To count with Play market backstack, After pressing back button,
		// to taken back to our application, we need to add following flags to intent.
		goToMarket.addFlags(getRateFlags());
		try {
			requireActivity().startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			try {
				requireActivity().startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
			} catch (ActivityNotFoundException ex) {
				Log.e("rateThisApp", "rateThisApp: app not found");
			}
		}
	}

	private void openFeedback() {
		Intent localIntent = new Intent(Intent.ACTION_SEND);

		localIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"lab.appton@gmail.com"});
		localIntent.putExtra(Intent.EXTRA_CC, "");
		String appVersion;
		try {
			appVersion = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName;
			localIntent.putExtra(Intent.EXTRA_SUBJECT, requireActivity().getString(R.string.feedback_for_app));
			localIntent.putExtra(Intent.EXTRA_TEXT,
					requireActivity().getString(R.string.feedback_device_os) + " " +
							requireActivity().getString(R.string.feedback_device_version) + " " + Build.VERSION.RELEASE +
							requireActivity().getString(R.string.feedback_device_brand) + " " + Build.BRAND +
							requireActivity().getString(R.string.feedback_device_model) + " " + Build.MODEL +
							requireActivity().getString(R.string.feedback_device_manufacturer) + " " + Build.MANUFACTURER +
							requireActivity().getString(R.string.feedback_app_version) + " " + appVersion );

			localIntent.setType("message/rfc822");
			requireActivity().startActivity(Intent.createChooser(localIntent, requireActivity().getString(R.string.choose_email_client)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openPrivacyPolicy() {
		Intent i = new Intent(requireActivity(), PrivacyActivity.class);
		requireActivity().startActivity(i);
	}

	private void openDeveloperPage() {
		Uri uri = Uri.parse("https://play.google.com/store/apps/dev?id=7197584919559775497");
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

		// To count with Play market backstack, After pressing back button,
		// to taken back to our application, we need to add following flags to intent.
		goToMarket.addFlags(getRateFlags());
		try {
			requireActivity().startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}
}