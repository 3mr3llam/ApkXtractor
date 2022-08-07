package com.pharaohapp.apkxtractor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.pharaohapp.apkxtractor.Models.Apk;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ApkListAdapter extends RecyclerView.Adapter<ApkListAdapter.AdapterViewHolder> {
	private static final String TAG = "ApkListAdapter";

	private Context mContext;
	private ArrayList<Apk> apkList;
	private MainActivity activity;
	private OnContextItemClickListener contextItemClickListener;
	private OnItemListClick onItemListClickListener;

	private boolean isLibrary;
	private ArrayList<Apk> libraryList;

	Fragment fragment;
	AdapterViewHolder adapterViewHolder;

	ApkListAdapter(Context context, ArrayList<Apk> apkList, boolean isLibrary, Fragment fragment) {
		this.mContext = context;
		this.apkList = apkList;
		this.isLibrary = isLibrary;
		this.activity = (MainActivity) context;
		this.fragment = fragment;
		contextItemClickListener = activity;
		onItemListClickListener = (OnItemListClick) fragment;

		libraryList = new ArrayList<>();
	}

	@NonNull
	@Override
	public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = LayoutInflater
				.from(mContext)
				.inflate(R.layout.apk_item, viewGroup, false);

		adapterViewHolder = new AdapterViewHolder(view);
		return adapterViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull AdapterViewHolder holder, int position) {
		holder.apkIcon
				.setImageDrawable(
						mContext.getPackageManager()
								.getApplicationIcon(apkList.get(position).getAppInfo()));

		holder.apkTitle
				.setText(
						mContext.getPackageManager()
								.getApplicationLabel(apkList.get(position).getAppInfo()).toString());

		holder.apkPackage
				.setText(
						apkList.get(position).getPackageName());


	}

	@Override
	public int getItemCount() {
		return apkList.size();
	}


	public void addToLibraryList(Apk apk) {
		libraryList.add(apk);
	}

	public ArrayList<Apk> getLibraryList() {
		return libraryList;
	}

	class AdapterViewHolder extends RecyclerView.ViewHolder {

		ImageView apkIcon;

		TextView apkTitle, apkPackage;

		ImageButton menuBtn;

		Button extract, shareApk, uninstall;
		View rootView;

		AdapterViewHolder(@NonNull View view) {
			super(view);

			apkIcon = view.findViewById(R.id.apk_icon_iv);
			apkTitle = view.findViewById(R.id.apk_label_tv);
			apkPackage = view.findViewById(R.id.apk_package_tv);

			extract = view.findViewById(R.id.extract_btn);
			shareApk = view.findViewById(R.id.share_btn);
			uninstall = view.findViewById(R.id.uninstall_btn);
			menuBtn = view.findViewById(R.id.menu_btn);

			if (isLibrary) {
				menuBtn.setVisibility(View.INVISIBLE);
				extract.setText(R.string.delete);
				uninstall.setText(R.string.install);
			}

			//activity.registerForContextMenu(menuBtn);
			rootView = activity.findViewById(R.id.container);

			extract.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//if (Utilities.checkPermission(activity)) {

						if (isLibrary) {
							onItemListClickListener.onExtractListener(false, getAdapterPosition(), rootView);
							//deleteApk(getAdapterPosition(), rootView);
						} else {
							onItemListClickListener.onExtractListener(true, getAdapterPosition(), rootView);
							//extractApk(getAdapterPosition(), rootView);
						}
					//}
				}
			});

			shareApk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					//if (activity.checkPermission()) {
						showHideProgressbar(View.VISIBLE);
						shareApk( getAdapterPosition());
					//}
				}
			});

			uninstall.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					if (isLibrary) {
						// install apk
						InstallApk(getAdapterPosition(), view.getContext());
					} else {
						//uninstall app
						uninstallApk(getAdapterPosition(), activity);
					}

				}
			});

			menuBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					onItemListClickListener.onMenuClickListener(apkList.get(getAdapterPosition()).getPackageName(), view);

//					contextItemClickListener.onItemClicked(
//							apkList.get(getAdapterPosition())
//									.getPackageName());

