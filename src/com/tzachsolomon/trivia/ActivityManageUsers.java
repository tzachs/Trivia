package com.tzachsolomon.trivia;


import com.tzachsolomon.trivia.JSONHandler.UserManageListener;
import com.tzachsolomon.trivia.MyFacebook.MyFacebookListener;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Toast;

/**
 * Class acts as management class for users It provides the following function:
 * 1. Register a Trivia user 2. Register using a facebook user 3. Login as
 * Trivia user 4. Login as Facebook user
 * 
 * @author tzach
 * 
 */
public class ActivityManageUsers extends Activity implements OnClickListener,
		UserManageListener, MyFacebookListener {

	public static final int ANNONYMOUS_USER = -2;
	public static final int USER_TYPE_TRIVIA = 0;
	public static final int USER_TYPE_FACEBOOK = 1;

	private Button buttonUserRegister;
	private Button buttonUserLogin;
	private EditText editTextUsername;
	private EditText editTextEmail;
	private EditText editTextPassword;

	private JSONHandler mJSONHandler;
	private LinearLayout linearLayoutUserRequestDetails;
	private Button buttonUserRequestClose;
	private Button buttonUserRequestSend;
	private TriviaDbEngine mTriviaDb;
	private ImageView imageViewFacebookButton;

	private MyFacebook mFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_users);

		initializeVariables();
	}

	private void initializeVariables() {

		// -2 means no user login was done
		setResult(ANNONYMOUS_USER);
		//
		mJSONHandler = new JSONHandler(this);

		mJSONHandler.setUserManageListener(this);

		mTriviaDb = new TriviaDbEngine(this);

		linearLayoutUserRequestDetails = (LinearLayout) findViewById(R.id.linearLayoutUserRequestDetails);

		initButtons();
		initEditText();

		mFacebook = new MyFacebook(ActivityManageUsers.this,ActivityManageUsers.this);
		
		mFacebook.setMyFacebookListener(this);

		// TODO: check if this actually works, login a user, back, retry to
		// login user.
		// button should be logout
		updateFacebookLoginLogoutImage();
	}

	private void updateFacebookLoginLogoutImage() {
		if (mFacebook.isSessionValid()) {
			imageViewFacebookButton.setImageResource(R.drawable.logout_button);
		} else {
			imageViewFacebookButton.setImageResource(R.drawable.login_button);
		}
	}

	private void initEditText() {
		//
		editTextUsername = (EditText) findViewById(R.id.editTextUsername);
		editTextPassword = (EditText) findViewById(R.id.editTextPassword);
		editTextEmail = (EditText) findViewById(R.id.editTextEmail);

	}

	private void initButtons() {
		//

		buttonUserRegister = (Button) findViewById(R.id.buttonUserRegister);
		buttonUserLogin = (Button) findViewById(R.id.buttonUserLogin);
		buttonUserRequestClose = (Button) findViewById(R.id.buttonUserRequestClose);
		buttonUserRequestSend = (Button) findViewById(R.id.buttonUserRequestSend);

		imageViewFacebookButton = (ImageView) findViewById(R.id.imageViewFacebookButton);

		buttonUserRegister.setOnClickListener(this);
		buttonUserLogin.setOnClickListener(this);
		buttonUserRequestClose.setOnClickListener(this);
		buttonUserRequestSend.setOnClickListener(this);
		imageViewFacebookButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.imageViewFacebookButton:
			imageViewFacebookButton_Clicked();
			break;
		case R.id.buttonUserLogin:
			buttonUserLogin_Clicked();
			break;

		case R.id.buttonUserRegister:
			buttonUserRegister_Clicked();
			break;

		case R.id.buttonUserRequestClose:
			buttonUserRequestClose_Clicked();
			break;

		case R.id.buttonUserRequestSend:
			buttonUserRequestSend_Clicked();
			break;

		default:
			break;
		}

	}

	private void imageViewFacebookButton_Clicked() {
		//
		mFacebook.loginLogout();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 
		super.onActivityResult(requestCode, resultCode, data);
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	/**
	 * Function checks if parameters are valid and calls register user in JSON
	 * object return value is in callback function onUserRegister
	 * 
	 * @param email
	 * @param password
	 * @param username
	 * @param userType
	 */
	private void registerUser(String email, String password, String username,
			int userType) {
		//
		if (email.length() == 0) {
			email = "nomail";
		}

		if (password.length() == 0 || username.length() == 0) {
			Toast.makeText(ActivityManageUsers.this,
					getString(R.string.please_fill_in_username_and_password),
					Toast.LENGTH_SHORT).show();
		} else {

			mJSONHandler.userRegisterAsync(new String[] { username, password,
					email, Integer.toString(userType) });

		}

	}

	private void buttonUserRequestClose_Clicked() {
		//
		linearLayoutUserRequestDetails.setVisibility(View.GONE);
		buttonUserLogin.setVisibility(View.VISIBLE);
		buttonUserRegister.setVisibility(View.VISIBLE);
	}

	private void buttonUserRequestSend_Clicked() {

		//
		String email = editTextEmail.getText().toString();
		String password = editTextPassword.getText().toString();
		String username = editTextUsername.getText().toString();
		
		if ( buttonUserLogin.getVisibility() == View.VISIBLE){
			mJSONHandler.userLoginAsync(new String[] { username,password,email});
		}else{
			registerUser(email, password, username, USER_TYPE_TRIVIA);	
		}
		

		

	}

	private void buttonUserLogin_Clicked() {
		//
		linearLayoutUserRequestDetails.setVisibility(View.VISIBLE);
		buttonUserRegister.setVisibility(View.GONE);
		editTextEmail.setVisibility(View.GONE);
	}

	private void buttonUserRegister_Clicked() {
		//

		buttonUserLogin.setVisibility(View.GONE);
		linearLayoutUserRequestDetails.setVisibility(View.VISIBLE);
		editTextEmail.setVisibility(View.VISIBLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tzachsolomon.trivia.JSONHandler.UserManageListener#onUserLogin(java
	 * .lang.String, int, int, java.lang.String)
	 * 
	 * Function is the callback answer when calling JSONHandler.loginUser
	 */
	@Override
	public void onUserLogin(String response, int userId, int userType,
			String username) {
		//
		// setting result id for calling activity, usually the Main activity
		setResult(userId);
		if ( userId != -1){
			// checking if login in with a user that exists but isn't in the local
			// database. This might be in case the user registered once the user and
			// uninstalled the application
			// or cleared it's data
			if (!mTriviaDb.isUsersExists(Integer.toString(userId))) {
				// adding the user locally
				mTriviaDb.insertUser(userId, userType, username);

			}
			
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
			
		}else{
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
		}


	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tzachsolomon.trivia.JSONHandler.UserManageListener#onUserRegister
	 * (java.lang.String, int, int, java.lang.String)
	 * 
	 * Function is a callback answer when calling JSONHandler.registerUSer
	 */
	@Override
	public void onUserRegister(String response, int userId, int userType,
			String username) {
		// checking if userId is valid
		if (userId != -1) {
			// adding the user locally
			mTriviaDb.insertUser(userId, userType, username);
			Toast.makeText(getApplicationContext(), response,Toast.LENGTH_LONG).show();

		}else{
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onFacebookUserLogin(int userId) {
		// 
		
		Toast.makeText(ActivityManageUsers.this, getString(R.string.user_authenticated_succesfully), Toast.LENGTH_LONG).show();
		setResult(userId);
		updateFacebookLoginLogoutImage();
	}

	@Override
	public void onFacebookUserLogout() {
		// 
		Toast.makeText(ActivityManageUsers.this, getString(R.string.user_authenticated_succesfully), Toast.LENGTH_LONG).show();
		setResult(ANNONYMOUS_USER);
		updateFacebookLoginLogoutImage();
		
	}

	@Override
	public void onFacebookUserRegister(int userId) {
		// TODO Auto-generated method stub
		Toast.makeText(ActivityManageUsers.this, getString(R.string.user_registered_succesfully), Toast.LENGTH_LONG).show();
		setResult(userId);
		updateFacebookLoginLogoutImage();
		
	}

}
