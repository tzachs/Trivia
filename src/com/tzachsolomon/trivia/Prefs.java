package com.tzachsolomon.trivia;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class Prefs extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private Preference checkBoxPreferenceAllowUpdateMobileNetwork;
	private Preference checkBoxPreferenceAllowUpdateMobileNetwork3G;
	private Preference checkBoxPreferenceAllowUpdateMobileNetworkRoaming;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);
		
		checkBoxPreferenceAllowUpdateMobileNetwork = (Preference)findPreference("checkBoxPreferenceAllowUpdateMobileNetwork");
		checkBoxPreferenceAllowUpdateMobileNetwork3G = (Preference) findPreference("checkBoxPreferenceAllowUpdateMobileNetwork3G");
		checkBoxPreferenceAllowUpdateMobileNetworkRoaming = (Preference) findPreference("checkBoxPreferenceAllowUpdateMobileNetworkRoaming");
		
		checkBoxPreferenceAllowUpdateMobileNetwork3G.setEnabled(checkBoxPreferenceAllowUpdateMobileNetwork.isEnabled());
		checkBoxPreferenceAllowUpdateMobileNetworkRoaming.setEnabled(checkBoxPreferenceAllowUpdateMobileNetwork.isEnabled());

	}
	
	@Override
	protected void onResume() {
		// 
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		// 
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		//
		boolean answer;
		if (key.contentEquals("checkBoxPreferenceAllowUpdateMobileNetwork")){
			
			answer = sharedPreferences.getBoolean("checkBoxPreferenceAllowUpdateMobileNetwork", false);
			checkBoxPreferenceAllowUpdateMobileNetwork3G.setEnabled(answer);
			checkBoxPreferenceAllowUpdateMobileNetworkRoaming.setEnabled(answer);
			
		}

	}

}
