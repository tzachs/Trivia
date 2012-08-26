package com.tzachsolomon.trivia;





import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;


import android.preference.PreferenceCategory;

import android.preference.PreferenceActivity;



public class ActivityPrefs extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private CheckBoxPreference checkBoxPreferenceAllowUpdateMobileNetwork;
	private CheckBoxPreference checkBoxPreferenceAllowUpdateMobileNetwork3G;
	private CheckBoxPreference checkBoxPreferenceAllowUpdateMobileNetworkRoaming;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);

		try{
			addPreferencesFromResource(R.xml.prefs);
		}catch (Exception e){
			// TODO: in case error, restore to defaults and print a message about it
			
		}
		
		
		checkBoxPreferenceAllowUpdateMobileNetwork = (CheckBoxPreference) findPreference("checkBoxPreferenceAllowUpdateMobileNetwork");

		checkBoxPreferenceAllowUpdateMobileNetwork3G = (CheckBoxPreference) findPreference("checkBoxPreferenceAllowUpdateMobileNetwork3G");
		checkBoxPreferenceAllowUpdateMobileNetworkRoaming = (CheckBoxPreference) findPreference("checkBoxPreferenceAllowUpdateMobileNetworkRoaming");

		setAllowUpdateMobileOptionsAccordingToState(checkBoxPreferenceAllowUpdateMobileNetwork
				.isChecked());

	}

	@Override
	protected void onResume() {
		//
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		
		PreferenceCategory invisible = (PreferenceCategory)findPreference("invisible");
		
		invisible.removeAll();
		
		
	}

	@Override
	protected void onPause() {
		//
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		//
		boolean answer;
		if (key.contentEquals("checkBoxPreferenceAllowUpdateMobileNetwork")) {

			answer = sharedPreferences.getBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork", false);
			setAllowUpdateMobileOptionsAccordingToState(answer);
		} 

	}


	private void setAllowUpdateMobileOptionsAccordingToState(boolean i_State) {

		checkBoxPreferenceAllowUpdateMobileNetwork3G.setEnabled(i_State);
		checkBoxPreferenceAllowUpdateMobileNetworkRoaming.setEnabled(i_State);
	}
	
}
