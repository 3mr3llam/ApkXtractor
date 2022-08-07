package com.pharaohapp.apkxtractor.Models;

import android.content.pm.ApplicationInfo;

public class Apk {

	private ApplicationInfo appInfo;
	private String appName, packageName, version;

	public Apk() {
	}

	public Apk(ApplicationInfo appInfo, String appName, String packageName, String version) {
		this.appInfo = appInfo;
		this.appName = appName;
		this.packageName = packageName;
		this.version = version;
	}

	public ApplicationInfo getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(ApplicationInfo appInfo) {
		this.appInfo = appInfo;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
