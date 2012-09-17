package com.tzachsolomon.trivia;

import java.util.Locale;

import com.facebook.android.FacebookError;

import com.tzachsolomon.trivia.JSONHandler.UserManageListener;
import com.tzachsolomon.trivia.MyFacebook.MyFacebookListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ViewFlipper;

public class ActivityWizardSetup extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemClickListener, UserManageListener,
		MyFacebookListener {

	public static final String TAG = ActivityWizardSetup.class.getSimpleName();

	private JSONHandler mJSONHandler;
	private TriviaDbEngine mTriviaDb;

	private ViewFlipper viewFlipper;
	private Button buttonNext;
	private CheckBox checkBoxAllowUpdateUsingWifi;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mSharedPreferencesEditor;
	private CheckBox checkBoxAllowUpdateUsingMobileNetwork;
	private CheckBox checkBoxAllowUpdateUsing3GOnly;
	private CheckBox checkBoxShowReportQuestion;
	private CheckBox checkBoxUploadWrongCorrectStatistics;
	private CheckBox checkBoxCheckUpdateOnStartup;

	private Button buttonUserRegister;
	private Button buttonFinish;
	private ListView listViewLanguages;
	private Button buttonBack;
	private String[] mLanguageValues;

	private CheckBox checkBoxShowConfigurationWizard;
	private CheckBox checkBoxQuestionLanguageEnglish;
	private CheckBox checkBoxQuestionLanguageHebrew;

	private CheckBox checkBoxPlayGameSounds;

	private boolean mChoseLanguage;

	private EditText editTextUsername;
	private EditText editTextPassword;
	private EditText editTextEmail;

	private ImageView imageViewFacebookButton;

	private MyFacebook mFacebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_setup);

		mLanguageValues = getResources().getStringArray(
				R.array.integerArrayLanguagesValues);

		mChoseLanguage = false;

		initializeVariables();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//
		super.onActivityResult(requestCode, resultCode, data);

		try {
			mFacebook.authorizeCallback(requestCode, resultCode, data);
		} catch (FacebookError e) {
			Toast.makeText(ActivityWizardSetup.this,
					"Error connecting to facebook", Toast.LENGTH_LONG).show();
			Log.d(TAG, e.getMessage());
		} catch (Exception e) {

			Toast.makeText(ActivityWizardSetup.this,
					"Error connecting to facebook", Toast.LENGTH_LONG).show();
			Log.d(TAG, e.getMessage());
		}
	}

	private void updateFacebookLoginLogout() {
		if (mFacebook.isSessionValid()) {
			imageViewFacebookButton.setImageResource(R.drawable.logout_button);
		} else {
			imageViewFacebookButton.setImageResource(R.drawable.login_button);
		}
	}

	private void initializeVariables() {
		//

		mTriviaDb = new TriviaDbEngine(ActivityWizardSetup.this);
		mJSONHandler = new JSONHandler(ActivityWizardSetup.this);

		mJSONHandler.setUserManageListener(this);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		mSharedPreferencesEditor = mSharedPreferences.edit();
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperWizardSetup);

		buttonNext = (Button) findViewById(R.id.buttonNext);
		buttonBack = (Button) findViewById(R.id.buttonBack);
		buttonFinish = (Button) findViewById(R.id.buttonSetupFinish);
		//buttonUserRegister = (Button) findViewById(R.id.buttonUserRegister);

		//imageViewFacebookButton = (ImageView) findViewById(R.id.imageViewFacebookButton);

		buttonNext.setOnClickListener(this);
		buttonBack.setOnClickListener(this);
		buttonFinish.setOnClickListener(this);
		//buttonUserRegister.setOnClickListener(this);
		//imageViewFacebookButton.setOnClickListener(this);

		listViewLanguages = (ListView) findViewById(R.id.listViewLanguages);

		listViewLanguages.setOnItemClickListener(this);

		editTextUsername = (EditText) findViewById(R.id.editTextUsername);
		editTextPassword = (EditText) findViewById(R.id.editTextPassword);
		editTextEmail = (EditText) findViewById(R.id.editTextEmail);

		mFacebook = new MyFacebook(ActivityWizardSetup.this,
				ActivityWizardSetup.this);

		mFacebook.setMyFacebookListener(this);

		initializeCheckBoxes();

	}

	private void initializeCheckBoxes() {

		checkBoxAllowUpdateUsingWifi = (CheckBox) findViewById(R.id.checkBoxAllowUpdateUsingWifi);
		checkBoxAllowUpdateUsingMobileNetwork = (CheckBox) findViewById(R.id.checkBoxAllowUpdateUsingMobileNetwork);
		checkBoxAllowUpdateUsing3GOnly = (CheckBox) findViewById(R.id.checkBoxAllowUpdateUsing3GOnly);
		checkBoxShowReportQuestion = (CheckBox) findViewById(R.id.checkBoxShowReportQuestion);
		checkBoxUploadWrongCorrectStatistics = (CheckBox) findViewById(R.id.checkBoxUploadWrongCorrectStatistics);
		checkBoxCheckUpdateOnStartup = (CheckBox) findViewById(R.id.checkBoxCheckUpdateOnStartup);
		checkBoxQuestionLanguageEnglish = (CheckBox) findViewById(R.id.checkBoxQuestionLanguageEnglish);
		checkBoxQuestionLanguageHebrew = (CheckBox) findViewById(R.id.checkBoxQuestionLanguageHebrew);
		checkBoxPlayGameSounds = (CheckBox) findViewById(R.id.checkBoxPlayGameSounds);

		checkBoxShowConfigurationWizard = (CheckBox) findViewById(R.id.checkBoxShowConfigurationWizard);

		checkBoxAllowUpdateUsingWifi.setOnCheckedChangeListener(this);
		checkBoxAllowUpdateUsingMobileNetwork.setOnCheckedChangeListener(this);
		checkBoxAllowUpdateUsing3GOnly.setOnCheckedChangeListener(this);
		checkBoxShowReportQuestion.setOnCheckedChangeListener(this);
		checkBoxUploadWrongCorrectStatistics.setOnCheckedChangeListener(this);
		checkBoxCheckUpdateOnStartup.setOnCheckedChangeListener(this);
		checkBoxQuestionLanguageEnglish.setOnClickListener(this);
		checkBoxQuestionLanguageHebrew.setOnClickListener(this);
		checkBoxPlayGameSounds.setOnCheckedChangeListener(this);

		checkBoxAllowUpdateUsingWifi.setChecked(mSharedPreferences.getBoolean(
				"checkBoxPreferenceAllowUpdateWifi", true));
		checkBoxAllowUpdateUsingMobileNetwork
				.setChecked(mSharedPreferences.getBoolean(
						"checkBoxPreferenceAllowUpdateMobileNetwork", true));
		checkBoxAllowUpdateUsing3GOnly
				.setChecked(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxAllowUpdateUsing3GOnly
				.setEnabled(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxQuestionLanguageEnglish.setChecked(mSharedPreferences
				.getBoolean("checkBoxPreferenceQuestionLanguageEnglish", true));
		checkBoxQuestionLanguageHebrew.setChecked(mSharedPreferences
				.getBoolean("checkBoxPreferenceQuestionLanguageHebrew", true));

		if (!mChoseLanguage) {
			buttonNext.setVisibility(View.GONE);

		} else {
			buttonNext.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.imageViewFacebookButton:
			imageViewFacebookButton_Clicked();
			break;

		case R.id.buttonUserRegister:
			buttonUserRegister_Clicked();
			break;

		case R.id.checkBoxQuestionLanguageEnglish:
			checkBoxQuestionLanguageEnglish_Clicked();
			break;

		case R.id.checkBoxQuestionLanguageHebrew:
			checkBoxQuestionLanguageHebrew_Clicked();
			break;

		case R.id.buttonNext:
			buttonNext_Clicked();
			break;

		case R.id.buttonBack:
			buttonBack_Clicked();
			break;

		case R.id.buttonSetupFinish:
			buttonSetupFinish_Clicked();
			break;

		default:
			break;
		}

	}

	private void imageViewFacebookButton_Clicked() {
		//
		mFacebook.loginLogout();

	}

	private void buttonUserRegister_Clicked() {
		//

		String email = editTextEmail.getText().toString();
		String password = editTextPassword.getText().toString();
		String username = editTextUsername.getText().toString();

		registerUser(email, password, username,
				ActivityManageUsers.USER_TYPE_TRIVIA);

	}

	private void registerUser(String email, String password, String username,
			int userType) {
		//
		if (email.length() == 0) {
			email = "nomail";
		}

		if (password.length() == 0 || username.length() == 0) {
			Toast.makeText(ActivityWizardSetup.this,
					getString(R.string.please_fill_in_username_and_password),
					Toast.LENGTH_SHORT).show();
		} else {

			mJSONHandler.userRegisterAsync(new String[] { username, password,
					email, Integer.toString(userType) });

		}

	}

	private void buttonBack_Clicked() {
		//
		viewFlipper.showPrevious();

		showHideNextBackButtons();

	}

	private void buttonSetupFinish_Clicked() {
		//

		saveChoices();

		finish();

	}

	private void saveChoices() {
		//
		// for debug only
		// m_SharedPreferencesEditor.putString("editTextPreferencePrimaryServerIP","http://192.168.200.100/index.php");

		mSharedPreferencesEditor.putBoolean("showFirstTimeConfiguration",
				checkBoxShowConfigurationWizard.isChecked());
		mSharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceAllowUpdateWifi",
				checkBoxAllowUpdateUsingWifi.isChecked());
		mSharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceShowReportQuestion",
				checkBoxShowReportQuestion.isChecked());

		mSharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceUploadCorrectWrongUserStat",
				checkBoxUploadWrongCorrectStatistics.isChecked());

		mSharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceCheckUpdateOnStartup",
				checkBoxCheckUpdateOnStartup.isChecked());

		mSharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceQuestionLanguageHebrew",
				checkBoxQuestionLanguageHebrew.isChecked());

		mSharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceQuestionLanguageEnglish",
				checkBoxQuestionLanguageEnglish.isChecked());

		mSharedPreferencesEditor.commit();

	}

	private void showHideNextBackButtons() {

		if (viewFlipper.getChildCount() - 1 == viewFlipper.getDisplayedChild()) {
			buttonNext.setVisibility(View.GONE);
		} else {
			buttonNext.setVisibility(View.VISIBLE);
		}

		if (viewFlipper.getDisplayedChild() == 0) {
			buttonBack.setVisibility(View.GONE);
		} else {
			buttonBack.setVisibility(View.VISIBLE);
		}
	}

	private void buttonNext_Clicked() {
		//

		viewFlipper.showNext();
		Object tag = viewFlipper.getCurrentView().getTag();

		if (tag != null) {
			if (tag.toString().contentEquals("chooseQuestionLanguage")) {
				checkBoxQuestionLanguageChangeNextVisibilty();
			}
		} else {

			showHideNextBackButtons();
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//
		switch (buttonView.getId()) {

		case R.id.checkBoxPlayGameSounds:
			checkBoxPlayGameSounds_Clicked();
			break;

		case R.id.checkBoxAllowUpdateUsingMobileNetwork:
			mSharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork", isChecked);
			checkBoxAllowUpdateUsing3GOnly.setChecked(isChecked);
			checkBoxAllowUpdateUsing3GOnly.setEnabled(isChecked);
			mSharedPreferencesEditor.commit();

			break;

		case R.id.checkBoxAllowUpdateUsing3GOnly:
			mSharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork3G", isChecked);
			mSharedPreferencesEditor.commit();

			break;

		default:
			break;

		}

	}

	private void checkBoxPlayGameSounds_Clicked() {
		//
		mSharedPreferencesEditor.putBoolean("checkBoxPreferencePlayGameSounds",
				checkBoxPlayGameSounds.isChecked()).commit();

	}

	private void checkBoxQuestionLanguageHebrew_Clicked() {
		//

		checkBoxQuestionLanguageChangeNextVisibilty();
	}

	private void checkBoxQuestionLanguageChangeNextVisibilty() {
		if (checkBoxQuestionLanguageEnglish.isChecked()
				|| checkBoxQuestionLanguageHebrew.isChecked()) {
			buttonNext.setVisibility(View.VISIBLE);
		} else {

			buttonNext.setVisibility(View.GONE);

		}
	}

	private void checkBoxQuestionLanguageEnglish_Clicked() {
		//

		checkBoxQuestionLanguageChangeNextVisibilty();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//
		mChoseLanguage = true;
		mSharedPreferencesEditor.putString("listPreferenceLanguages",
				mLanguageValues[position]).commit();

		changeLanguageTo(mSharedPreferences.getString(
				"listPreferenceLanguages", "iw"));

		Toast.makeText(ActivityWizardSetup.this,
				getString(R.string.language_chosen), Toast.LENGTH_SHORT).show();
	}

	private void changeLanguageTo(String string) {
		//

		Locale locale = new Locale(string);
		Locale.setDefault(locale);

		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.wizard_setup);
		initializeVariables();

		if (string.contentEquals("iw")) {
			checkBoxQuestionLanguageEnglish.setChecked(false);
			checkBoxQuestionLanguageHebrew.setChecked(true);
		} else if (string.contentEquals("en")) {
			checkBoxQuestionLanguageEnglish.setChecked(true);
			checkBoxQuestionLanguageHebrew.setChecked(false);
		}

	}

	@Override
	public void onUserLogin(String response, int userId, int userType,
			String username) {
		//
		mSharedPreferencesEditor.putInt("defaultUserId", userId);
		setResult(userId);

		if (userId != -1) {
			// checking if login in with a user that exists but isn't in the
			// local
			// database. This might be in case the user registered once the user
			// and
			// uninstalled the application
			// or cleared it's data
			if (!mTriviaDb.isUsersExists(Integer.toString(userId))) {
				// adding the user locally
				mTriviaDb.insertUser(userId, userType, username);

			}

			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG)
					.show();

		} else {
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onUserRegister(String response, int userId, int userType,
			String username) {

		// checking if userId is valid
		if (userId != -1) {
			// adding the user locally
			mTriviaDb.insertUser(userId, userType, username);
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG)
					.show();

		} else {
			Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG)
					.show();
		}

		setResult(userId);

	}

	@Override
	public void onFacebookUserLogin(int userId) {
		//
		Toast.makeText(ActivityWizardSetup.this,
				getString(R.string.user_authenticated_succesfully),
				Toast.LENGTH_LONG).show();
		setResult(userId);
		updateFacebookLoginLogout();
	}

	@Override
	public void onFacebookUserLogout() {
		//
		Toast.makeText(ActivityWizardSetup.this, "User logged out succesfully",
				Toast.LENGTH_LONG).show();
		updateFacebookLoginLogout();

	}

	@Override
	public void onFacebookUserRegister(int userId) {
		//
		Toast.makeText(ActivityWizardSetup.this,
				getString(R.string.user_registered_succesfully),
				Toast.LENGTH_LONG).show();
		setResult(userId);
		updateFacebookLoginLogout();

	}

}
