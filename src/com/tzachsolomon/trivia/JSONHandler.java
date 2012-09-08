package com.tzachsolomon.trivia;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Class handles all request from the Database server using JSON format
 * 
 * @author Tzach Solomon
 * 
 * 
 */
public class JSONHandler {

	public static final String TAG = JSONHandler.class.getSimpleName();
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_ERROR = "error";

	public static final int TYPE_UPDATE_CATEGORIES = 2;
	public static final int TYPE_UPDATE_QUESTIONS = 1;

	private static final String TAG_UPDATE_FROM_DB = "updateFromDb";
	private static final String TAG_REPORT_QUESTION = "reportMistakeInQuestion";
	private static final String TAG_UPDATE_WRONG_CORRECT = "updateWrongCorrectStat";
	private static final String TAG_GET_LAST_UPDATE_QUESTIONS = "getLastUpdateQuestions";
	private static final String TAG_GET_LAST_UPDATE_CATEGORIES = "getLastUpdateCategories";
	private static final String TAG_GET_CATEGORIES = "getCategories";
	private static final String TAG_USER_REGISTER = "tagUserRegister";
	private static final String TAG_USER_LOGIN = "tagUserLogin";
	private static final String TAG_SUGGEST_QUESTION = "tagSuggestQuestion";
	private static final String TAG_DOWNLOAD_GAME_SCORES = "tagDownloadGameScores";
	public static final String TAG_UPLOAD_GAME_SCORE = "tagUploadGameScore";

	private static final int ERROR_CODE_USER_EXIST = 1001;
	//private static final int ERROR_ADDING_USER = 1002;
	private static final int ERROR_CODE_USER_DOES_NOT_EXISTS = 1003;
	private static final int ERROR_CODE_USER_WRONG_PASSWORD = 1004;
	public static final int ERROR_QUESTION_NOT_ADDED = 1005;
	public static final int ERROR_SCORE_WAS_NOT_ADDED = 1006;
	
	private static final int SUCCESS_CODE_USER_REGISTERED = 2001;
	private static final int SUCCESS_CODE_USER_EXIST = 2002;
	public static final int SUCCESS_QUESTION_ADDED = 2003;
	public static final int SUCCESS_SCORE_ADDED = 2004;

	


	// Success
	private String m_ServerUrl;
	private HttpClient m_HttpClient;
	private Context m_Context;
	private ConnectivityManager m_ConnectivityManager;
	private TelephonyManager m_TelephonyManager;
	private SharedPreferences m_SharedPreferences;
	private UserManageListener m_UserManagerListener;
	private DatabaseUpdateListener m_DatabaseUpdateListener;
	private SuggestQuestionListener m_SuggestQuestionListener;
	private ScoreListener m_ScoreUpdateListener;
	private JSONArray jsonArray;

	/**
	 * CTOR
	 * 
	 * @param i_Context
	 *            - Receive context in order to display progress
	 */
	public JSONHandler(Context i_Context) {
		m_HttpClient = new DefaultHttpClient();
		m_Context = i_Context;
		m_ConnectivityManager = (ConnectivityManager) m_Context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		m_TelephonyManager = (TelephonyManager) m_Context
				.getSystemService(Context.TELEPHONY_SERVICE);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(m_Context);

		updateServerIpFromPreferences();
	}

	/**
	 * Function starts an asynchronous worker thread to receive update from the
	 * database server
	 */
	public void updateQuestionFromInternet(long i_LastUserUpdate) {
		ContentValues[] ret = new ContentValues[1];

		ret[0] = new ContentValues();
		ret[0].put("lastUserUpdate", i_LastUserUpdate);

		new AsyncTaskGetQuestionFromServer().execute(ret);

	}

	public void updateCategoriesFromInternet(long i_LastUserUpdate) {
		ContentValues[] ret = new ContentValues[1];

		ret[0] = new ContentValues();
		ret[0].put("lastUserUpdate", i_LastUserUpdate);

		new AsyncTaskGetCategoriesFromServer().execute(ret);

	}

