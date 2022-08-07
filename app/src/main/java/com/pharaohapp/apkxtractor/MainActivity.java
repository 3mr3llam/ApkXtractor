package com.pharaohapp.apkxtractor;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.dxtt.coolmenu.CoolMenuFrameLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

public class MainActivity extends AppCompatActivity implements ApkListAdapter.OnContextItemClickListener {

	private String contextItemPackageName;

	BackButtonsFragment backButtonsFragment;
	SystemInstalledAppsFragment systemInstalledAppsFragment;
	UserInstalledAppsFragment userInstalledAppsFragment;
	LibraryFragment libraryFragment;

	CoolMenuFrameLayout coolMenuLayout;
	List<Fragment> fragmentList = new ArrayList<>();
	List<String> titleList = null;
	List<Integer> colorList = null;

	public final static int STORAGE_PERMISSION_CODE = 1008;
	public static String[] PERMISSIONS_ACCESS_EXTERNAL = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.REQUEST_DELETE_PACKAGES};
	private static ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract;
	private static ActivityResultLauncher<String[]> multiplePermissionLauncher;

	public final static int UNINSTALL_APP_CODE = 304;

	public boolean checkPermission() {
		boolean permissionGranted = false;
		multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
		multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, isGranted -> {
			Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
			if (isGranted.containsValue(false)) {
				Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
				multiplePermissionLauncher.launch(PERMISSIONS_ACCESS_EXTERNAL);
			}
		});
		permissionGranted = Utilities.askPermissions(multiplePermissionLauncher, this);
		return permissionGranted;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		boolean permissionGranted = false;
		multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
		multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, isGranted -> {
			Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
			if (isGranted.containsValue(false)) {
				Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
				multiplePermissionLauncher.launch(PERMISSIONS_ACCESS_EXTERNAL);
			}
		});
		permissionGranted = Utilities.askPermissions(multiplePermissionLauncher, this);

		/////////////////////////////////////////

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.

		setupCoolMenus();
	}

	private void setupCoolMenus() {
		String[] titles = {getString(R.string.more), getString(R.string.library),getString(R.string.system_apps), getString(R.string.user_apps)};
		Integer[] colors = {ResourcesCompat.getColor(getResources(), R.color.textAlt2, null),
				ResourcesCompat.getColor(getResources(), R.color.textAlt2, null),
				ResourcesCompat.getColor(getResources(), R.color.textAlt2, null),
				ResourcesCompat.getColor(getResources(), R.color.textAlt2, null)};
		titleList = Arrays.asList(titles);
		colorList = Arrays.asList(colors);

		coolMenuLayout = findViewById(R.id.cool_menu_layout);
		coolMenuLayout.setTitles(titleList);
		coolMenuLayout.setMenuIcon(R.drawable.ic_menu);
		coolMenuLayout.setMenuTitleColor(colorList);

		backButtonsFragment = BackButtonsFragment.getInstance();
		systemInstalledAppsFragment = SystemInstalledAppsFragment.getInstance();
		userInstalledAppsFragment = UserInstalledAppsFragment.getInstance();
		libraryFragment = LibraryFragment.getInstance();

		systemInstalledAppsFragment.setLibraryFragment(libraryFragment);
		userInstalledAppsFragment.setLibraryFragment(libraryFragment);

		fragmentList.add(backButtonsFragment);
		fragmentList.add(libraryFragment);
		fragmentList.add(systemInstalledAppsFragment);
		fragmentList.add(userInstalledAppsFragment);

		PagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
			@NonNull
			@Override
			public Fragment getItem(int position) {
				return fragmentList.get(position);
			}

			@Override
			public int getCount() {
				return fragmentList.size();
			}
		};

		coolMenuLayout.setAdapter(adapter);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		/*switch (item.getItemId()) {

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
		}*/

		return true;
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
						getString(R.string.storage_perms_req), Snackbar.LENGTH_LONG).show();
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		 if (requestCode == UNINSTALL_APP_CODE) {
			 if(resultCode == RESULT_OK){
				 //Deleted
			 }
			 if (resultCode == RESULT_CANCELED) {
				 //Dismissed
			 }
		}
	}
}
