package com.tzachsolomon.trivia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DatabaseMenuActivity extends Activity implements OnClickListener {

	public static final String TAG = DatabaseMenuActivity.class.getSimpleName();
	
	private Button buttonDeleteDatabase;
	private Button buttonUpdateDatabase;

	private TriviaDbEngine m_TriviaDb;
	private JSONHandler m_JSONHandler;

	private SharedPreferences m_SharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database);
		
		m_SharedPreferences = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();
	}

	private void initializeVariables() {
		//
		m_TriviaDb = new TriviaDbEngine(DatabaseMenuActivity.this);

		m_JSONHandler = new JSONHandler(DatabaseMenuActivity.this);
		
		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonUpdateDatabase = (Button) findViewById(R.id.buttonUpdateDatabase);
		buttonDeleteDatabase = (Button) findViewById(R.id.buttonDeleteDatabase);
	

		buttonUpdateDatabase.setOnClickListener(this);
		buttonDeleteDatabase.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonUpdateDatabase:
			buttonUpdateDatabase_Clicked();

			break;

		case R.id.buttonDeleteDatabase:
			buttonDeleteDatabase_Clicked();
			break;

		default:
			break;
		}

	}

	private void buttonUpdateDatabase_Clicked() {
		// checking if to upload user correct wrong statistics before
		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceUploadCorrectWrongUserStat", true)) {
			new AsyncTaskUpdateCorrectWrongAsync().execute();
		} else {
			checkIsUpdateAvailableAsync(true);
		}

	}

	private void buttonDeleteDatabase_Clicked() {
		//
		m_TriviaDb.deleteQuestions();

	}

	private void checkIsUpdateAvailableAsync(boolean i_DisplayInfoIfNoUpdate) {
		new AsyncTaskCheckUpdateIsAvailable().execute(i_DisplayInfoIfNoUpdate);
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

				m_ProgressDialog = new ProgressDialog(DatabaseMenuActivity.this);
				m_ProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_ProgressDialog
						.setTitle(getString(R.string.uploading_correct_wrong_statistics));
				m_ProgressDialog.show();
			} else {
				Toast.makeText(DatabaseMenuActivity.this, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (enabled) {
				if (result.length() > 0) {
					Toast.makeText(DatabaseMenuActivity.this, result,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							DatabaseMenuActivity.this,
							getString(R.string.thank_you_for_making_this_trivia_better_),
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
						sb.append(getString(R.string.error_occoured_stopping_upload_check_server_url_or_connectivity));
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
				m_ProgressDialog = new ProgressDialog(DatabaseMenuActivity.this);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog
						.setTitle(getString(R.string.checking_for_updates_));
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(DatabaseMenuActivity.this, detailedResult.toString(),
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
						DatabaseMenuActivity.this);
				dialog.setCancelable(false);
				dialog.setMessage(getString(R.string.update_is_available_for_)
						+ result
						+ getString(R.string._questions_update_database_));
				dialog.setPositiveButton(getString(R.string.update),
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
				dialog.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//
							}
						});

				dialog.show();

			} else {
				Toast.makeText(DatabaseMenuActivity.this,
						getString(R.string.no_update_available),
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
