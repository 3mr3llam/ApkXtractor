package com.pharaohapp.apkxtractor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.pharaohapp.apkxtractor.Models.Apk;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FileUtils {

	private static final String TAG = "FileUtils";

	private final String SOURCE_PATH = Utilities.getAppFolder().getAbsolutePath();

	private WeakReference<Context> mContext;

	public FileUtils(Context context) {
		this.mContext = new WeakReference<>(context);
	}

	public ArrayList<Apk> fetchFiles() {
		ArrayList<Apk> apks = new ArrayList<>();
		File source = new File(SOURCE_PATH);
		File[] files;

		if (source.exists()) {
			files = source.listFiles();

			if (files != null && files.length>0){
				for (File file : files) {
					if (!file.getName().toLowerCase().contains(".apk"))
						continue;
					apks.add(getApkFromFile(file));
				}
			}
		}

		return apks;
	}

	private Apk getApkFromFile(File file) {

		PackageManager pm = mContext.get().getPackageManager();
		if (pm != null) {
			PackageInfo pi = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);

			// the secret are these two lines....
			pi.applicationInfo.sourceDir       = file.getAbsolutePath();
			pi.applicationInfo.publicSourceDir = file.getAbsolutePath();

			return new Apk(pi.applicationInfo, pi.applicationInfo.name, pi.packageName, pi.versionName);
		}
		return null;
	}
}
