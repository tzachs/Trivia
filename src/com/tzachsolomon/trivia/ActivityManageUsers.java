package com.tzachsolomon.trivia;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.tzachsolomon.trivia.JSONHandler.UserManageListener;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Toast;

public class ActivityManageUsers extends Activity implements OnClickListener,
		UserManageListener {

	public static final int ANNONYMOUS_USER = -2;
	public static final int USER_TYPE_TRIVIA = 0;
	public static final int USER_TYPE_FACEBOOK = 1;
	
	private Button buttonUserRegister;
	private Button buttonUserLogin;
	private EditText editTextUsername;
	private EditText editTextEmail;
	private EditText editTextPassword;

	private JSONHandler m_JSONHandler;
	private LinearLayout linearLayoutUserRequestDetails;
	private Button buttonUserRequestClose;
	private Button buttonUserRequestSend;
	private TriviaDbEngine m_TriviaDb;
	private ImageView imageViewFacebookButton;
	
	private Facebook mFacebook;

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
		m_JSONHandler = new JSONHandler(this);

		m_JSONHandler.setUserManageListener(this);

		m_TriviaDb = new TriviaDbEngine(this);

		linearLayoutUserRequestDetails = (LinearLayout) findViewById(R.id.linearLayoutUserRequestDetails);

		initButtons();
		initEditText();
		
		mFacebook = new Facebook(getString(R.string.facebook_app_id));
	}
	
	private void updateFacebookLoginLogout() {
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
		if ( mFacebook.isSessionValid()){
			// logout the user
			setResult(ANNONYMOUS_USER);
		}else{
			mFacebook.authorize(ActivityManageUsers.this, new Facebook.DialogListener() {
				
				@Override
				public void onFacebookError(FacebookError e) {
					// 
					Toast.makeText(ActivityManageUsers.this,"onFacebookError",Toast.LENGTH_LONG).show();
					
				}
				
				@Override
				public void onError(DialogError e) {
					// 
					Toast.makeText(ActivityManageUsers.this,"onError",Toast.LENGTH_LONG).show();
				}
				
				@Override
				public void onComplete(Bundle values) {
					// 
					// 
					try {

						String jsonUser = mFacebook.request("me");
						JSONObject jsonObject = Util.parseJson(jsonUser);

						String id = jsonObject.getString("id");
						String username = jsonObject.getString("username");
						String email = jsonObject.getString("email");

						if (!m_TriviaDb.isUsersExists(id)) {
							// Register the user
							registerUser(email, id, username,
									USER_TYPE_FACEBOOK);
						} else {
							Toast.makeText(
									getApplicationContext(),
									getString(R.string.user_authenticated_succesfully),
									Toast.LENGTH_LONG).show();
						}

						updateFacebookLoginLogout();

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
				public void onCancel() {
					// 
					Toast.makeText(ActivityManageUsers.this,"onCancel",Toast.LENGTH_LONG).show();
				}
			});
		}

	}
	
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

			m_JSONHandler.userRegisterAsync(new String[] { username, password,
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

		if (email.length() == 0) {
			email = "nomail";
		}

		if (password.length() == 0 || username.length() == 0) {
			Toast.makeText(ActivityManageUsers.this,
					getString(R.string.please_fill_in_username_and_password),
					Toast.LENGTH_SHORT).show();
		} else {

			
			if (buttonUserLogin.getVisibility() == View.VISIBLE) {
				m_JSONHandler.userLoginAsync(new String[] { username, password,
						email });
			} else {
				m_JSONHandler.userRegisterAsync(new String[] { username,
						password, email });

			}
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

	@Override
	public void onUserLogin(String i_Response, int userId, int userType,
			String username) {
		//
		setResult(userId);

		// checking if login in with a user that exists but isn't in the local
		// database
		if (!m_TriviaDb.isUsersExists(Integer.toString(userId))) {
			// adding the user locally
			m_TriviaDb.insertUser(userId, userType, username);

		}
	}

	@Override
	public void onUserRegister(String i_Respone, int userId, int userType,
			String username) {
		//
		if (userId != -1) {
			// adding the user locally
			m_TriviaDb.insertUser(userId, userType, username);

		}

	}

}
