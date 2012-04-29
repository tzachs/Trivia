package com.tzachsolomon.trivia;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

public class JSONHandler {

	private static final String TAG = JSONHandler.class.getSimpleName();

	private HttpClient m_HttpClient;
	private String m_ServerUrl;

	public JSONHandler(String i_Server) {
		m_ServerUrl = i_Server;
		m_HttpClient = new DefaultHttpClient();
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

	private ContentValues convertJSONObjectToContentValue(
			JSONObject i_JsonObject) throws JSONException {
		ContentValues ret = new ContentValues();

		String[] keys = { TriviaDbEngine.KEY_ANSWER1,
				TriviaDbEngine.KEY_ANSWER2, TriviaDbEngine.KEY_ANSWER3,
				TriviaDbEngine.KEY_ANSWER4, TriviaDbEngine.KEY_ANSWER_INDEX,
				TriviaDbEngine.KEY_CATEGORY,
				TriviaDbEngine.KEY_CORRECT_FROM_DB,
				TriviaDbEngine.KEY_DATE_CREATED, TriviaDbEngine.KEY_ENABLED,
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

	private ContentValues[] handleRead() throws ClientProtocolException,
			IOException, JSONException {
		ContentValues[] ret = null;
		HttpEntity e;
		String data;
		JSONArray jsonArray;
		JSONObject jsonObject;
		int numberOfRows, i;

		StringBuilder url = new StringBuilder(m_ServerUrl);
		

		HttpGet get = new HttpGet(url.toString());
		HttpResponse response = m_HttpClient.execute(get);
		int status = response.getStatusLine().getStatusCode();

		if (status == 200) {
			e = response.getEntity();
			data = new String(EntityUtils.toString(e).getBytes(), "UTF-8");
			
			jsonArray = new JSONArray(data);
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
			Log.e(TAG, "status code from server is " + Integer.toString(status));
		}

		return ret;
	}

	public class Read extends AsyncTask<String, Integer, ContentValues[]> {

		@Override
		protected ContentValues[] doInBackground(String... params) {
			//
			ContentValues[] ret = null;
			try {
				ret = handleRead();
			} catch (ClientProtocolException e) {
				//
				Log.e(TAG, e.getMessage().toString());
			} catch (IOException e) {
				//
				Log.e(TAG, e.getMessage().toString());

			} catch (JSONException e) {
				//
				Log.e(TAG, e.getMessage().toString());
			}

			return ret;
		}

	}

}