	/**
	 * Function receive a JSON object and parse it to ContentValues object
	 * 
	 * @param i_JsonObject
	 * @return
	 * @throws JSONException
	 */
	private ContentValues convertJSONObjectToQuestionContentValue(
			JSONObject i_JsonObject) throws JSONException {
		ContentValues ret = new ContentValues();

		String[] keys = { TriviaDbEngine.KEY_ANSWER1,
				TriviaDbEngine.KEY_ANSWER2, TriviaDbEngine.KEY_ANSWER3,
				TriviaDbEngine.KEY_ANSWER4, TriviaDbEngine.KEY_ANSWER_INDEX,
				TriviaDbEngine.KEY_CATEGORY,
				TriviaDbEngine.KEY_CORRECT_WRONG_RATIO,
				TriviaDbEngine.KEY_ENABLED, TriviaDbEngine.KEY_LANGUAGE,
				TriviaDbEngine.KEY_LAST_UPDATE, TriviaDbEngine.KEY_QUESTION,
				TriviaDbEngine.KEY_QUESTIONID

		};
		int i = keys.length - 1;

		while (i > -1) {
			ret.put(keys[i], i_JsonObject.getString(keys[i]));
			i--;
		}

		return ret;
	}
	
	private ContentValues convertJSONObjectToGameScoreContentValue(
			JSONObject jsonObject) throws JSONException {
		// 
		
		ContentValues ret = new ContentValues();

		String[] keys = { TriviaDbEngine.KEY_COL_USERNAME,
				TriviaDbEngine.KEY_COL_GAME_SCORE, TriviaDbEngine.KEY_COL_GAME_TYPE,
				TriviaDbEngine.KEY_COL_GAME_TIME

		};
		int i = keys.length - 1;

		while (i > -1) {
			ret.put(keys[i], jsonObject.getString(keys[i]));
			i--;
		}

		return ret;


	}

	/**
	 * Function receive a JSON object and parse it to ContentValues object
	 * 
	 * @param i_JsonObject
	 * @return
	 * @throws JSONException
	 */
	private ContentValues convertJSONObjectToCategoryContentValue(
			JSONObject i_JsonObject) throws JSONException {
		ContentValues ret = new ContentValues();

		String[] keys = { TriviaDbEngine.KEY_ROWID,
				TriviaDbEngine.KEY_COL_PARENT_ID,
				TriviaDbEngine.KEY_COL_EN_NAME, TriviaDbEngine.KEY_COL_HE_NAME,
				TriviaDbEngine.KEY_LAST_UPDATE

		};
		int i = keys.length - 1;

		while (i > -1) {
			ret.put(keys[i], i_JsonObject.getString(keys[i]));
			i--;
		}

		return ret;
	}

	/**
	 * 
	 * @return
	 */
	private JSONArray getQuestionsAsJSONArray(long i_LastUpdate) {
		JSONArray jsonArray = null;
		StringBuilder detailedResult = new StringBuilder();

		try {
			if (isInternetAvailable(detailedResult)) {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("tag", TAG_UPDATE_FROM_DB));
				params.add(new BasicNameValuePair("lastUserUpdate", Long
						.toString(i_LastUpdate)));

				jsonArray = getJSONArrayFromUrl(m_ServerUrl, params);
				if (jsonArray != null) {
					if (!jsonArray.getJSONObject(0).has(RESULT_SUCCESS)) {
						jsonArray = null;
					}
				}

			} else {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e1) {
			Log.e(TAG, e1.getMessage().toString());
		}

		return jsonArray;

	}

	/**
	 * 
	 * @return
	 */
	private JSONArray getCategoriesAsJSONArray(long i_LastUpdate) {
		JSONArray jsonArray = null;
		StringBuilder detailedResult = new StringBuilder();

		try {
			if (isInternetAvailable(detailedResult)) {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("tag", TAG_GET_CATEGORIES));
				params.add(new BasicNameValuePair("lastUserUpdate", Long
						.toString(i_LastUpdate)));

				jsonArray = getJSONArrayFromUrl(m_ServerUrl, params);
				if (jsonArray != null) {
					if (!jsonArray.getJSONObject(0).has(RESULT_SUCCESS)) {
						jsonArray = null;
					}
				}

			} else {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e1) {
			Log.e(TAG, e1.getMessage().toString());
		}

		return jsonArray;

	}

