package com.tzachsolomon.trivia;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
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

public class JSONHandler {

	private static final String TAG = JSONHandler.class.getSimpleName();

	private HttpClient m_HttpClient;
	private String m_ServerUrl;
	private Context m_ActivityContext;
	private ConnectivityManager m_ConnectivityManager;
	private TelephonyManager m_TelephonyManager;

	private SharedPreferences m_SharedPreferences;

	public JSONHandler(Context i_Context) {
		m_HttpClient = new DefaultHttpClient();
		m_ActivityContext = i_Context;
		m_ConnectivityManager = (ConnectivityManager) m_ActivityContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		m_TelephonyManager = (TelephonyManager) m_ActivityContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(m_ActivityContext);
	}

	public void setServerUrl(String i_ServerUrl) {
		this.m_ServerUrl = i_ServerUrl;
	}

	public String getServerUrl() {
		return m_ServerUrl;

	}

	public void updateFromInternetAsync() {
		ContentValues[] ret = null;

		new GetQuestionFromServerAsyncTask().execute(ret);

	}

	public ContentValues[] updateFromInternetSync() {
		ContentValues[] ret = null;

		try {
			ret = handleRead();
		} catch (ClientProtocolException e) {

			Log.e(TAG, e.getMessage().toString());

		} catch (IOException e) {

			Log.e(TAG, e.getMessage().toString());

		} catch (JSONException e) {
			//
			Log.e(TAG, e.getMessage().toString());

		}
		return ret;
	}

	private ContentValues[] handleRead() throws ClientProtocolException,
			IOException, JSONException {
		ContentValues[] ret = null;

		JSONArray jsonArray;
		JSONObject jsonObject;
		int numberOfRows, i;

		try {
			jsonArray = getQuestionAsJSONArray();
			// checking if the wasn't any errors while getting the JSON array
			if (jsonArray != null) {
				// getting the first object
				jsonObject = jsonArray.getJSONObject(0);

				numberOfRows = jsonObject.getInt("number_of_rows");
				ret = new ContentValues[numberOfRows];
				Log.v(TAG, "Number of rows to parse: " + numberOfRows);
				numberOfRows++;
				for (i = 1; i < numberOfRows; i++) {
					jsonObject = jsonArray.getJSONObject(i);
					ret[i - 1] = convertJSONObjectToContentValue(jsonObject);
				}
			} else {
				Log.e(TAG, "Error duruing downloading questions from database");
			}

		} catch (Exception e1) {
			String message = e1.getMessage().toString();
			if (message != null) {
				Log.e(TAG, message);
			}
		}

		return ret;
	}

	private ContentValues convertJSONObjectToContentValue(
			JSONObject i_JsonObject) throws JSONException {
		ContentValues ret = new ContentValues();

		String[] keys = { TriviaDbEngine.KEY_ANSWER1,
				TriviaDbEngine.KEY_ANSWER2, TriviaDbEngine.KEY_ANSWER3,
				TriviaDbEngine.KEY_ANSWER4, TriviaDbEngine.KEY_ANSWER_INDEX,
				TriviaDbEngine.KEY_CATEGORY,
				TriviaDbEngine.KEY_CORRECT_FROM_DB, TriviaDbEngine.KEY_ENABLED,
				TriviaDbEngine.KEY_LANGUAGE, TriviaDbEngine.KEY_LAST_UPDATE,
				TriviaDbEngine.KEY_QUESTION, TriviaDbEngine.KEY_QUESTIONID,
				TriviaDbEngine.KEY_SUB_CATEGORY,
				TriviaDbEngine.KEY_WRONG_FROM_DB

		};
		int i = keys.length - 1;

		while (i > -1) {
			ret.put(keys[i], i_JsonObject.getString(keys[i]));
			i--;
		}

		return ret;
	}

