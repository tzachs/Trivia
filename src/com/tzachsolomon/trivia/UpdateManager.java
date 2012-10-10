package com.tzachsolomon.trivia;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

	private JSONHandler mJSONHandler;
	private Context mContext;
	private TriviaDbEngine mTriviaDb;

	private SharedPreferences mSharedPreferences;
	private StringParser mStringParser;

	private CategoriesListener mCategoriesListener;

	private QuestionsListener mQuestionsListener;
	

	public UpdateManager(Context i_Context) {

		mContext = i_Context;

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		mStringParser = new StringParser(mSharedPreferences);

		mTriviaDb = new TriviaDbEngine(mContext);
		mTriviaDb.setUpdateListener(this);

		mJSONHandler = new JSONHandler(mContext);
		mJSONHandler.setUpdateManager(this);
		
		

	}

	public void updateQuestions(boolean i_SilentMode) {

		boolean isUpdateEnabled = mSharedPreferences.getBoolean(
				"checkBoxPreferenceCheckUpdateOnStartup", true);

		if (isUpdateEnabled) {

			if (mSharedPreferences.getBoolean(
					"checkBoxPreferenceUploadCorrectWrongUserStat", true)) {
				AsyncTaskUpdateCorrectWrongAsync a = new AsyncTaskUpdateCorrectWrongAsync();
				a.mSilentMode = i_SilentMode;
				a.execute();
			} else {
				checkIsUpdateAvailable(i_SilentMode);
			}
		}
	}

	public void updateCategories(boolean i_SilentMode) {
		//

		AsyncTaskCheckUpdateIsAvailable a = new AsyncTaskCheckUpdateIsAvailable();
		a.mSilentMode = i_SilentMode;
		a.setUpdateType(JSONHandler.TYPE_UPDATE_CATEGORIES);
		a.execute(true);
	}

	private void checkIsUpdateAvailable(boolean i_SilentMode) {
		AsyncTaskCheckUpdateIsAvailable a = new AsyncTaskCheckUpdateIsAvailable();

		a.mSilentMode = i_SilentMode;

		a.setUpdateType(JSONHandler.TYPE_UPDATE_QUESTIONS);
		a.execute(i_SilentMode);

	}

	public class AsyncTaskUpdateCorrectWrongAsync extends
			AsyncTask<Void, Integer, String> {

		public boolean mSilentMode;
		boolean isInternetAvailable;
		ContentValues[] wrongCorrectStat;
		private ProgressDialog mProgressDialog;
		private int mNumberOfQuestionsToSend;

		@Override
		protected void onPreExecute() {
			StringBuilder detailedResult = new StringBuilder();
			//

			isInternetAvailable = mJSONHandler
					.isInternetAvailable(detailedResult);
			if (isInternetAvailable) {

				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog
						.setTitle(mContext
								.getString(R.string.uploading_correct_wrong_statistics));
				if (mSilentMode == false) {
					mProgressDialog.show();
				}
			} else {
				Toast.makeText(mContext, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);
		}

		@Override
		protected void onPostExecute(String result) {
			//
			if (isInternetAvailable) {
				if (result.length() > 0) {

					Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
				} else {

				}

				mProgressDialog.dismiss();
				checkIsUpdateAvailable(true);
			}

			// checking if we sent any questions
			if (mNumberOfQuestionsToSend > 0 && !mSilentMode) {
				if (mQuestionsListener != null) {
					mQuestionsListener.onQuestionsCorrectRatioSent();
				}
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			//
			StringBuilder sb = new StringBuilder();

			if (isInternetAvailable) {
				int i = 0;

				wrongCorrectStat = mTriviaDb.getWrongCorrectStat();

				mNumberOfQuestionsToSend = wrongCorrectStat.length;
				mProgressDialog.setMax(mNumberOfQuestionsToSend);

				if (mNumberOfQuestionsToSend > 0) {

					while (i < mNumberOfQuestionsToSend) {
						if (mJSONHandler
								.uploadCorrectWrongStatistics(wrongCorrectStat[i])) {
							mTriviaDb
									.clearUserCorrectWrongStat(wrongCorrectStat[i]
											.getAsString(TriviaDbEngine.KEY_QUESTIONID));
						} else {
							i = mNumberOfQuestionsToSend;
							sb.append(mContext
									.getString(R.string.error_occoured_stopping_upload_check_server_url_or_connectivity));
							Log.e(TAG, sb.toString());
						}
						publishProgress(++i);

					}

				}

			}
			return sb.toString();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//
			mProgressDialog.setProgress(values[0]);
		}

	}

	public class AsyncTaskCheckUpdateIsAvailable extends
			AsyncTask<Boolean, Integer, Integer> {

		private ProgressDialog mProgressDialog;
		private boolean enabled;

		private int mUpdateType = -1;
		private long mLastUserUpdate;

		public boolean mSilentMode;

		public void setUpdateType(int i_UpdateType) {
			mUpdateType = i_UpdateType;
		}

		@Override
		protected void onPreExecute() {
			//

			StringBuilder detailedResult = new StringBuilder();

			enabled = mJSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setTitle(mContext
						.getString(R.string.checking_for_updates_));
				mProgressDialog.setCancelable(true);
				if (mSilentMode == false) {
					mProgressDialog.show();
				}
			} else {
				Toast.makeText(mContext, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(Integer result) {
			//
			if ( mProgressDialog != null ){
				mProgressDialog.dismiss();	
			}
			
			if (result > 0) {

				StringBuilder message = new StringBuilder();

				message.append(mContext
						.getString(R.string.update_is_available_for_));
				message.append(result);
				message.append(' ');

				if (mUpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

					message.append(mContext.getString(R.string.categories));

				} else if (mUpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {

					message.append(mContext
							.getString(R.string._questions_update_database_));
				}

				AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
				dialog.setCancelable(false);

				dialog.setMessage(mStringParser
						.reverseNumbersInStringHebrew(message.toString()));

				dialog.setPositiveButton(mContext.getString(R.string.update),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								//
								if (mUpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

									mJSONHandler
											.updateCategoriesFromInternet(mLastUserUpdate);

								} else if (mUpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {
									mJSONHandler
											.updateQuestionFromInternet(mLastUserUpdate);
								}

							}
						});
				dialog.setNegativeButton(mContext.getString(R.string.later),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								//
								if (mUpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

									if (mCategoriesListener != null) {
										mCategoriesListener
												.onUpdateCategoriesPostponed();
									}

								} else if (mUpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {

									if (mQuestionsListener != null) {
										mQuestionsListener
												.onUpdateQuestionsPostponed();
									}
								}

							}
						});

				dialog.show();

			} else {
				if (mSilentMode == false) {
					Toast.makeText(mContext,
							mContext.getString(R.string.no_update_available),
							Toast.LENGTH_SHORT).show();
				}

			}
		}

		@Override
		protected Integer doInBackground(Boolean... params) {
			//
			int ret = -1;
			try {
				mLastUserUpdate = 0;
				if (mUpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

					mLastUserUpdate = mTriviaDb.getCategoriesLastUpdate();
				} else if (mUpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {
					mLastUserUpdate = mTriviaDb.getQuestionsLastUpdate();
				}
				ret = mJSONHandler.isUpdateAvailable(mLastUserUpdate,
						mUpdateType);

			} catch (Exception e) {
				// 
				String msg = e.getMessage();
				if (msg != null) {
					Log.e(TAG, msg);
				}

			}

			return ret;
		}

	}

	@Override
	public void onDownloadedQuestions(ContentValues[] i_DownloadedQuestions) {
		//

		if (i_DownloadedQuestions != null) {
			mTriviaDb.updateQuestionAsync(i_DownloadedQuestions,
					TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET, false);

		} else {
			Toast.makeText(
					mContext,
					mContext.getString(R.string.error_while_trying_to_update_from_server),
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onDownloadedCategories(ContentValues[] i_DownloadedCategories) {
		//

		if (i_DownloadedCategories != null) {

			mTriviaDb.updateCategoriesAysnc(i_DownloadedCategories,
					TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET, false);

		} else {
			Toast.makeText(
					mContext,
					mContext.getString(R.string.error_while_trying_to_update_from_server),
					Toast.LENGTH_SHORT).show();
		}

	}

	static public interface CategoriesListener {
		public void onCategoriesUpdated(int i_UpdateFrom);

		public void onUpdateCategoriesPostponed();
	}

	static public interface QuestionsListener {
		public void onQuestionsCorrectRatioSent();

		public void onQuestionsUpdated(int i_UpdateFrom);

		public void updateQuestionProgress(int i_Progress, int i_Max);

		public void onUpdateQuestionsPostponed();
	}

	public void setCategoriesListener(CategoriesListener listener) {
		this.mCategoriesListener = listener;
	}

	public void setQuestionsListener(QuestionsListener listener) {
		this.mQuestionsListener = listener;
	}

	@Override
	public void onUpdateCategoriesFinished(int i_UpdateFrom) {
		//
		// sending event that the categories have been updated
		if (mCategoriesListener != null) {
			mCategoriesListener.onCategoriesUpdated(i_UpdateFrom);
		}

	}

	@Override
	public void onUpdateQuestionsFinished(int i_UpdateFrom) {
		//
		if (mQuestionsListener != null) {
			mQuestionsListener.onQuestionsUpdated(i_UpdateFrom);
		}
	}

	public void updateServerIpFromPreferences() {
		//
		mJSONHandler.updateServerIpFromPreferences();

	}

	@Override
	public void onAddedScoreToDatabase(long returnCode) {
		//

	}

	public void importQuestionsFromXml() {
		//
		AsyncTaskImportQuestionsFromXml a = new AsyncTaskImportQuestionsFromXml(
				mTriviaDb.getQuestionsLastUpdate());
		a.execute();

	}

	public class AsyncTaskImportCategoriesFromXml extends
			AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPostExecute(Void result) {
			//
			mTriviaDb.updateCategoriesAysnc(xmlDataHandler.getCategories(),
					TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE, true);
		}

		private XmlDataHandlerCategories xmlDataHandler;

		@Override
		protected Void doInBackground(Void... params) {
			//
			InputStream raw = mContext.getResources().openRawResource(
					R.raw.categories);

			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = null;
			try {
				saxParser = saxParserFactory.newSAXParser();
			} catch (ParserConfigurationException e1) {
				//
				e1.printStackTrace();
			} catch (SAXException e1) {
				//
				e1.printStackTrace();
			}

			Reader reader = new InputStreamReader(raw);
			InputSource inputSource = new InputSource(reader);
			inputSource.setEncoding("UTF-8");
			xmlDataHandler = new XmlDataHandlerCategories();

			try {
				try {
					saxParser.parse(inputSource, xmlDataHandler);
				} catch (SAXException e) {
					//

					e.printStackTrace();
				}
			} catch (IOException e) {
				// s
				e.printStackTrace();
			}

			return null;
		}

	}

	public void importCategoriesFromXml() {
		new AsyncTaskImportCategoriesFromXml().execute();
	}

	public class AsyncTaskImportQuestionsFromXml extends
			AsyncTask<Void, Integer, Void> {

		private XmlDataHandlerQuestions xmlDataHandler;
		private long m_LastUpdate;

		public AsyncTaskImportQuestionsFromXml(long i_LastUpdate) {
			m_LastUpdate = i_LastUpdate;
		}

		@Override
		protected void onPostExecute(Void result) {
			//
			mTriviaDb.updateQuestionAsync(xmlDataHandler.getQuestions(),
					TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE, true);

		}

		@Override
		protected void onPreExecute() {
			//

		}

		@Override
		protected Void doInBackground(Void... params) {
			//
			InputStream raw = mContext.getResources().openRawResource(
					R.raw.questions);

			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = null;
			try {
				saxParser = saxParserFactory.newSAXParser();
			} catch (ParserConfigurationException e1) {
				//
				e1.printStackTrace();
			} catch (SAXException e1) {
				//
				e1.printStackTrace();
			}

			Reader reader;
			try {
				reader = new InputStreamReader(raw, "UTF-8");
				InputSource inputSource = new InputSource(reader);
				inputSource.setEncoding("UTF-8");
				xmlDataHandler = new XmlDataHandlerQuestions(m_LastUpdate);
				try {
					saxParser.parse(inputSource, xmlDataHandler);
				} catch (SAXException e) {
					//
					Log.e(TAG,
							"Error at AsyncTaskImportQuestionsFromXml->doInBackground");

				} catch (IOException e) {

				}
			} catch (UnsupportedEncodingException e1) {
				//
				Log.e(TAG, "unsupported encoding of questions");
				e1.printStackTrace();
			}
			return null;
		}

	}

	@Override
	public void updateProgressQuestionsInsertToDatabase(int i_Progress,
			int i_Max) {
		//
		if (mQuestionsListener != null) {
			mQuestionsListener.updateQuestionProgress(i_Progress, i_Max);
		}

	}

}