	public void reportMistakeInQuestionAsync(String i_QuestionId,
			String i_Description) throws ClientProtocolException, IOException {

		new AsyncTaskReportMistakeInQuestion().execute(i_QuestionId,
				i_Description);

	}

	/**
	 * Function tries to retrieve the JSON object from i_URL
	 * 
	 * @param i_URL
	 * @param params
	 * @return
	 */
	private JSONObject getJSONObjectFromUrl(String i_URL,
			List<NameValuePair> params) {
		String data;
		HttpEntity httpEntity;
		JSONObject ret = null;
		int status;
		HttpPost httpPost = new HttpPost(i_URL);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse httpResponse = m_HttpClient.execute(httpPost);

			status = httpResponse.getStatusLine().getStatusCode();

			if (status == 200) {
				httpEntity = httpResponse.getEntity();

				data = new String(EntityUtils.toString(httpEntity).getBytes(),
						"UTF-8");

				
				// DEBUG
				//Log.v(TAG, "the raw JSON response is " + data);

				// try parse the string to a JSON object
				try {
					ret = new JSONObject(data);

				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

			} else {
				Log.e(TAG,
						"status code from server is "
								+ Integer.toString(status));
			}

		} catch (UnsupportedEncodingException e) {

			Log.e(TAG, e.getMessage().toString());

		} catch (ClientProtocolException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		} catch (IOException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		}

		return ret;
	}

	/**
	 * Function tries to retrieve the JSON object from i_URL
	 * 
	 * @param i_URL
	 * @param params
	 * @return
	 */
	private JSONArray getJSONArrayFromUrl(String i_URL,
			List<NameValuePair> params) {
		String data;
		HttpEntity httpEntity;
		JSONArray ret = null;
		int status;
		HttpPost httpPost = new HttpPost(i_URL);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse httpResponse = m_HttpClient.execute(httpPost);

			status = httpResponse.getStatusLine().getStatusCode();

			if (status == 200) {
				httpEntity = httpResponse.getEntity();
				httpEntity = httpResponse.getEntity();
				data = new String(EntityUtils.toString(httpEntity).getBytes(),
						"UTF-8");

				Log.v(TAG, "the raw JSON response is " + data);

				// try parse the string to a JSON array
				try {
					ret = new JSONArray(data);

				} catch (JSONException e) {
					Log.e("JSON Parser", "Error parsing data " + e.toString());
				}

			} else {
				Log.e(TAG,
						"status code from server is "
								+ Integer.toString(status));
			}

		} catch (UnsupportedEncodingException e) {

			Log.e(TAG, e.getMessage().toString());

		} catch (ClientProtocolException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		} catch (IOException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		}

