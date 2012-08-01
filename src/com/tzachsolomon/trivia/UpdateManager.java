package com.tzachsolomon.trivia;

import com.tzachsolomon.trivia.JSONHandler.DatabaseUpdateListener;
import com.tzachsolomon.trivia.TriviaDbEngine.TriviaDbEngineUpdateListener;

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

public class UpdateManager implements DatabaseUpdateListener,
		TriviaDbEngineUpdateListener {

	public static final String TAG = UpdateManager.class.getSimpleName();

	private JSONHandler m_JSONHandler;
	private Context m_Context;
	private TriviaDbEngine m_TriviaDb;

	private SharedPreferences m_SharedPreferences;
	private StringParser m_StringParser;

	private CategoriesListener m_CategoriesListener;

	private QuestionsListener m_QuestionsListener;

	public UpdateManager(Context i_Context) {

		m_Context = i_Context;

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(m_Context);

		m_StringParser = new StringParser(m_SharedPreferences);

		m_TriviaDb = new TriviaDbEngine(m_Context);
		m_TriviaDb.setUpdateListener(this);

		m_JSONHandler = new JSONHandler(m_Context);
		m_JSONHandler.setUpdateManager(this);

	}

	public void updateQuestions() {

		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceUploadCorrectWrongUserStat", true)) {
			new AsyncTaskUpdateCorrectWrongAsync().execute();
		} else {
			checkIsUpdateAvailable(true);
		}
	}

	public void updateCategories() {
		//

		AsyncTaskCheckUpdateIsAvailable a = new AsyncTaskCheckUpdateIsAvailable();
		a.setUpdateType(JSONHandler.TYPE_UPDATE_CATEGORIES);
		a.execute(true);
	}

	private void checkIsUpdateAvailable(boolean i_DisplayInfoIfNoUpdate) {
		AsyncTaskCheckUpdateIsAvailable a = new AsyncTaskCheckUpdateIsAvailable();

		a.setUpdateType(JSONHandler.TYPE_UPDATE_QUESTIONS);
		a.execute(i_DisplayInfoIfNoUpdate);

	}

	public class AsyncTaskUpdateCorrectWrongAsync extends
			AsyncTask<Void, Integer, String> {

		boolean isInternetAvailable;
		ContentValues[] wrongCorrectStat;
		private ProgressDialog m_ProgressDialog;

		@Override
		protected void onPreExecute() {
			StringBuilder detailedResult = new StringBuilder();
			//

			isInternetAvailable = m_JSONHandler
					.isInternetAvailable(detailedResult);
			if (isInternetAvailable) {

				m_ProgressDialog = new ProgressDialog(m_Context);
				m_ProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				m_ProgressDialog
						.setTitle(m_Context
								.getString(R.string.uploading_correct_wrong_statistics));
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (isInternetAvailable) {
				if (result.length() > 0) {
					Toast.makeText(m_Context, result, Toast.LENGTH_LONG).show();
				} else {
					/*
					 * Toast.makeText( m_Context, m_Context
					 * .getString(R.string.thank_you_for_making_this_trivia_better_
					 * ),
					 * 
					 * Toast.LENGTH_SHORT).show();
					 */
				}

				m_ProgressDialog.dismiss();
				checkIsUpdateAvailable(true);
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			//
			StringBuilder sb = new StringBuilder();

			if (isInternetAvailable) {
				int i = 0;
				int length;
				wrongCorrectStat = m_TriviaDb.getWrongCorrectStat();

				length = wrongCorrectStat.length;
				m_ProgressDialog.setMax(length);

				if (length > 0) {

					while (i < length) {
						if (m_JSONHandler
								.uploadCorrectWrongStatistics(wrongCorrectStat[i])) {
							m_TriviaDb
									.clearUserCorrectWrongStat(wrongCorrectStat[i]
											.getAsString(TriviaDbEngine.KEY_QUESTIONID));
						} else {
							i = length;
							sb.append(m_Context
									.getString(R.string.error_occoured_stopping_upload_check_server_url_or_connectivity));
							Log.e(TAG, sb.toString());
						}
						publishProgress(++i);

					}

					if (m_QuestionsListener != null) {
						m_QuestionsListener.onQuestionsCorrectRatioSent();
					}

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

		private int m_UpdateType = -1;
		private long m_LastUserUpdate;

		public void setUpdateType(int i_UpdateType) {
			m_UpdateType = i_UpdateType;
		}

		@Override
		protected void onPreExecute() {
			//

			StringBuilder detailedResult = new StringBuilder();

			enabled = m_JSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(m_Context);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog.setTitle(m_Context
						.getString(R.string.checking_for_updates_));
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(Integer result) {
			//
			m_ProgressDialog.dismiss();
			if (result > 0) {

				StringBuilder message = new StringBuilder();

				message.append(m_Context
						.getString(R.string.update_is_available_for_));
				message.append(result);
				message.append(' ');

				if (m_UpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

					message.append(m_Context.getString(R.string.categories));

				} else if (m_UpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {

					message.append(m_Context
							.getString(R.string._questions_update_database_));
				}

				AlertDialog.Builder dialog = new AlertDialog.Builder(m_Context);
				dialog.setCancelable(false);

				dialog.setMessage(m_StringParser
						.reverseNumbersInStringHebrew(message.toString()));

				dialog.setPositiveButton(m_Context.getString(R.string.update),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								//
								if (m_UpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

									m_JSONHandler
											.updateCategoriesFromInternet(m_LastUserUpdate);

								} else if (m_UpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {
									m_JSONHandler
											.updateQuestionFromInternet(m_LastUserUpdate);
								}

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
				m_LastUserUpdate = 0;
				if (m_UpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

					m_LastUserUpdate = m_TriviaDb.getCategoriesLastUpdate();
				} else if (m_UpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {
					m_LastUserUpdate = m_TriviaDb.getQuestionsLastUpdate();
				}
				ret = m_JSONHandler.isUpdateAvailable(m_LastUserUpdate,
						m_UpdateType);

			} catch (Exception e) {
				String msg = e.getMessage().toString();
				if (msg != null) {
					Log.v(TAG, msg);
				}

			}

			return ret;
		}

	}

	@Override
	public void onDownloadedQuestions(ContentValues[] i_DownloadedQuestions) {
		//

		if (i_DownloadedQuestions != null) {
			m_TriviaDb.updateQuestionFromInternetAsync(i_DownloadedQuestions);

		} else {
			Toast.makeText(
					m_Context,
					m_Context
							.getString(R.string.error_while_trying_to_update_from_server),
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onDownloadedCategories(ContentValues[] i_DownloadedCategories) {
		//

		if (i_DownloadedCategories != null) {

			m_TriviaDb.updateCategoriesAysnc(i_DownloadedCategories);

		} else {
			Toast.makeText(
					m_Context,
					m_Context
							.getString(R.string.error_while_trying_to_update_from_server),
					Toast.LENGTH_SHORT).show();
		}

	}

	static public interface CategoriesListener {
		public void onCategoriesUpdated();
	}

	static public interface QuestionsListener {
		public void onQuestionsCorrectRatioSent();

		public void onQuestionsUpdated();
	}

	public void setCategoriesListener(CategoriesListener listener) {
		this.m_CategoriesListener = listener;
	}

	public void setQuestionsListener(QuestionsListener listener) {
		this.m_QuestionsListener = listener;
	}

	@Override
	public void onUpdateCategoriesFinished() {
		//

		// sending event that the categories have been updated
		if (m_CategoriesListener != null) {
			m_CategoriesListener.onCategoriesUpdated();
		}

	}

	@Override
	public void onUpdateQuestionsFinished() {
		// TODO Auto-generated method stub

	}

	public void updateServerIpFromPreferences() {
		// 
		m_JSONHandler.updateServerIpFromPreferences();
		
	}

	@Override
	public void onAddedScoreToDatabase(long returnCode) {
		// TODO Auto-generated method stub
		
	}


}
