package com.pharaohapp.apkxtractor;

import android.content.Context;
import android.os.AsyncTask;

import com.pharaohapp.apkxtractor.Models.Apk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FetchLibraryTask extends AsyncTask<Void,Void,ArrayList<Apk>> {

	private Listener listener;
	private FileUtils fileUtils;

	private WeakReference<Context> mContext;

	FetchLibraryTask(Context context) {
		mContext = new WeakReference<>(context);
	}

	@Override
	protected void onPreExecute() {
		fileUtils = new FileUtils(mContext.get());
	}

	@Override
	protected ArrayList<Apk> doInBackground(Void... voids) {

		return fileUtils.fetchFiles();

	}

	@Override
	protected void onPostExecute(ArrayList<Apk> apks) {
		if (listener != null) {
			listener.onSuccess(apks);
		}
	}

	void setListener(Listener listener) {
		this.listener = listener;
	}

	interface Listener {
		void onSuccess(ArrayList<Apk> apks);
	}

}
