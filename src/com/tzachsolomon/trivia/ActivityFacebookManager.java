package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class ActivityFacebookManager extends Activity {
	
	String APP_ID = "203003926497543";
	Facebook mFacebook;
	private FacebookListener mFacebookListener;
	
 
	
	public ActivityFacebookManager () {
		mFacebook = new Facebook(APP_ID);
		
		
	}
	
	public void loginLogoutFacebookUser () {
		if ( mFacebook.isSessionValid()){
			// logout the user
			
		} else {
			// login the user
			mFacebook.authorize(ActivityFacebookManager.this, new Facebook.DialogListener() {
				
				@Override
				public void onFacebookError(FacebookError e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onError(DialogError e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onComplete(Bundle values) {
					// TODO Auto-generated method stub
					if ( mFacebookListener != null){
						mFacebookListener.onFacebookLogin(values);
					}
				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
			});
				
			
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 
		super.onActivityResult(requestCode, resultCode, data);
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	
	static public interface FacebookListener {
		public void onFacebookLogin (Bundle values);
		public void onFacebookLogout (int userId);
		public void onFacebookError (String errorMessage);
		
	}
	
	public void setFacebookListener (FacebookListener listener ){
		this.mFacebookListener = listener;
	}
	
	

}
