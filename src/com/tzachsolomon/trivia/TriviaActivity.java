package com.tzachsolomon.trivia;

import java.util.Locale;

import android.app.Activity;

import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TriviaActivity extends Activity implements OnClickListener {

	// TODO: highest score
	// TODO: Delete database
	// TODO: play categories
	// TODO: create service to update the database daily
	// TODO: initial settings with XML file

	public static final String TAG = TriviaActivity.class.getSimpleName();

	private Button buttonNewGame;
	private Button buttonManageDatabase;
	private Button buttonPreferences;
	private Button buttonNewGameLevels;

	private SharedPreferences m_SharedPreferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//PreferenceManager.setDefaultValues(this, R.xml.prefs, true);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

	}

	@Override
	protected void onStart() {
		//
		super.onStart();

	}

	@Override
	protected void onResume() {
		//
		super.onResume();

		if (m_SharedPreferences.getBoolean("showFirstTimeConfiguration", true)) {
			buttonWizardSetup_Clicked();
			m_SharedPreferences.edit()
					.putBoolean("showFirstTimeConfiguration", false).commit();
		}

		changeLanguageTo(m_SharedPreferences.getString("listPreferenceLanguages",
		 "iw"));
		
		

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

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonNewGame = (Button) findViewById(R.id.buttonNewGame);
		buttonManageDatabase = (Button) findViewById(R.id.buttonManageDatabase);
		buttonPreferences = (Button) findViewById(R.id.buttonPreferences);
		buttonNewGameLevels = (Button) findViewById(R.id.buttonNewGameLevels);

		buttonNewGame.setOnClickListener(this);
		buttonManageDatabase.setOnClickListener(this);
		buttonPreferences.setOnClickListener(this);
		buttonNewGameLevels.setOnClickListener(this);

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
		case R.id.buttonNewGame:
			buttonNewGame_Clicked();
			break;

		case R.id.buttonNewGameLevels:
			buttonNewGameLevels_Clicked();
			break;

		case R.id.buttonManageDatabase:
			buttonManageDatabase_Clicked();
			break;

		case R.id.buttonPreferences:
			buttonPreferences_Clicked();
			break;
		}

	}

	private void buttonWizardSetup_Clicked() {
		//
		Intent pref = new Intent(TriviaActivity.this, WizardSetup.class);

		startActivity(pref);

	}

	private void buttonNewGameLevels_Clicked() {
		//
		startNewGame(Game.GAMETYPE_LEVELS);
		// TODO: add start level, max level;

	}

	private void buttonPreferences_Clicked() {
		//
		menuItemPreferences_Clicked();

	}

	private void buttonManageDatabase_Clicked() {
		//
		Intent intent = new Intent(this, DatabaseMenuActivity.class);

		startActivity(intent);

	}

	private void buttonNewGame_Clicked() {
		//
		startNewGame(Game.GAMETYPE_ALL_QUESTIONS);

	}

	private void startNewGame(int i_GameType) {
		//

		Intent intent = new Intent(this, Game.class);
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
		Intent pref = new Intent(TriviaActivity.this, Prefs.class);

		startActivity(pref);

	}

	private void menuItemAbout_Clicked() {
		//

	}

}