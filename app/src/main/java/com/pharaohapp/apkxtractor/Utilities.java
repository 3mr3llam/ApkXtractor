package com.pharaohapp.apkxtractor;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.pharaohapp.apkxtractor.Models.Apk;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

class Utilities {

	public final static int STORAGE_PERMISSION_CODE = 1008;
	private static final String TAG = "Utilities";
	public static String[] PERMISSIONS_ACCESS_EXTERNAL = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
	private static ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract;
	private static ActivityResultLauncher<String[]> multiplePermissionLauncher;

	public static boolean checkPermission(final AppCompatActivity activity) {
		boolean permissionGranted = false;
		multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
		multiplePermissionLauncher = activity.registerForActivityResult(multiplePermissionsContract, isGranted -> {
			Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
			if (isGranted.containsValue(false)) {
				Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
				multiplePermissionLauncher.launch(PERMISSIONS_ACCESS_EXTERNAL);
			}
		});
		permissionGranted = askPermissions(multiplePermissionLauncher, activity);

/*
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

			// BEGIN_INCLUDE(Storage_permission_request)
			if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
					PERMISSIONS_ACCESS_EXTERNAL[0]) && ActivityCompat.shouldShowRequestPermissionRationale(activity,
					PERMISSIONS_ACCESS_EXTERNAL[1])) {
				// Provide an additional rationale to the user if the permission was not granted
				// and the user would benefit from additional context for the use of the permission.
				// For example if the user has previously denied the permission.
				Snackbar.make(((MainActivity) activity).findViewById(R.id.container),
						R.string.storage_perms_req,
						Snackbar.LENGTH_INDEFINITE)
						.setAction(android.R.string.ok, new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								ActivityCompat.requestPermissions(activity,
										PERMISSIONS_ACCESS_EXTERNAL,
										STORAGE_PERMISSION_CODE);
							}
						})
						.show();
			} else {

				// Storage permission has not been granted yet. Request it directly.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
					try {
						Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
						Intent i = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
						i.addCategory(Intent.CATEGORY_DEFAULT);
						i.setData(uri);
//						((MainActivity) activity).startActivityForResult(i, STORAGE_PERMISSION_CODE);

					} catch(Exception e) {
						Intent i = new Intent();
						i.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
						((MainActivity) activity).startActivityForResult(i, STORAGE_PERMISSION_CODE);
					}
				} else {
					ActivityCompat.requestPermissions(activity, PERMISSIONS_ACCESS_EXTERNAL, STORAGE_PERMISSION_CODE);
				}
			}
		} else {
			permissionGranted = true;
		}*/

		return permissionGranted;
	}

	public static boolean askPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher, AppCompatActivity activity) {
		if (!hasPermissions(PERMISSIONS_ACCESS_EXTERNAL, activity)) {
			Log.d("PERMISSIONS", "Launching multiple contract permission launcher for ALL required permissions");
			multiplePermissionLauncher.launch(PERMISSIONS_ACCESS_EXTERNAL);
		} else {
			Log.d("PERMISSIONS", "All permissions are already granted");
			return true;
		}
		return false;
	}

	public static boolean hasPermissions(String[] permissions, AppCompatActivity activity) {
		if (permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
					Log.d("PERMISSIONS", "Permission is not granted: " + permission);
					return false;
				}
				Log.d("PERMISSIONS", "Permission already granted: " + permission);
			}
			return true;
		}
		return false;
	}

	static  boolean checkExternalStorage() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	static File getAppFolder() {
		File file = null;
		if (checkExternalStorage()) {
//			file = new File(Environment.getExternalStorageDirectory(), "ApkXtractor");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
			{
				file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/ApkXtractor" );
			}
			else
			{
				file = new File(Environment.getExternalStorageDirectory() + "/ApkXtractor");
			}
		}
		return file;
	}

	static void makeAppDir() {
		File file = getAppFolder();
		if (file != null && !file.exists()) {
			file.mkdir();
		}
	}

	static boolean extractApk(Context mContext, Apk apk) {
		makeAppDir();
		File originalFile = new File(apk.getAppInfo().sourceDir);
		Log.e(TAG, "extractApk: "+ originalFile.getAbsolutePath() );

		File extractedFile = getApkFile(apk);

		Log.e(TAG, "extractApk: "+ extractedFile.getAbsolutePath() );
		try {
			if (Environment.isExternalStorageEmulated()) {
				FileUtils.copyFile(originalFile, extractedFile);
			} else {
				copyFileToInternalStorage(mContext, Uri.fromFile(originalFile), extractedFile.getPath());
			}
			return true;
		} catch (Exception e) {
			Log.d("test", "problem - " + e.getMessage());
			return false;
		}
	}

	static File getApkFile(Apk apk) {
		String fileName = getAppFolder()
				.getPath()
				+ File.separator + apk.getAppName()
				+ "_" + apk.getVersion() + ".apk";
		Log.e(TAG, "getApkFile: " + fileName);
		return new File(fileName);
	}

	private static String copyFileToInternalStorage(Context mContext, Uri uri, String newDirName) {
		Uri returnUri = uri;

		Cursor returnCursor = mContext.getContentResolver().query(returnUri, new String[]{
				OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
		}, null, null, null);
		File output = null;

		try {

			/*
			 * Get the column indexes of the data in the Cursor,
			 *     * move to the first row in the Cursor, get the data,
			 *     * and display it.
			 * */
			int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
			int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
			returnCursor.moveToFirst();
			String name = (returnCursor.getString(nameIndex));
			String size = (Long.toString(returnCursor.getLong(sizeIndex)));

			if (!newDirName.equals("")) {
				File dir = new File(newDirName);
				if (!dir.exists()) {
					boolean res = dir.mkdirs();
				}
				output = new File(newDirName + "/" + name);
			} else {
				output = new File(mContext.getFilesDir() + "/" + name);
			}

			InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
			FileOutputStream outputStream = new FileOutputStream(output);
			int read = 0;
			int bufferSize = 1024;
			final byte[] buffers = new byte[bufferSize];
			while ((read = inputStream.read(buffers)) != -1) {
				outputStream.write(buffers, 0, read);
			}

			inputStream.close();
			outputStream.close();
		} catch( NullPointerException e) {
			Log.e("copyFileToStorage: ", e.getMessage() );
		} catch (Exception e) {

			Log.e("Exception", e.getMessage());
		}

		return output != null ? output.getPath() : "";
	}

	static Intent getShareableIntent(Context context, final Apk apk, final boolean extract) {
		// USE LATCH TO MAKE THE UI THREAD WAIT TILL THE OTHER THREAD FINISHED ITS WORK
		final CountDownLatch latch = new CountDownLatch(1);

		if (extract) {

//			Thread thread = new Thread(new Runnable() {
//				@Override
//				public void run() {
//
//					latch.countDown();
//				}
//			});
//			thread.start();
			extractApk(context,apk);
		}

		// USE LATCH TO MAKE THE UI THREAD WAIT TILL THE OTHER THREAD FINISHED ITS WORK
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		File file = getApkFile(apk);
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getPackageName()+".fileprovider", file));
//		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		shareIntent.setType("application/vnd.android.package-archive");
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return shareIntent;
	}

}
