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
	// TODO: create levels
	// TODO: Delete database
	// TODO: play categories
	// TODO: play till you die
	// TODO: play in increasing levels
	// TODO: create service to update the database daily

	public static final String TAG = TriviaActivity.class.getSimpleName();

	private Button buttonNewGame;

	private TriviaDbEngine m_TriviaDb;
	private JSONHandler m_JSONHandler;
	private Button buttonUpdateDatabase;

	private SharedPreferences m_SharedPreferences;

	private Button buttonPreferences;

	private Button buttonNewGameLevels;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		PreferenceManager.setDefaultValues(this, R.xml.prefs, true);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();
		
		//m_TriviaDb.deleteQuestions();

		if (!checkIfFirstTime()) {
			checkIsUpdateAvailable(false);
		}

	}

	/**
	 * Function receives the max between question id and last update columns.
	 * The max value is the last update that was made from the database
	 */
	private void checkIsUpdateAvailable(boolean i_DisplayInfoIfNoUpdate) {
		//
		try {
			long lastUpdate = m_TriviaDb.getLastUpdate();
			int numberOfQuestionsToUpdate = m_JSONHandler
					.isUpdateAvailable(lastUpdate);

			if (numberOfQuestionsToUpdate > 0) {
				updateDatabaseFromInternetDisplayQuestion("Update is available for "
						+ Integer.toString(numberOfQuestionsToUpdate)
						+ " questions.\nUpdate database?");

			}else if ( i_DisplayInfoIfNoUpdate){
				Toast.makeText(TriviaActivity.this, "No updates", Toast.LENGTH_LONG).show();
			}
				
		} catch (Exception e) {
			String msg = e.getMessage().toString();
			if (msg != null) {
				Log.v(TAG, msg);
			}

		}

	}

	private boolean checkIfFirstTime() {
		//
		boolean ret = m_TriviaDb.isEmpty();
		if (ret) {
			updateDatabaseFromInternetDisplayQuestion("It seems this is the first time you are running Trivia.\n"
					+ "You must once update the datatable.\n"
					+ "Update Now? (This requires internet connection)");
		}

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

		StringBuilder detailedResult = new StringBuilder();
		long lastUpdate = m_TriviaDb.getLastUpdate();
		int isUpdateAvailable = m_JSONHandler.isUpdateAvailable(lastUpdate);

		if (isUpdateAvailable > 0) {

			if (m_JSONHandler.isInternetAvailable(detailedResult)) {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(i_Message);
				builder.setCancelable(false);
				builder.setPositiveButton("Update",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//

								updateDatabaseFromInternet();

							}
						});
				builder.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//

							}
						});

				builder.show();
			} else {
				if (detailedResult.length() > 0) {
					Toast.makeText(TriviaActivity.this,
							detailedResult.toString(), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(TriviaActivity.this,
							"Check server URL in preferences",
							Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			Toast.makeText(TriviaActivity.this, "No update is available",
					Toast.LENGTH_SHORT).show();
		}

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
		buttonNewGameLevels = (Button)findViewById(R.id.buttonNewGameLevels);

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

	private void buttonNewGameLevels_Clicked() {
		// 
		startNewGame(Game.KEY_GAMETYPE_LEVELS);
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
		startNewGame(Game.KEY_GAMETYPE_ALL_QUESTIONS);
		

	}

	private void startNewGame(int i_GameType) {
		// 
		
		Intent intent = new Intent(this, Game.class);
		intent.putExtra("GameType",i_GameType);
		
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
					"checkBoxPreferenceUploadCorrectWrongUserStat", true)
					&& m_JSONHandler.isInternetAvailable(detailedResult);

			if (enabled) {
				m_ProgressDialog = new ProgressDialog(TriviaActivity.this);
				m_ProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_ProgressDialog.setTitle("Uploading correct wrong statistics");
				m_ProgressDialog.show();
			} // ignoring checking if isInternetAvailable returned false
				// since it updateFromDatabase will show that error.
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (result.length() > 0) {
				Toast.makeText(TriviaActivity.this, result, Toast.LENGTH_LONG)
						.show();
			}

			if (enabled) {
				m_ProgressDialog.dismiss();
			}
			
			checkIsUpdateAvailable(true);
			//updateDatabaseFromInternetDisplayQuestion("Update Now? (This requires internet connection)");

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

}