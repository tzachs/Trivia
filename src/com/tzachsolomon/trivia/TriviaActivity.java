package com.tzachsolomon.trivia;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.ProgressDialog;

import android.content.ContentValues;
import android.content.DialogInterface;

import android.content.Intent;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TriviaActivity extends Activity implements OnClickListener {

	// TODO: different game options
	// TODO: highest score
	// TODO: Delete database
	// TODO: play categories
	// TODO: play till you die
	// TODO: play in increasing levels
	// TODO: create service to update the database daily

	public static final String TAG = TriviaActivity.class.getSimpleName();

	private Button buttonNewGame;
	private Button buttonUpdateDatabase;
	private Button buttonPreferences;
	private Button buttonNewGameLevels;

	private TriviaDbEngine m_TriviaDb;
	private JSONHandler m_JSONHandler;

	private SharedPreferences m_SharedPreferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		PreferenceManager.setDefaultValues(this, R.xml.prefs, true);

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

	}

	private void checkIsUpdateAvailableAsync(boolean i_DisplayInfoIfNoUpdate) {
		new AsyncTaskCheckUpdateIsAvailable().execute(i_DisplayInfoIfNoUpdate);
	}

	private void initializeVariables() {
		//
		m_TriviaDb = new TriviaDbEngine(this);

		//m_TriviaDb.deleteQuestions();

		m_JSONHandler = new JSONHandler(TriviaActivity.this);

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonNewGame = (Button) findViewById(R.id.buttonNewGame);
		buttonUpdateDatabase = (Button) findViewById(R.id.buttonUpdateDatabase);
		buttonPreferences = (Button) findViewById(R.id.buttonPreferences);
		buttonNewGameLevels = (Button) findViewById(R.id.buttonNewGameLevels);

		buttonNewGame.setOnClickListener(this);
		buttonUpdateDatabase.setOnClickListener(this);
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

		case R.id.buttonUpdateDatabase:
			buttonUpdateDatabase_Clicked();
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

	private void buttonUpdateDatabase_Clicked() {
		//
		// checking if to upload user correct wrong statistics before
		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceUploadCorrectWrongUserStat", true)) {
			new AsyncTaskUpdateCorrectWrongAsync().execute();
		} else {
			checkIsUpdateAvailableAsync(true);
		}

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

	public class AsyncTaskUpdateCorrectWrongAsync extends
			AsyncTask<Void, Integer, String> {

		boolean enabled;
		ContentValues[] wrongCorrectStat;
		private ProgressDialog m_ProgressDialog;

		@Override
		protected void onPreExecute() {
			StringBuilder detailedResult = new StringBuilder();
			//

			enabled = m_JSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {

				m_ProgressDialog = new ProgressDialog(TriviaActivity.this);
				m_ProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_ProgressDialog.setTitle("Uploading correct wrong statistics");
				m_ProgressDialog.show();
			} else {
				Toast.makeText(TriviaActivity.this, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (enabled) {
				if (result.length() > 0) {
					Toast.makeText(TriviaActivity.this, result,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(TriviaActivity.this,
							"Thank you for making this trivia better! :)",
							Toast.LENGTH_SHORT).show();
				}

				m_ProgressDialog.dismiss();
				checkIsUpdateAvailableAsync(true);
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			//
			StringBuilder sb = new StringBuilder();

			if (enabled) {
				int i = 0;
				int length;
				wrongCorrectStat = m_TriviaDb.getWrongCorrectStat();

				length = wrongCorrectStat.length;
				m_ProgressDialog.setMax(length);

				while (i < length) {
					if (m_JSONHandler
							.uploadCorrectWrongStatistics(wrongCorrectStat[i])) {
						m_TriviaDb
								.clearUserCorrectWrongStat(wrongCorrectStat[i]
										.getAsString(TriviaDbEngine.KEY_QUESTIONID));
					} else {
						i = length;
						sb.append("Error occoured stopping upload, check Server URL or Connectivity");
						Log.e(TAG, sb.toString());
					}
					publishProgress(++i);

				}

			}
			return sb.toString();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//
			m_ProgressDialog.setProgress(values[0]);
		}

	}

	private class AsyncTaskCheckUpdateIsAvailable extends
			AsyncTask<Boolean, Integer, Integer> {

		private ProgressDialog m_ProgressDialog;
		private boolean enabled;

		@Override
		protected void onPreExecute() {
			//
			StringBuilder detailedResult = new StringBuilder();

			enabled = m_JSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(TriviaActivity.this);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog.setTitle("Checking for updates...");
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(TriviaActivity.this, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(Integer result) {
			//
			m_ProgressDialog.dismiss();
			if (result > 0) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						TriviaActivity.this);
				dialog.setCancelable(false);
				dialog.setMessage("Update is available for " + result
						+ " Questions.\nUpdate database?");
				dialog.setPositiveButton("Update",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//
								m_JSONHandler
										.updateFromInternetAsync(m_TriviaDb
												.getLastUpdate());

							}
						});
				dialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//
							}
						});

				dialog.show();

			} else {
				Toast.makeText(TriviaActivity.this, "No update available",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Integer doInBackground(Boolean... params) {
			//
			int ret = -1;
			try {
				long lastUpdate = m_TriviaDb.getLastUpdate();
				ret = m_JSONHandler.isUpdateAvailable(lastUpdate);

			} catch (Exception e) {
				String msg = e.getMessage().toString();
				if (msg != null) {
					Log.v(TAG, msg);
				}

			}

			return ret;
		}

	}

}