package com.pharaohapp.apkxtractor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.pharaohapp.apkxtractor.Models.Apk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GetApkTask extends AsyncTask<Void, Void, ArrayList<Apk>> {
	private WeakReference<Context> context;


	private OnTaskListener onTaskListener;
	boolean systemAppsOrUserApps;

	public GetApkTask(Context context, boolean systemAppsOrUserApps) {

		this.context = new WeakReference<>(context);
		this.systemAppsOrUserApps = systemAppsOrUserApps;
	}


	@Override
	protected ArrayList<Apk> doInBackground(Void... voids) {
		Apk userApk;
		ArrayList<Apk> apkList = new ArrayList<>();

		List<PackageInfo> allPackages = context.get()
				.getPackageManager()
				.getInstalledPackages(PackageManager.GET_META_DATA);

		for(PackageInfo ai : allPackages) {
			ApplicationInfo applicationInfo = ai.applicationInfo;

			userApk = new Apk(
					applicationInfo,
					context.get().getPackageManager().getApplicationLabel(applicationInfo).toString(),
					ai.packageName,
					ai.versionName);
			apkList.add(userApk);
		}

		return apkList;
	}


	@Override
	protected void onPostExecute(ArrayList<Apk> apks) {
		super.onPostExecute(apks);

		ArrayList<Apk> systemApps = new ArrayList<>();
		ArrayList<Apk> userApps = new ArrayList<>();

		for (Apk apk : apks) {
			if (((apk.getAppInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0) ||
					( (apk.getAppInfo().flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {

				systemApps.add(apk);
			} else {

				userApps.add(apk);
			}
		}
		if (systemAppsOrUserApps) {

			if (onTaskListener != null)
				onTaskListener.onFinish(systemApps);
		} else {

			if (onTaskListener != null)
				onTaskListener.onFinish(userApps);
		}


		context.clear();
	}


	public void setOnTaskListener(OnTaskListener listener) {
		onTaskListener = listener;
	}

	public interface OnTaskListener {

		public void onFinish(ArrayList<Apk> apks);
	}
}