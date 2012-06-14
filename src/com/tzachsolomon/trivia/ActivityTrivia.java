package com.tzachsolomon.trivia;

import java.util.Locale;

import android.app.Activity;

import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityTrivia extends Activity implements OnClickListener {

	// TODO: highest score
	// TODO: play categories
	// TODO: create service to update the database daily
	// TODO: initial settings with XML file
	// TODO: 

	public static final String TAG = ActivityTrivia.class.getSimpleName();

	private Button buttonNewGameAllQuestions;
	private Button buttonManageDatabase;
	private Button buttonPreferences;
	private Button buttonNewGameSimple;
	private Button buttonUpdateDatabase;

	private SharedPreferences m_SharedPreferences;
	
	private UpdateManager m_UpdateManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		checkIfNeedToShowFirstTimeMessage();

	}


	@Override
	protected void onStart() {

		//
		super.onStart();

	}

	private void checkIfNeedToShowFirstTimeMessage() {
		//
		PackageInfo packageInfo = null;
		String i = m_SharedPreferences.getString("showFirstTimeMessageVersion",
				"1.0");

		try {
			packageInfo = getPackageManager().getPackageInfo(
					"com.tzachsolomon.trivia", PackageManager.GET_META_DATA);

			if (!packageInfo.versionName.contentEquals(i)) {
				showWizardSetup();
				m_SharedPreferences
						.edit()
						.putString("showFirstTimeMessageVersion",
								packageInfo.versionName).commit();
			}
		} catch (NameNotFoundException e) {
			//
			Log.e(TAG, "Could not get meta data info for Trivia");
		}

	}

	private void showWizardSetup() {
		//
		buttonWizardSetup_Clicked();
		m_SharedPreferences.edit()
				.putBoolean("showFirstTimeConfiguration", false).commit();
	}

	@Override
	protected void onResume() {
		//
		super.onResume();

		if (m_SharedPreferences.getBoolean("showFirstTimeConfiguration", true)) {
			showWizardSetup();
		}

		changeLanguageTo(m_SharedPreferences.getString(
				"listPreferenceLanguages", "iw"));

	}

	private void changeLanguageTo(String string) {
		//
		Locale locale = new Locale(string);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.main);
		initializeButtons();

	}

	private void initializeVariables() {
		//
		m_UpdateManager = new UpdateManager(this);

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonNewGameAllQuestions = (Button) findViewById(R.id.buttonNewGameAllQuestions);
		buttonManageDatabase = (Button) findViewById(R.id.buttonManageDatabase);
		buttonPreferences = (Button) findViewById(R.id.buttonPreferences);
		buttonNewGameSimple = (Button) findViewById(R.id.buttonNewGameSimple);
		buttonUpdateDatabase = (Button)findViewById(R.id.buttonUpdateDatabase);

		buttonNewGameAllQuestions.setOnClickListener(this);
		buttonManageDatabase.setOnClickListener(this);
		buttonPreferences.setOnClickListener(this);
		buttonNewGameSimple.setOnClickListener(this);
		buttonUpdateDatabase.setOnClickListener(this);

		// checking if the device is with API 11 and earlier,
		// if so, hide the preferences button since it can be done through menu
		// option
		if (android.os.Build.VERSION.SDK_INT < 11) {
			buttonPreferences.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonUpdateDatabase:
			buttonUpdateDatabase_Clicked();
			break;
			
		case R.id.buttonNewGameAllQuestions:
			buttonNewGameAllQuestions_Clicked();
			break;

		case R.id.buttonNewGameSimple:
			buttonNewGameSimple_Clicked();
			break;

		case R.id.buttonManageDatabase:
			buttonManageDatabase_Clicked();
			break;

		case R.id.buttonPreferences:
			buttonPreferences_Clicked();
			break;
		}

	}

	private void buttonUpdateDatabase_Clicked() {
		// 
		m_UpdateManager.updateNow();
		
	}


	private void buttonWizardSetup_Clicked() {
		//
		Intent pref = new Intent(ActivityTrivia.this, ActivityWizardSetup.class);

		startActivity(pref);

	}

	private void buttonNewGameSimple_Clicked() {
		//
		startNewGame(ActivityGame.GAMETYPE_LEVELS);
		// TODO: add start level, max level;

	}

	private void buttonPreferences_Clicked() {
		//
		menuItemPreferences_Clicked();

	}

	private void buttonManageDatabase_Clicked() {
		//
		Intent intent = new Intent(this, ActivityDatabaseMenu.class);

		startActivity(intent);

	}

	private void buttonNewGameAllQuestions_Clicked() {
		//
		startNewGame(ActivityGame.GAMETYPE_ALL_QUESTIONS);

	}

	private void startNewGame(int i_GameType) {
		//

		Intent intent = new Intent(this, ActivityGame.class);
		intent.putExtra("GameType", i_GameType);

		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//
		boolean ret = true;

		switch (item.getItemId()) {

		case R.id.menuItemAbout:
			menuItemAbout_Clicked();
			break;

		case R.id.menuItemExit:
			finish();
			break;

		case R.id.menuItemPreferences:
			menuItemPreferences_Clicked();
			break;

		default:
			ret = super.onOptionsItemSelected(item);

		}

		return ret;

	}

	private void menuItemPreferences_Clicked() {
		//
		Intent pref = new Intent(ActivityTrivia.this, ActivityPrefs.class);

		startActivity(pref);

	}

	private void menuItemAbout_Clicked() {
		//
		Intent intent = new Intent(ActivityTrivia.this, ActivityAbout.class);
		startActivity(intent);

	}

}