//					activity.openContextMenu(menuBtn);
//
//					menuBtn.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//						@Override
//						public void onCreateContextMenu(ContextMenu contextMenu,
//														View view,
//														ContextMenu.ContextMenuInfo contextMenuInfo) {
//
//							activity.getMenuInflater().inflate(R.menu.context_menu, contextMenu);
//						}
//					});
				}
			});
		}

		public void disableButtons() {
			extract.setEnabled(false);
			shareApk.setEnabled(false);
			uninstall.setEnabled(false);
		}

		public void enableButtons() {
			extract.setEnabled(true);
			shareApk.setEnabled(true);
			uninstall.setEnabled(true);
		}

	}

	class ExtractApkTask extends AsyncTask<Apk, Void, Void> {

		int position;
		View rootView;

		ExtractApkTask(int position, View rootView) {
			this.position = position;
			this.rootView = rootView;
		}

		@Override
		protected void onPreExecute() {
			showHideProgressbar(View.VISIBLE);
			adapterViewHolder.disableButtons();
			notifyDataSetChanged();
		}

		@Override
		protected Void doInBackground(Apk... apks) {
			Log.e(TAG, "doInBackground: " + apkList.get(position).toString() );
			Utilities.extractApk(this.rootView.getContext(), apkList.get(position));
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			notifyDataSetChanged();

			// add to library fragment
			addToLibrary(apkList.get(position));

			Snackbar.make(rootView,
					apkList.get(position).getAppName()
							+ " " + activity.getString(R.string.apk_extracted_success),
					Snackbar.LENGTH_LONG).show();
			showHideProgressbar(View.GONE);

			adapterViewHolder.enableButtons();

		}
	}

	public void extractApk(int index, View rootView) {
		new ExtractApkTask(index, rootView).execute();
	}

	public void shareApk(final int index) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean extract = true;

				if ( (fragment instanceof SystemInstalledAppsFragment && ((SystemInstalledAppsFragment)fragment)
						.getLibraryFragment()
						.getAdapter()
						.apkList.contains(apkList.get(index))) ||

						(fragment instanceof UserInstalledAppsFragment && ((UserInstalledAppsFragment)fragment)
								.getLibraryFragment()
								.getAdapter()
								.apkList.contains(apkList.get(index))) ||

						(fragment instanceof LibraryFragment &&
								apkList.contains(apkList.get(index))) ) {

					extract = false;
				}

				Intent intent = Utilities.getShareableIntent(mContext.getApplicationContext(),
						apkList.get(index), extract);


				addToLibrary(apkList.get(index));

				fragment.requireActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showHideProgressbar(View.GONE);
					}
				});

				activity.startActivity(Intent.createChooser(intent,
						activity.getString(R.string.share_the_apk)));
			}
		}).start();
	}

	public void InstallApk(int index, Context context) {
		File filePath = new File(apkList.get(index).getAppInfo().publicSourceDir);
		filePath.setReadable(true, false);
		Uri uri = Uri.fromFile(filePath);

		if (Build.VERSION.SDK_INT >= 24) {
			uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", filePath);
			Log.e(TAG, "InstallApk: " + uri.toString());
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
		intent.setDataAndType( uri, "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		mContext.startActivity(intent);
	}

	public void uninstallApk(int index, MainActivity activity) {
		Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
		uninstallIntent.setData(Uri.parse("package:" + apkList.get(index).getPackageName()));

		uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
		activity.startActivity(uninstallIntent);
		apkList.remove(index);
		notifyItemRemoved(index);
	}

	public void deleteApk(int position, View rootView) {

		File file = new File(apkList.get(position).getAppInfo().publicSourceDir);
		String apkName = file.getName();

		if (file.delete()) {
			apkList.remove(position);
			notifyItemRemoved(position);

			Snackbar.make(rootView,
					apkName + " " +
							activity.getString(R.string.apk_deleted_success),
					Snackbar.LENGTH_LONG).show();
		}

	}

	private void showHideProgressbar(int gone) {
		if (fragment instanceof SystemInstalledAppsFragment) {
			((SystemInstalledAppsFragment) fragment).progressBar.setVisibility(gone);
		} else if (fragment instanceof UserInstalledAppsFragment) {
			((UserInstalledAppsFragment) fragment).progressBar.setVisibility(gone);
		}
	}

	private void addToLibrary(Apk apk) {


		if ( fragment instanceof SystemInstalledAppsFragment && !((SystemInstalledAppsFragment)fragment)
				.getLibraryFragment()
				.getAdapter()
				.apkList.contains(apk) ||

				fragment instanceof UserInstalledAppsFragment && !((UserInstalledAppsFragment)fragment)
						.getLibraryFragment()
						.getAdapter()
						.apkList.contains(apk)) {

			if (fragment instanceof  SystemInstalledAppsFragment) {
				((SystemInstalledAppsFragment)fragment)
						.getLibraryFragment()
						.fetchLibrary();
			} else if ( fragment instanceof UserInstalledAppsFragment) {
				((UserInstalledAppsFragment)fragment)
						.getLibraryFragment()
						.fetchLibrary();
			}



		}
	}

	public interface OnItemListClick{
		void onMenuClickListener(String packageName, View view);
		void onExtractListener(boolean isExtract, int index, View rootView);
	}

	interface OnContextItemClickListener {
		void onItemClicked(String packageName);
	}
}
