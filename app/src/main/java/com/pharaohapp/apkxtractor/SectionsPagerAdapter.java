package com.pharaohapp.apkxtractor;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

	private final List<Fragment> mFragmentList = new ArrayList<>();
	private final List<String> mFragmentTitleList = new ArrayList<>();


	SectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}


	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

	public void addFragment(Fragment fragment, String title) {
		mFragmentList.add(fragment);
		mFragmentTitleList.add(title);
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return mFragmentTitleList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}


	/**
	 * This is the solution for the error class not found FragmentManagerState
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final Object fragment = super.instantiateItem(container, position);
		try {
			final Field saveFragmentStateField = Fragment.class.getDeclaredField("mSavedFragmentState");
			saveFragmentStateField.setAccessible(true);
			final Bundle savedFragmentState = (Bundle) saveFragmentStateField.get(fragment);
			if (savedFragmentState != null) {
				savedFragmentState.setClassLoader(Fragment.class.getClassLoader());
			}
		} catch (Exception e) {
			Log.w("CustomFragment", "Could not get mSavedFragmentState field", e);
		}
		return fragment;
	}
}