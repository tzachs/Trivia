package com.tzachsolomon.trivia;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import android.os.Bundle;
import android.util.Log;


import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.Util;
import com.tzachsolomon.trivia.JSONHandler.UserManageListener;


public class MyFacebook extends Facebook implements DialogListener, UserManageListener {

	private static final String TAG = MyFacebook.class.getSimpleName();

	private TriviaDbEngine mTriviaDb;

	private Activity mActivity;

	private Context mContext;

	private MyFacebookListener mMyFacebookListener;
	private JSONHandler mJSONHandler;

	private boolean mForceUserRegister;

	public MyFacebook(Context context, Activity activity) {
		super("203003926497543");

		mContext = context;
		mActivity = activity;
		
		mTriviaDb = new TriviaDbEngine(mContext);
		mJSONHandler = new JSONHandler(mContext);
		
		mJSONHandler.setUserManageListener(this);
		
		

	}

	

	public void loginLogout() {
		if (this.isSessionValid()) {
			// logout the user
			try {
				this.logout(mContext);
				mForceUserRegister = false;
			} catch (MalformedURLException e) {
				// 
				e.printStackTrace();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}catch (Exception e){
				Log.d(TAG, e.getMessage());
			}
		} else {
			// login the user
			this.authorize(mActivity, new String[] { "email" }, this);
		}
	}

	@Override
	public void onComplete(Bundle values) {
		// 
		try {

			String jsonUser = MyFacebook.this.request("me");
			JSONObject jsonObject = Util.parseJson(jsonUser);

			String id = jsonObject.getString("id");
			String username = jsonObject.getString("username");
			String email = jsonObject.getString("email");

			if (!mTriviaDb.isUsersExists(id)) {
				// Register the user
				mForceUserRegister = true;
				mJSONHandler.userRegisterAsync(new String[] { username, id,
						email, Integer.toString(ActivityManageUsers.USER_TYPE_FACEBOOK) });
				
				
			} else {
				// User login
				if ( mMyFacebookListener != null ){
					mMyFacebookListener.onFacebookUserLogin(Integer.valueOf(id));
				}
			}

		} catch (MalformedURLException e) {
			//
			e.printStackTrace();
		} catch (IOException e) {
			//
			e.printStackTrace();
		} catch (JSONException e) {

		}

	}
	
	
	

	@Override
	public void onFacebookError(FacebookError e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(DialogError e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub

	}
	
	static public interface MyFacebookListener {
		public void onFacebookUserLogin (int userId);
		public void onFacebookUserLogout ();
		public void onFacebookUserRegister (int userId);
	}
	
	public void setMyFacebookListener ( MyFacebookListener listener ){
		this.mMyFacebookListener = listener;
	}

	@Override
	public void onUserLogin(String i_Response, int userId, int userType,
			String username) {
		
		if ( userType == ActivityManageUsers.USER_TYPE_FACEBOOK){
			if ( mForceUserRegister){
				// reaching this point means the user is registered at the database but isn't registered 
				// on the local DB
				// This should happen for example, in case the user installed the app, registered, uninstalled the app
				// and then reinstalled and registered again
				mTriviaDb.insertUser(userId, userType, username);
			}
			if (mMyFacebookListener != null ){
				mMyFacebookListener.onFacebookUserLogin(userId);
			}
		}
	}

	@Override
	public void onUserRegister(String i_Response, int userId, int userType,
			String username) {
		// 
		if ( userType == ActivityManageUsers.USER_TYPE_FACEBOOK){
			mTriviaDb.insertUser(userId, userType, username);
			if (mMyFacebookListener != null ){
				mMyFacebookListener.onFacebookUserRegister(userId);
			}
		}
		
	}

}
