package com.bitpops.barcode.Helpers;

import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bitpops.barcode.Activities.MainActivity;

public class SavedData {

    Context ct;

    public SavedData(Context ct) {
        this.ct = ct;
    }

    public String getServerAPIAddress() {
        return loadString("ApiUrl");
    }
    public String getDeviceAuthToken() {
        return loadString("DeviceAuthToken");
    }
    public String getDeviceId() {
        return loadString("DeviceId");
    }
    public String getCompanyName() {
        return loadString("CompanyName");
    }

    public void setServerAPIAddress(String serverAPIAddress) {
        saveString("ApiUrl", serverAPIAddress);
    }
    public void setDeviceAuthToken(String deviceAuthToken) {
        saveString("DeviceAuthToken", deviceAuthToken);
    }

    public void setDeviceId(String deviceId) {
        saveString("DeviceId", deviceId);
    }
    public void setCompanyName(String companyName) {
        saveString("CompanyName", companyName);
    }

    private String loadString(String key)
    {
        return PreferenceManager.getDefaultSharedPreferences(ct).getString(key, "");
    }
    private void saveString(String key, String value)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ct);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }


}
