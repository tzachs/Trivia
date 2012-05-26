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

	private Button buttonWizardSetup;

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

	private boolean checkIfFirstTime() {
		//

		boolean ret = m_TriviaDb.isEmpty();
		/*
		 * if (ret) { updateDatabaseFromInternetDisplayQuestion(
		 * "It seems this is the first time you are running Trivia.\n" +
		 * "You must once update the datatable.\n" +
		 * "Update Now? (This requires internet connection)"); }
		 */

		return ret;

	}

	private void updateDatabaseFromInternet() {
		ContentValues[] values;
		String updateMethod;
		String serverUrl = m_SharedPreferences.getString(
				"editTextPreferencePrimaryServerIP", "");

		if (serverUrl.contentEquals("")) {
			Toast.makeText(this,
					"Error finding server URL, please check prefereneces",
					Toast.LENGTH_SHORT).show();

		} else {

			// getting the desired update method
			updateMethod = m_SharedPreferences.getString(
					"listPreferenceUpdateMethod", "async");

			if (updateMethod.contentEquals("sync")) {
				values = m_JSONHandler.updateFromInternetSync(m_TriviaDb
						.getLastUpdate());
				if (values == null) {
					Toast.makeText(this, "Error updating from server",
							Toast.LENGTH_SHORT).show();
				} else {

					m_TriviaDb.updateFromInternetSync(values);
				}

			} else {
				m_JSONHandler.updateFromInternetAsync(m_TriviaDb
						.getLastUpdate());

			}
		}

	}

	private void updateDatabaseFromInternetDisplayQuestion(String i_Message) {

	}

	private void initializeVariables() {
		//
		m_TriviaDb = new TriviaDbEngine(this);

		m_JSONHandler = new JSONHandler(TriviaActivity.this);

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonNewGame = (Button) findViewById(R.id.buttonNewGame);
		buttonUpdateDatabase = (Button) findViewById(R.id.buttonUpdateDatabase);
		buttonPreferences = (Button) findViewById(R.id.buttonPreferences);
		buttonNewGameLevels = (Button) findViewById(R.id.buttonNewGameLevels);
		buttonWizardSetup = (Button) findViewById(R.id.buttonWizardSetup);

		buttonNewGame.setOnClickListener(this);
		buttonUpdateDatabase.setOnClickListener(this);
		buttonPreferences.setOnClickListener(this);
		buttonNewGameLevels.setOnClickListener(this);
		buttonWizardSetup.setOnClickListener(this);

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
		case R.id.buttonWizardSetup:
			buttonWizardSetup_Clicked();
			break;
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
		uploadCorrectWrong();

	}

	private void uploadCorrectWrong() {
		//
		new UpdateCorrectWrongAsync().execute();

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

	public class UpdateCorrectWrongAsync extends
			AsyncTask<Void, Integer, String> {

		boolean enabled;
		ContentValues[] wrongCorrectStat;
		private ProgressDialog m_ProgressDialog;

		@Override
		protected void onPreExecute() {
			StringBuilder detailedResult = new StringBuilder();
			//
			enabled = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceUploadCorrectWrongUserStat", true);

			if (enabled) {
				enabled = m_JSONHandler.isInternetAvailable(detailedResult);
				if (enabled) {

					m_ProgressDialog = new ProgressDialog(TriviaActivity.this);
					m_ProgressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					m_ProgressDialog
							.setTitle("Uploading correct wrong statistics");
					m_ProgressDialog.show();
				} else {
					Toast.makeText(TriviaActivity.this,
							detailedResult.toString(), Toast.LENGTH_LONG)
							.show();
				}
			}

			detailedResult.setLength(0);
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (result.length() > 0) {
				Toast.makeText(TriviaActivity.this, result, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(TriviaActivity.this,
						"Thank you for making this trivia better! :)",
						Toast.LENGTH_SHORT).show();
			}

			if (enabled) {
				m_ProgressDialog.dismiss();
			}

			checkIsUpdateAvailableAsync(true);

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
			AsyncTask<Boolean, Integer, Boolean> {

		private ProgressDialog m_ProgressDialog;

		@Override
		protected void onPreExecute() {
			//
			m_ProgressDialog = new ProgressDialog(TriviaActivity.this);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			m_ProgressDialog.setTitle("Checking for updates...");
			m_ProgressDialog.setCancelable(true);
			m_ProgressDialog.show();

		}

		@Override
		protected void onPostExecute(Boolean result) {
			//
			m_ProgressDialog.dismiss();
			if (result) {
				try {
					new AsyncTaskUpdateFromDatabase()
							.execute("Update is available\nUpdate database?");

				} catch (Exception e) {
					Log.e(TAG, "error at onPostExecute");

				}
			} else {
				Toast.makeText(TriviaActivity.this, "No update available",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			//
			boolean ret = false;
			try {
				long lastUpdate = m_TriviaDb.getLastUpdate();
				int numberOfQuestionsToUpdate = m_JSONHandler
						.isUpdateAvailable(lastUpdate);

				if (numberOfQuestionsToUpdate > 0) {
					ret = true;
				}

			} catch (Exception e) {
				String msg = e.getMessage().toString();
				if (msg != null) {
					Log.v(TAG, msg);
				}

			}

			return ret;
		}

	}

	public class AsyncTaskUpdateFromDatabase extends
			AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//
			super.onProgressUpdate(values);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			//
			m_JSONHandler.updateFromInternetAsync(m_TriviaDb
					.getLastUpdate());
			return null;
		}

	}

}