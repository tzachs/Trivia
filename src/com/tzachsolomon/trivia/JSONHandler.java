package com.tzachsolomon.trivia;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

	private static final String SUCCESS_CODE = "success";
	private static final String ERROR_CODE = "error";
	private static final int SUCCUESS_CODE_USER_REGISTERED = 2001;
	private static final int SUCCUESS_CODE_USER_EXIST = 2002;
	private static final int ERROR_CODE_USER_DOES_NOT_EXISTS = 1003;
	private static final int ERROR_CODE_USER_EXIST = 1001;
	private static final int ERROR_CODE_USER_WRONG_PASSWORD = 1004;

	private String m_ServerUrl;
	private HttpClient m_HttpClient;
	private Context m_Context;
	private ConnectivityManager m_ConnectivityManager;
	private TelephonyManager m_TelephonyManager;
	private SharedPreferences m_SharedPreferences;
	private UserManageListener m_UserManagerListener;
	private DatabaseUpdateListener m_DatabaseUpdateListener;

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
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse httpResponse = m_HttpClient.execute(httpPost);

			status = httpResponse.getStatusLine().getStatusCode();

			if (status == 200) {
				httpEntity = httpResponse.getEntity();

				data = new String(EntityUtils.toString(httpEntity).getBytes(),
						"UTF-8");

				// Log.v(TAG, "the raw JSON response is " + data);

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
						Log.v(TAG, "Number of rows to parse: " + numberOfRows);
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
	
	public void userLoginAsync(String[]i_Params){
		AsyncTaskUserLogin asyncTaskUserLogin = new AsyncTaskUserLogin();

		asyncTaskUserLogin.execute(i_Params);
	}

	public String userLogin(String[] i_Params) {
		//
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONObject result;
		String ret = "";
		int userId = -1;

		params.add(new BasicNameValuePair("tag", TAG_USER_LOGIN));
		params.add(new BasicNameValuePair("username", i_Params[0]));
		params.add(new BasicNameValuePair("userpass", i_Params[1]));
		params.add(new BasicNameValuePair("usermail", i_Params[2]));

		result = getJSONObjectFromUrl(m_ServerUrl, params);

		try {
			if (result != null) {
				// checking if user added successfully
				int successCode = result.getInt(SUCCESS_CODE);
				int errorCode = result.getInt(ERROR_CODE);

				if (successCode == SUCCUESS_CODE_USER_EXIST) {
					ret = "User authenticated succesfully";
					userId = result.getInt("userId");
				} else if (errorCode == ERROR_CODE_USER_DOES_NOT_EXISTS) {
					ret = "User does not exits";
				} else if (errorCode == ERROR_CODE_USER_WRONG_PASSWORD) {
					ret = "Wrong password!";
				}
			} else {
				//
				ret = "Error connecting to server";
			}
		} catch (JSONException e) {
			//
			ret = "General error";
			e.printStackTrace();
		}

		m_UserManagerListener.onUserLogin(ret, userId);
		return ret;
	}
	
	public void userRegisterAsync(String[] i_Params){
		AsyncTaskUserRegister asyncTaskUserRegister = new AsyncTaskUserRegister();

		asyncTaskUserRegister.execute(i_Params);
	}

	public String userRegister(String[] i_Params) {
		//
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONObject result;
		String ret = "";
		int userId = -1;

		params.add(new BasicNameValuePair("tag", TAG_USER_REGISTER));
		params.add(new BasicNameValuePair("username", i_Params[0]));
		params.add(new BasicNameValuePair("userpass", i_Params[1]));
		params.add(new BasicNameValuePair("usermail", i_Params[2]));

		result = getJSONObjectFromUrl(m_ServerUrl, params);

		try {
			if (result != null) {
				// checking if user added successfully
				int successCode = result.getInt(SUCCESS_CODE);
				int errorCode = result.getInt(ERROR_CODE);

				if (successCode == SUCCUESS_CODE_USER_REGISTERED) {
					ret = "User registered succesfully";
					userId = result.getInt("userId");
				} else if (errorCode == ERROR_CODE_USER_EXIST) {
					ret = "User already exits";
				}
			} else {
				//
				ret = "Error connecting to server";
			}
		} catch (JSONException e) {
			//
			ret = "General error";
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

	public void setUserManageListener(UserManageListener listener) {
		this.m_UserManagerListener = listener;
	}

	public void setUpdateManager(DatabaseUpdateListener listener) {
		this.m_DatabaseUpdateListener = listener;
		//

	}

	public void updateServerIpFromPreferences() {
		//

		m_ServerUrl = m_SharedPreferences.getString(
				"editTextPreferencePrimaryServerIP",
				"http://23.23.238.181/index.php");
	}
	
	public class AsyncTaskUserRegister extends AsyncTask<String, Integer, String> {

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
				m_ProgressDialog.setTitle("Register");
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context,
						detailedResult.toString(), Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected String doInBackground(String... params) {
			//
			return userRegister(params);
			

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
				m_ProgressDialog.setTitle("Login");
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(m_Context,
						detailedResult.toString(), Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected String doInBackground(String... params) {
			//
			return userLogin(params);
			

		}

		@Override
		protected void onPostExecute(String result) {
			//

			m_ProgressDialog.dismiss();
			Toast.makeText(m_Context, result, Toast.LENGTH_SHORT).show();
			
		}

	}


}