		return ret;
	}

	private void reportMistakeInQuestionSync(String i_QuestionId,
			String i_Description) throws ClientProtocolException, IOException {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONObject result;

		params.add(new BasicNameValuePair("tag", TAG_REPORT_QUESTION));
		params.add(new BasicNameValuePair("colQuestionId", i_QuestionId));
		params.add(new BasicNameValuePair("colDescription", i_Description));

		result = getJSONObjectFromUrl(m_ServerUrl, params);

		try {
			if (result.has("error")) {
				Log.e(TAG, result.getString("error"));
			}

		} catch (JSONException e) {
			//
			e.printStackTrace();
		}

	}

	public class AsyncTaskReportMistakeInQuestion extends
			AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... params) {
			//
			try {
				reportMistakeInQuestionSync(params[0], params[1]);
			} catch (ClientProtocolException e) {
				//
				Log.e(TAG, e.getMessage().toString());

			} catch (IOException e) {
				//
				Log.e(TAG, e.getMessage().toString());
			}
			return null;
		}

	}

	public boolean isInternetAvailable(StringBuilder i_DetailedResult) {
		//
		boolean ret = false;
		NetworkInfo info;
		int netType;
		int netSubType;
		boolean allowUpdateWifi;
		boolean allowUpdateMobile;
		boolean allowOnlyUpdate3G;
		boolean allowUpdateRoaming;

		info = m_ConnectivityManager.getActiveNetworkInfo();

		if (info != null) {

			netType = info.getType();
			netSubType = info.getSubtype();

			allowUpdateWifi = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceAllowUpdateWifi", true);
			allowUpdateMobile = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork", true);
			allowOnlyUpdate3G = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork3G", true);
			allowUpdateRoaming = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetworkRoaming", false);

			// checking if the user allow WiFi updates and
			if (netType == ConnectivityManager.TYPE_WIFI) {
				if (allowUpdateWifi) {
					ret = info.isConnected();
					if (!ret) {
						i_DetailedResult
								.append(m_Context
										.getString(R.string.wifi_is_enabled_but_isn_t_connected_please_check_wifi_connection));
					}
				} else {
					i_DetailedResult
							.append("WiFi connection is disabled, please check preferences");
				}
			} else if (netType == ConnectivityManager.TYPE_MOBILE) {
				// Checking if the device found is mobile network device
				if (allowUpdateMobile) {
					// checking if user wants only to allow 3g connection
					if (allowOnlyUpdate3G) {
						if ((netSubType == TelephonyManager.NETWORK_TYPE_UMTS || netSubType == TelephonyManager.NETWORK_TYPE_HSDPA)) {
							ret = info.isConnected();
							if (!ret) {
								i_DetailedResult
										.append(m_Context
												.getString(R.string.mobile_network_found_but_only_3g_mobile_network_connection_is_allowed_));
								i_DetailedResult
										.append(m_Context
												.getString(R.string.check_preferencs_to_allow_slow_networks));
							}
						} else {
							i_DetailedResult
									.append(m_Context
											.getString(R.string.mobile_network_found_but_only_3g_mobile_network_connection_is_allowed_));
							i_DetailedResult
									.append(m_Context
											.getString(R.string.check_preferencs_to_allow_slow_networks));
						}
					} else {
						ret = info.isConnected();
					}

					// checking if connection is roaming
					if (m_TelephonyManager.isNetworkRoaming()) {
						// the connection is roaming, checking if the user allow
						// update on roaming
						if (allowUpdateRoaming) {
							ret = true;
						} else {
							i_DetailedResult
									.append(m_Context
											.getString(R.string.mobile_connection_was_found_but_it_is_in_roaming));
						}

					}

				} else {
					i_DetailedResult
							.append(m_Context
									.getString(R.string.mobile_device_is_online_but_option_is_disabled_please_check_preferences));
				}
			}

		} else {
			i_DetailedResult
					.append(m_Context
							.getString(R.string.no_network_devices_are_available_check_your_wifi_or_mobile_network_connection));

		}

		return ret;
	}

	public class AsyncTaskGetCategoriesFromServer extends
			AsyncTask<ContentValues, Integer, ContentValues[]> {

		private ProgressDialog m_ProgressDialog;
		private boolean isInternetAvailable;
		private StringBuilder detailedResult;

		@Override
		protected void onPreExecute() {

			detailedResult = new StringBuilder();

			m_ProgressDialog = new ProgressDialog(m_Context);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_ProgressDialog.setTitle(m_Context
					.getString(R.string.downloading_categories));
			m_ProgressDialog.show();

			isInternetAvailable = isInternetAvailable(detailedResult);

			if (isInternetAvailable == false) {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_SHORT).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(ContentValues[] result) {
			//
			m_ProgressDialog.dismiss();

			if (m_DatabaseUpdateListener != null) {
				m_DatabaseUpdateListener.onDownloadedCategories(result);
			}

		}

		@Override
		protected ContentValues[] doInBackground(ContentValues... params) {
			//
			if (isInternetAvailable) {
				JSONArray jsonArray;
				JSONObject jsonObject;
				int numberOfRows, i;
				long lastUserUpdate = 0;

				if (params.length > 0) {
					lastUserUpdate = params[0].getAsLong("lastUserUpdate");
				}

				jsonArray = getCategoriesAsJSONArray(lastUserUpdate);
				try {
					if (jsonArray != null) {

						jsonObject = jsonArray.getJSONObject(0);
						numberOfRows = jsonObject.getInt("number_of_rows");

						params = new ContentValues[numberOfRows];
						Log.v(TAG, "Number of rows to parse: " + numberOfRows);
						m_ProgressDialog.setMax(numberOfRows);

						numberOfRows++;

						for (i = 1; i < numberOfRows; i++) {

							jsonObject = jsonArray.getJSONObject(i);
							params[i - 1] = convertJSONObjectToCategoryContentValue(jsonObject);
							publishProgress(i);
						}
					}
				} catch (JSONException e) {
					//

					e.printStackTrace();
				}
			}

			return params;
		}

	}

	/**
	 * @author Tzach Solomon
	 * 
	 */
	public class AsyncTaskGetQuestionFromServer extends
			AsyncTask<ContentValues, Integer, ContentValues[]> {

		private ProgressDialog m_ProgressDialog;
		private boolean isInternetAvailable;
		private StringBuilder detailedResult;

		@Override
		protected void onPreExecute() {

			//

			detailedResult = new StringBuilder();

			m_ProgressDialog = new ProgressDialog(m_Context);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_ProgressDialog.setTitle(m_Context
					.getString(R.string.downloading_questions));
			m_ProgressDialog.show();

			isInternetAvailable = isInternetAvailable(detailedResult);

			if (isInternetAvailable == false) {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_SHORT).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(ContentValues[] result) {
			//
			m_ProgressDialog.dismiss();

			m_DatabaseUpdateListener.onDownloadedQuestions(result);

		}

		@Override
		protected ContentValues[] doInBackground(ContentValues... params) {
			//
			if (isInternetAvailable) {
				JSONArray jsonArray;
				JSONObject jsonObject;
				int numberOfRows, i;
				long lastUserUpdate = 0;

				if (params.length > 0) {
					lastUserUpdate = params[0].getAsLong("lastUserUpdate");
				}

				jsonArray = getQuestionsAsJSONArray(lastUserUpdate);
				try {
					if (jsonArray != null) {

						jsonObject = jsonArray.getJSONObject(0);
						numberOfRows = jsonObject.getInt("number_of_rows");

						params = new ContentValues[numberOfRows];
						// Log.v(TAG, "Number of rows to parse: " +
						// numberOfRows);
						m_ProgressDialog.setMax(numberOfRows);

						numberOfRows++;

						for (i = 1; i < numberOfRows; i++) {

							jsonObject = jsonArray.getJSONObject(i);
							params[i - 1] = convertJSONObjectToQuestionContentValue(jsonObject);
							publishProgress(i);
						}
					}
				} catch (JSONException e) {
					//

					e.printStackTrace();
				}
			}

			return params;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//
			m_ProgressDialog.setProgress(values[0]);
		}
	}

	public boolean uploadCorrectWrongStatistics(ContentValues cv) {
		//
		boolean ret = false;
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("tag", TAG_UPDATE_WRONG_CORRECT));
		params.add(new BasicNameValuePair("colQuestionId", cv
				.getAsString(TriviaDbEngine.KEY_QUESTIONID)));
		params.add(new BasicNameValuePair("colWrongCounter", cv
				.getAsString(TriviaDbEngine.KEY_WRONG_USER)));
		params.add(new BasicNameValuePair("colCorrectCounter", cv
				.getAsString(TriviaDbEngine.KEY_CORRECT_USER)));
		JSONObject response = getJSONObjectFromUrl(m_ServerUrl, params);

		if (response != null) {
			if (response.has(RESULT_SUCCESS)) {
				ret = true;
			}
		}

		return ret;

	}

	public int isUpdateAvailable(long lastUpdate, int i_UpdateType) {
		//
		int ret = -1;
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (i_UpdateType == 1) {

			params.add(new BasicNameValuePair("tag",
					TAG_GET_LAST_UPDATE_QUESTIONS));
		} else if (i_UpdateType == 2) {
			params.add(new BasicNameValuePair("tag",
					TAG_GET_LAST_UPDATE_CATEGORIES));
		}
		params.add(new BasicNameValuePair("lastUserUpdate", Long
				.toString(lastUpdate)));

		JSONObject obj = getJSONObjectFromUrl(m_ServerUrl, params);

		if (obj != null) {

			if (obj.has(RESULT_SUCCESS)) {
				ret = obj.optInt("number");

			}
		}

		return ret;
	}

	public void userLoginAsync(String[] i_Params) {
		AsyncTaskUserLogin asyncTaskUserLogin = new AsyncTaskUserLogin();

		asyncTaskUserLogin.execute(i_Params);
	}

	public String userLogin(String[] i_Params) throws NoSuchAlgorithmException {
		//
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONObject result;
		String ret = "";
		int userId = -1;

		params.add(new BasicNameValuePair("tag", TAG_USER_LOGIN));
		params.add(new BasicNameValuePair("username", i_Params[0]));
		params.add(new BasicNameValuePair("userpass", md5hash(i_Params[1])));
		params.add(new BasicNameValuePair("usermail", i_Params[2]));
		
		// 

		result = getJSONObjectFromUrl(m_ServerUrl, params);

		try {
			if (result != null) {
				// checking if user added successfully
				int successCode = result.getInt(RESULT_SUCCESS);
				int errorCode = result.getInt(RESULT_ERROR);

				if (successCode == SUCCESS_CODE_USER_EXIST) {
					ret = m_Context
							.getString(R.string.user_authenticated_succesfully);
					userId = result.getInt("userId");
				} else if (errorCode == ERROR_CODE_USER_DOES_NOT_EXISTS) {
					ret = m_Context.getString(R.string.user_does_not_exits);
				} else if (errorCode == ERROR_CODE_USER_WRONG_PASSWORD) {
					ret = m_Context.getString(R.string.wrong_password_);
				}
			} else {
				//
				ret = m_Context.getString(R.string.error_connecting_to_server);
			}
		} catch (JSONException e) {
			//
			ret = m_Context.getString(R.string.general_error);
			e.printStackTrace();
		}

		m_UserManagerListener.onUserLogin(ret, userId);
		return ret;
	}

	private String md5hash(String i_Password) throws NoSuchAlgorithmException {
		//

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(i_Password.getBytes(), 0, i_Password.length());
		String hash = new BigInteger(1, md5.digest()).toString(16);

		return hash;
	}

	public void userRegisterAsync(String[] i_Params) {
		AsyncTaskUserRegister asyncTaskUserRegister = new AsyncTaskUserRegister();

		asyncTaskUserRegister.execute(i_Params);
	}

	public String userRegister(String[] i_Params)
			throws NoSuchAlgorithmException {
		//
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONObject result;
		String ret = "";
		int userId = -1;
		
		// TODO: add user type (trivia or facebook)

		params.add(new BasicNameValuePair("tag", TAG_USER_REGISTER));
		params.add(new BasicNameValuePair("username", i_Params[0]));
		params.add(new BasicNameValuePair("userpass", md5hash(i_Params[1])));
		params.add(new BasicNameValuePair("usermail", i_Params[2]));

		result = getJSONObjectFromUrl(m_ServerUrl, params);

		try {
			if (result != null) {
				// checking if user added successfully
				int successCode = result.getInt(RESULT_SUCCESS);
				int errorCode = result.getInt(RESULT_ERROR);

				if (successCode == SUCCESS_CODE_USER_REGISTERED) {
					ret = m_Context
							.getString(R.string.user_registered_succesfully);
					userId = result.getInt("userId");
				} else if (errorCode == ERROR_CODE_USER_EXIST) {
					ret = m_Context.getString(R.string.user_already_exits);
				}
			} else {
				//
				ret = m_Context.getString(R.string.error_connecting_to_server);
			}
		} catch (JSONException e) {
			//
			ret = m_Context.getString(R.string.general_error);
			e.printStackTrace();
		}

		m_UserManagerListener.onUserRegister(ret, userId);
		return ret;

	}

	static public interface UserManageListener {

		public void onUserLogin(String i_Response, int i_UserId);

		public void onUserRegister(String i_Response, int i_UserId);
	}

	static public interface DatabaseUpdateListener {

		public void onDownloadedQuestions(ContentValues[] i_DownloadedQuestions);

		public void onDownloadedCategories(
				ContentValues[] i_DownloadedCategories);

	}

	static public interface ScoreListener {
		public void onScoreAdded(int i_Result);

		public void deleteScoreFromDatabase(int rowInDatabase);

	}

	static public interface SuggestQuestionListener {
		public void onSuggestionSent(int result);
	}

	public void setSuggestQuestionListener(SuggestQuestionListener listener) {
		this.m_SuggestQuestionListener = listener;
	}

	public void setUserManageListener(UserManageListener listener) {
		this.m_UserManagerListener = listener;
	}

	public void setUpdateManager(DatabaseUpdateListener listener) {
		this.m_DatabaseUpdateListener = listener;
		//

	}

	public void setScoreUpdateListener(ScoreListener listener) {
		this.m_ScoreUpdateListener = listener;
	}

	public void updateServerIpFromPreferences() {
		//

		m_ServerUrl = m_SharedPreferences.getString(
				"editTextPreferencePrimaryServerIP",
				"http://23.23.238.181/index.php");
	}

	public class AsyncTaskUserRegister extends
			AsyncTask<String, Integer, String> {

		private ProgressDialog m_ProgressDialog;
		private boolean enabled;

		@Override
		protected void onPreExecute() {
			//
			StringBuilder detailedResult = new StringBuilder();

			enabled = isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(m_Context);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog.setTitle(m_Context
						.getString(R.string.register));
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected String doInBackground(String... params) {
			//
			try {
				return userRegister(params);
			} catch (NoSuchAlgorithmException e) {
				//
				return m_Context
						.getString(R.string.md5_algorithm_was_not_found_);
			}

		}

		@Override
		protected void onPostExecute(String result) {
			//

			m_ProgressDialog.dismiss();
			Toast.makeText(m_Context, result, Toast.LENGTH_LONG).show();
		}

	}

	public class AsyncTaskUserLogin extends AsyncTask<String, Integer, String> {

		private ProgressDialog m_ProgressDialog;
		private boolean enabled;

		@Override
		protected void onPreExecute() {
			//
			StringBuilder detailedResult = new StringBuilder();

			enabled = isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(m_Context);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog.setTitle(m_Context.getString(R.string.login));
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context, detailedResult.toString(),
						Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected String doInBackground(String... params) {
			//
			try {
				return userLogin(params);
			} catch (NoSuchAlgorithmException e) {
				//
				return m_Context
						.getString(R.string.md5_algorithm_was_not_found_);

			}

		}

		@Override
		protected void onPostExecute(String result) {
			//

			m_ProgressDialog.dismiss();
			Toast.makeText(m_Context, result, Toast.LENGTH_SHORT).show();

		}

	}

	public class AsyncTaskSendSuggestion extends
			AsyncTask<String, Integer, Integer> {

		@Override
		protected void onPostExecute(Integer result) {
			//
			super.onPostExecute(result);
			// callback
			if (m_SuggestQuestionListener != null) {
				m_SuggestQuestionListener.onSuggestionSent(result);
			}

		}

		@Override
		protected Integer doInBackground(String... params) {
			//
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			JSONObject result;
			Integer ret = -1;

			params1.add(new BasicNameValuePair("tag", TAG_SUGGEST_QUESTION));
			params1.add(new BasicNameValuePair("userId", params[0]));
			params1.add(new BasicNameValuePair("answerCorrect", params[1]));
			params1.add(new BasicNameValuePair("answerQuestion", params[2]));
			params1.add(new BasicNameValuePair("answerWrong1", params[3]));
			params1.add(new BasicNameValuePair("answerWrong2", params[4]));
			params1.add(new BasicNameValuePair("answerWrong3", params[5]));

			result = getJSONObjectFromUrl(m_ServerUrl, params1);

			try {
				if (result != null) {
					// checking if user added successfully
					int successCode = result.getInt(RESULT_SUCCESS);
					int errorCode = result.getInt(RESULT_ERROR);

					if (successCode == SUCCESS_QUESTION_ADDED) {
						ret = SUCCESS_QUESTION_ADDED;

					} else if (errorCode == ERROR_QUESTION_NOT_ADDED) {
						ret = ERROR_QUESTION_NOT_ADDED;
					}
				} else {
					//

				}
			} catch (JSONException e) {
				//

				e.printStackTrace();
			}

			return ret;
		}

	}

	public void sendQuestionSuggestionAsync(int m_CurrentUserID,
			String answerCorrect, String answerQuestion, String answerWrong1,
			String answerWrong2, String answerWrong3) {
		String[] params = new String[6];

		params[0] = String.valueOf(m_CurrentUserID);
		params[1] = answerCorrect;
		params[2] = answerQuestion;
		params[3] = answerWrong1;
		params[4] = answerWrong2;
		params[5] = answerWrong3;

		AsyncTaskSendSuggestion a = new AsyncTaskSendSuggestion();
		a.execute(params);

	}

	public void uploadScoreToDatabase(String userId, String currentGameType,
			String gameScore, String currentTime, int rowIdInDatabase) {
		//
		String[] params = new String[4];

		params[0] = String.valueOf(userId);
		params[1] = String.valueOf(currentGameType);
		params[2] = String.valueOf(gameScore);
		params[3] = String.valueOf(currentTime);

		AsyncTaskUploadScore a = new AsyncTaskUploadScore();
		a.m_RowInDatabase = rowIdInDatabase;
		a.execute(params);

	}

	public class AsyncTaskUploadScore extends AsyncTask<String, Integer, Integer> {

		public int m_RowInDatabase;

		public AsyncTaskUploadScore() {
			m_RowInDatabase = -1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			//
			super.onPostExecute(result);
			// callback
			if (m_ScoreUpdateListener != null) {
				m_ScoreUpdateListener.onScoreAdded(result);

				// checking if uploaded a score that was in the local db, if so
				// then delete if from the local db
				if (m_RowInDatabase != -1) {
					if (result == SUCCESS_SCORE_ADDED) {
						m_ScoreUpdateListener
								.deleteScoreFromDatabase(m_RowInDatabase);
					}

				}

			}

		}

		@Override
		protected Integer doInBackground(String... params) {
			//
			List<NameValuePair> params1 = new ArrayList<NameValuePair>();
			JSONObject result;
			Integer ret = -1;

			params1.add(new BasicNameValuePair("tag", TAG_UPLOAD_GAME_SCORE));
			params1.add(new BasicNameValuePair("userId", params[0]));
			params1.add(new BasicNameValuePair("gameType", params[1]));
			params1.add(new BasicNameValuePair("gameScore", params[2]));
			params1.add(new BasicNameValuePair("gameTime", params[3]));

			result = getJSONObjectFromUrl(m_ServerUrl, params1);

			try {
				if (result != null) {
					// checking if user added successfully
					int successCode = result.getInt(RESULT_SUCCESS);
					int errorCode = result.getInt(RESULT_ERROR);

					if (successCode == SUCCESS_SCORE_ADDED) {
						ret = SUCCESS_SCORE_ADDED;

					} else if (errorCode == ERROR_SCORE_WAS_NOT_ADDED) {
						ret = ERROR_SCORE_WAS_NOT_ADDED;
					}
				} else {
					// there was an error at the JSON request, save the score locally 
					ret = ERROR_SCORE_WAS_NOT_ADDED;
				}
			} catch (JSONException e) {
				//

				e.printStackTrace();
			}

			return ret;
		}

	}

	public ContentValues[] getGameScores(int gameType) {
		// 

		ContentValues[] ret = null;
		int i, numberOfRows;

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", TAG_DOWNLOAD_GAME_SCORES));
		params.add(new BasicNameValuePair("gameType", Integer.toString(gameType)));
		params.add(new BasicNameValuePair("firstRowIndex", Long.toString(0)));
		params.add(new BasicNameValuePair("lastRowIndex", Long.toString(10)));
		

		try {
			
			jsonArray = getJSONArrayFromUrl(m_ServerUrl, params);

			if (jsonArray != null) {

				JSONObject jsonObject = jsonArray.getJSONObject(0);
				numberOfRows = jsonObject.getInt("number_of_rows");

				ret = new ContentValues[numberOfRows];

				numberOfRows++;

				for (i = 1; i < numberOfRows; i++) {

					jsonObject = jsonArray.getJSONObject(i);
					
					ret[i - 1] = convertJSONObjectToGameScoreContentValue(jsonObject);
					

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;
	}

	
}
