package com.tzachsolomon.trivia;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class UpdateManager {
	
	public static final String TAG = UpdateManager.class.getSimpleName();
	
	private JSONHandler m_JSONHandler;
	private Context m_Context;
	private TriviaDbEngine m_TriviaDb;

	private SharedPreferences m_SharedPreferences;

	
	public UpdateManager (Context i_Context){
		
		
		
		m_Context = i_Context;
		
		m_SharedPreferences = PreferenceManager
		.getDefaultSharedPreferences(m_Context);
		
		m_TriviaDb = new TriviaDbEngine(m_Context);
		m_JSONHandler = new JSONHandler(m_Context);
	}
	
	public void updateNow(){
		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceUploadCorrectWrongUserStat", true)) {
			new AsyncTaskUpdateCorrectWrongAsync().execute();
		} else {
			checkIsUpdateAvailableAsync(true);
		}
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

				m_ProgressDialog = new ProgressDialog(m_Context);
				m_ProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_ProgressDialog
						.setTitle(m_Context.getString(R.string.uploading_correct_wrong_statistics));
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context,
						detailedResult.toString(), Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (enabled) {
				if (result.length() > 0) {
					Toast.makeText(m_Context, result,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							m_Context,
							m_Context.getString(R.string.thank_you_for_making_this_trivia_better_),
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
						sb.append(m_Context.getString(R.string.error_occoured_stopping_upload_check_server_url_or_connectivity));
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

	public class AsyncTaskCheckUpdateIsAvailable extends
			AsyncTask<Boolean, Integer, Integer> {

		private ProgressDialog m_ProgressDialog;
		private boolean enabled;

		@Override
		protected void onPreExecute() {
			//
			StringBuilder detailedResult = new StringBuilder();

			enabled = m_JSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(m_Context);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog
						.setTitle(m_Context.getString(R.string.checking_for_updates_));
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context,
						detailedResult.toString(), Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(Integer result) {
			//
			m_ProgressDialog.dismiss();
			if (result > 0) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						m_Context);
				dialog.setCancelable(false);
				dialog.setMessage(m_Context.getString(R.string.update_is_available_for_)
						+ result
						+ m_Context.getString(R.string._questions_update_database_));
				dialog.setPositiveButton(m_Context.getString(R.string.update),
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
				dialog.setNegativeButton(m_Context.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//

							}
						});

				dialog.show();

			} else {
				Toast.makeText(m_Context,
						m_Context.getString(R.string.no_update_available),
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
