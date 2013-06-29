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


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

	private CategoriesListener mCategoriesListener;

	private QuestionsListener mQuestionsListener;

	private boolean mSilentMode;

	private int mUpdateType;

	private long mLastUserQuestionsUpdate;
	private long mLastUserCategoriesUpdate;

    public UpdateManager(Context i_Context) {

		mContext = i_Context;

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		mTriviaDb = new TriviaDbEngine(mContext);
		mTriviaDb.setUpdateListener(this);

		mJSONHandler = new JSONHandler(mContext);
		mJSONHandler.setUpdateManager(this);

	}

	public void updateQuestions(boolean silentMode) {

		boolean isUpdateEnabled = mSharedPreferences.getBoolean(
				"checkBoxPreferenceCheckUpdateOnStartup", true);

		mSilentMode = silentMode;

		if (isUpdateEnabled) {

			if (mSharedPreferences.getBoolean(
					"checkBoxPreferenceUploadCorrectWrongUserStat", true)) {
				AsyncTaskUpdateCorrectWrongAsync a = new AsyncTaskUpdateCorrectWrongAsync();

				a.execute();
			} else {
				checkIsUpdateAvailable(mSilentMode);
			}
		} else if (mSilentMode == false) {
			Toast.makeText(mContext, "Update is disabled, check preferences",
					Toast.LENGTH_LONG).show();
		}
	}

	public void updateCategories(boolean silentMode) {
		//

		AsyncTaskCheckUpdateIsAvailable a = new AsyncTaskCheckUpdateIsAvailable();
		
		mSilentMode = silentMode;

		mUpdateType = JSONHandler.TYPE_UPDATE_CATEGORIES;
		a.execute();
	}

	private void checkIsUpdateAvailable(boolean silentMode) {
		AsyncTaskCheckUpdateIsAvailable a = new AsyncTaskCheckUpdateIsAvailable();

		mSilentMode = silentMode;
		mUpdateType = JSONHandler.TYPE_UPDATE_QUESTIONS;
		a.execute();

	}

    public void setUpdateType(int updateType) {
        this.mUpdateType = updateType;
    }

    public class AsyncTaskUpdateCorrectWrongAsync extends
			AsyncTask<Void, Integer, String> {

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
				checkIsUpdateAvailable(mSilentMode);
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
			AsyncTask<Boolean, Integer, Bundle> {

		private ProgressDialog mProgressDialog;
		private boolean enabled;

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
		protected void onPostExecute(Bundle result) {
			//
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			
			switch (mUpdateType){
			case JSONHandler.TYPE_UPDATE_CATEGORIES:
				if (mCategoriesListener != null){
									
					mCategoriesListener.onCheckIfCategoriesUpdateAvailablePost(result);
					
				}
				break;
			case JSONHandler.TYPE_UPDATE_QUESTIONS:
				if (mQuestionsListener != null) {
					
					mQuestionsListener.onCheckIfQuestionUpdateAvailablePost(result);
				}
				break;
			}

			

		}

		@Override
		protected Bundle doInBackground(Boolean... params) {
			//
			Bundle ret = null;
			try {
				
				if (mUpdateType == JSONHandler.TYPE_UPDATE_CATEGORIES) {

					mLastUserCategoriesUpdate = 0;
					mLastUserCategoriesUpdate = mTriviaDb.getCategoriesLastUpdate();
					ret = mJSONHandler.isUpdateAvailable(mLastUserCategoriesUpdate,
							mUpdateType);
				} else if (mUpdateType == JSONHandler.TYPE_UPDATE_QUESTIONS) {
					mLastUserQuestionsUpdate = 0;
					mLastUserQuestionsUpdate = mTriviaDb.getQuestionsLastUpdate();
					ret = mJSONHandler.isUpdateAvailable(mLastUserQuestionsUpdate,
							mUpdateType);
				}
				

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

		public void onCheckIfCategoriesUpdateAvailablePost(Bundle result);
		public void onUpdateCategoriesPostponed();
	}

	static public interface QuestionsListener {
		public void onQuestionsCorrectRatioSent();

		public void onCheckIfQuestionUpdateAvailablePost(Bundle result);

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

	public boolean getSilentMode() {
		//
		return mSilentMode;

	}

	public int getUpdateType() {
		//
		return mUpdateType;

	}

	public void updateQuestionsNow() {
		//
		mJSONHandler.updateQuestionFromInternet(mLastUserQuestionsUpdate);
	}

	public void updateCategoriesNow() {
		//
		mJSONHandler.updateCategoriesFromInternet(mLastUserCategoriesUpdate);
		
	}

}
