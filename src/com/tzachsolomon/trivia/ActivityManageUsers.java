package com.tzachsolomon.trivia;

import com.tzachsolomon.trivia.JSONHandler.UserManageListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityManageUsers extends Activity implements OnClickListener,
		UserManageListener {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_users);

		initializeVariables();
	}

	private void initializeVariables() {

		//
		m_JSONHandler = new JSONHandler(this);

		m_JSONHandler.setUserManageListener(this);
		
		m_TriviaDb = new TriviaDbEngine(this);

		linearLayoutUserRequestDetails = (LinearLayout) findViewById(R.id.linearLayoutUserRequestDetails);

		initButtons();
		initEditText();
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

		buttonUserRegister.setOnClickListener(this);
		buttonUserLogin.setOnClickListener(this);
		buttonUserRequestClose.setOnClickListener(this);
		buttonUserRequestSend.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
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
					"Please fill in username and password", Toast.LENGTH_SHORT)
					.show();
		} else {

			if (buttonUserLogin.getVisibility() == View.VISIBLE) {
				AsyncTaskUserLogin asyncTaskUserLogin = new AsyncTaskUserLogin();

				asyncTaskUserLogin.execute(new String[] { username, password,
						email });
			} else {
				AsyncTaskUserRegister asyncTaskUserRegister = new AsyncTaskUserRegister();

				asyncTaskUserRegister.execute(new String[] { username,
						password, email });

			}
		}

	}

	private void buttonUserLogin_Clicked() {
		//
		buttonUserRegister.setVisibility(View.GONE);
		linearLayoutUserRequestDetails.setVisibility(View.VISIBLE);
	}

	private void buttonUserRegister_Clicked() {
		//
		buttonUserLogin.setVisibility(View.GONE);
		linearLayoutUserRequestDetails.setVisibility(View.VISIBLE);
	}

	public class AsyncTaskUserLogin extends AsyncTask<String, Integer, Void> {

		private ProgressDialog m_ProgressDialog;
		private boolean enabled;

		@Override
		protected void onPreExecute() {
			//
			StringBuilder detailedResult = new StringBuilder();

			enabled = m_JSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(ActivityManageUsers.this);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog.setTitle("Login");
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(ActivityManageUsers.this,
						detailedResult.toString(), Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected Void doInBackground(String... params) {
			//
			m_JSONHandler.userLogin(params);
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			//
			
			super.onPostExecute(result);
			m_ProgressDialog.dismiss();
		}

	}

	public class AsyncTaskUserRegister extends AsyncTask<String, Integer, Void> {

		private ProgressDialog m_ProgressDialog;
		private boolean enabled;

		@Override
		protected void onPreExecute() {
			//
			StringBuilder detailedResult = new StringBuilder();

			enabled = m_JSONHandler.isInternetAvailable(detailedResult);
			if (enabled) {
				m_ProgressDialog = new ProgressDialog(ActivityManageUsers.this);
				m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				m_ProgressDialog.setTitle("Register");
				m_ProgressDialog.setCancelable(true);
				m_ProgressDialog.show();
			} else {
				Toast.makeText(ActivityManageUsers.this,
						detailedResult.toString(), Toast.LENGTH_LONG).show();
			}

			detailedResult.setLength(0);

		}

		@Override
		protected Void doInBackground(String... params) {
			//
			m_JSONHandler.userRegister(params);
			return null;

		}
		
		@Override
		protected void onPostExecute(Void result) {
			// 

			super.onPostExecute(result);
			m_ProgressDialog.dismiss();
		}

	}

	@Override
	public void onUserLogin(String i_Response, int i_UserId) {
		//
		if (i_UserId != -1) {
			Toast.makeText(this, "User logged in", Toast.LENGTH_SHORT);
		}

		Toast.makeText(ActivityManageUsers.this, i_Response, Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void onUserRegister(String i_Respone, int i_UserId) {
		//
		if (i_UserId != -1) {
			// adding the user locally
			m_TriviaDb.insertUser(i_UserId, editTextUsername.getText().toString(), editTextPassword.getText().toString());
		}
		Toast.makeText(ActivityManageUsers.this, i_Respone, Toast.LENGTH_LONG)
				.show();
	}

}