	private JSONArray getQuestionAsJSONArray() {
		HttpEntity httpEntity;
		String data;
		JSONArray jsonArray = null;
		StringBuilder detailedResult = new StringBuilder();

		try {
			if (isInternetAvailable(detailedResult)) {
				StringBuilder url = new StringBuilder(m_ServerUrl);

				HttpGet get = new HttpGet(url.toString());
				HttpResponse response = m_HttpClient.execute(get);
				int status = response.getStatusLine().getStatusCode();

				if (status == 200) {
					httpEntity = response.getEntity();
					data = new String(EntityUtils.toString(httpEntity)
							.getBytes(), "UTF-8");

					jsonArray = new JSONArray(data);

				} else {
					Log.e(TAG,
							"status code from server is "
									+ Integer.toString(status));
				}
			} else {
				Toast.makeText(m_ActivityContext, detailedResult.toString(),
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e1) {
			Log.e(TAG, e1.getMessage().toString());
		}

		return jsonArray;

	}

	public void reportMistakeInQuestion(String i_QuestionId, String i_Description
			) throws ClientProtocolException,
			IOException {

		HttpEntity httpEntity;
		String data;
		String i_ServerUrl = "http://23.23.238.181/reportMistakeInQuestion.php";
		List<NameValuePair> params =new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("tag", "reportMistakeInQuestion"));
		params.add(new BasicNameValuePair("colQuestionId", i_QuestionId));
		params.add(new BasicNameValuePair("colDescription", i_Description));
		
		
		HttpPost httpPost = new HttpPost(i_ServerUrl);
		httpPost.setEntity(new UrlEncodedFormEntity(params));
		HttpResponse httpResponse = m_HttpClient.execute(httpPost);
		int status = httpResponse.getStatusLine().getStatusCode();

		if (status == 200) {
			httpEntity = httpResponse.getEntity();
			data = new String(EntityUtils.toString(httpEntity).getBytes(),
					"UTF-8");
		} else {
			Log.e(TAG, "status code from server is " + Integer.toString(status));
		}

	}

	private boolean isInternetAvailable(StringBuilder i_DetailedResult) {
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
					"checkBoxPreferenceAllowUpdateMobileNetwork", false);
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
								.append("WiFi is enabled but isn't connected, please check WiFi connection");
					}
				} else {
					i_DetailedResult
							.append("WiFi connection is disabled, please check preferences");
				}
			} else if (netType == ConnectivityManager.TYPE_MOBILE) {
				// checking if 3G connection
				if (allowUpdateMobile) {
					if (allowOnlyUpdate3G) {
						if ((netSubType == TelephonyManager.NETWORK_TYPE_UMTS || netSubType == TelephonyManager.NETWORK_TYPE_HSDPA)) {
							ret = info.isConnected();
							if (!ret) {
								i_DetailedResult
										.append("3G mobile network connection isn't available");
							}
						}
					} else {
						ret = info.isConnected();
					}

					// checking if the user allow update when roaming
					if (!allowUpdateRoaming) {
						ret = info.isConnected()
								&& !m_TelephonyManager.isNetworkRoaming();
					}
				} else {
					i_DetailedResult
							.append("Mobile connection is disabled, please check preferences");
				}

			}

		} else {
			i_DetailedResult
					.append("No connections available, check your WiFi or Mobile Network connection");

		}

		return ret;
	}

	public class GetQuestionFromServerAsyncTask extends
			AsyncTask<ContentValues, Integer, ContentValues[]> {

		private ProgressDialog m_ProgressDialog;
		private boolean isInternetAvailable;
		private StringBuilder detailedResult;

		@Override
		protected void onPreExecute() {

			//

			detailedResult = new StringBuilder();

			m_ProgressDialog = new ProgressDialog(m_ActivityContext);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_ProgressDialog.setTitle("Downloading questions");
			m_ProgressDialog.show();

			isInternetAvailable = isInternetAvailable(detailedResult);

			if (isInternetAvailable == false) {
				Toast.makeText(m_ActivityContext, detailedResult.toString(),
						Toast.LENGTH_SHORT).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected void onPostExecute(ContentValues[] result) {
			//
			m_ProgressDialog.dismiss();

			if (result != null) {
				TriviaDbEngine dbEngine = new TriviaDbEngine(m_ActivityContext);
				dbEngine.updateFromInternet(result);
			} else {
				Toast.makeText(m_ActivityContext,
						"Error while trying to update from server",
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected ContentValues[] doInBackground(ContentValues... params) {
			//
			if (isInternetAvailable) {
				JSONArray jsonArray;
				JSONObject jsonObject;
				int numberOfRows, i;

				jsonArray = getQuestionAsJSONArray();
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
							params[i - 1] = convertJSONObjectToContentValue(jsonObject);
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

}
