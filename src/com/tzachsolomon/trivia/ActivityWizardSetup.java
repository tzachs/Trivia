package com.tzachsolomon.trivia;

import java.util.Locale;

import com.tzachsolomon.trivia.JSONHandler.UserManageListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.Bundle;
import android.preference.PreferenceManager;

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
		OnCheckedChangeListener, OnItemClickListener, UserManageListener {

	public static final String TAG = ActivityWizardSetup.class.getSimpleName();

	private JSONHandler m_JSONHandler;
	private TriviaDbEngine m_TriviaDb;

	private ViewFlipper viewFlipper;
	private Button buttonNext;
	private CheckBox checkBoxAllowUpdateUsingWifi;
	private SharedPreferences m_SharedPreferences;
	private SharedPreferences.Editor m_SharedPreferencesEditor;
	private CheckBox checkBoxAllowUpdateUsingMobileNetwork;
	private CheckBox checkBoxAllowUpdateUsing3GOnly;
	private CheckBox checkBoxShowReportQuestion;
	private CheckBox checkBoxUploadWrongCorrectStatistics;
	private CheckBox checkBoxCheckUpdateOnStartup;

	private Button buttonUserRegister;
	private Button buttonFinish;
	private ListView listViewLanguages;
	private Button buttonBack;
	private String[] m_LanguageValues;

	private CheckBox checkBoxShowConfigurationWizard;
	private CheckBox checkBoxQuestionLanguageEnglish;
	private CheckBox checkBoxQuestionLanguageHebrew;

	private CheckBox checkBoxPlayGameSounds;

	private boolean m_ChoseLanguage;

	private EditText editTextUsername;

	private EditText editTextPassword;

	private EditText editTextEmail;

	private ImageView imageViewFacebookButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_setup);

		m_LanguageValues = getResources().getStringArray(
				R.array.integerArrayLanguagesValues);

		m_ChoseLanguage = false;

		initializeVariables();

	}

	private void initializeVariables() {
		//
		m_TriviaDb = new TriviaDbEngine(ActivityWizardSetup.this);
		m_JSONHandler = new JSONHandler(ActivityWizardSetup.this);

		m_JSONHandler.setUserManageListener(this);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		m_SharedPreferencesEditor = m_SharedPreferences.edit();
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperWizardSetup);

		buttonNext = (Button) findViewById(R.id.buttonNext);
		buttonBack = (Button) findViewById(R.id.buttonBack);
		buttonFinish = (Button) findViewById(R.id.buttonSetupFinish);
		buttonUserRegister = (Button) findViewById(R.id.buttonUserRegister);
		
		imageViewFacebookButton = (ImageView)findViewById(R.id.imageViewFacebookButton);

		buttonNext.setOnClickListener(this);
		buttonBack.setOnClickListener(this);
		buttonFinish.setOnClickListener(this);
		buttonUserRegister.setOnClickListener(this);
		imageViewFacebookButton.setOnClickListener(this);

		listViewLanguages = (ListView) findViewById(R.id.listViewLanguages);

		listViewLanguages.setOnItemClickListener(this);

		editTextUsername = (EditText) findViewById(R.id.editTextUsername);
		editTextPassword = (EditText) findViewById(R.id.editTextPassword);
		editTextEmail = (EditText) findViewById(R.id.editTextEmail);

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

		checkBoxAllowUpdateUsingWifi.setChecked(m_SharedPreferences.getBoolean(
				"checkBoxPreferenceAllowUpdateWifi", true));
		checkBoxAllowUpdateUsingMobileNetwork
				.setChecked(m_SharedPreferences.getBoolean(
						"checkBoxPreferenceAllowUpdateMobileNetwork", true));
		checkBoxAllowUpdateUsing3GOnly
				.setChecked(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxAllowUpdateUsing3GOnly
				.setEnabled(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxQuestionLanguageEnglish.setChecked(m_SharedPreferences
				.getBoolean("checkBoxPreferenceQuestionLanguageEnglish", true));
		checkBoxQuestionLanguageHebrew.setChecked(m_SharedPreferences
				.getBoolean("checkBoxPreferenceQuestionLanguageHebrew", true));

		if (!m_ChoseLanguage) {
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
		// TODO: facebook class
		
	}

	private void buttonUserRegister_Clicked() {
		//
		String email = editTextEmail.getText().toString();
		String password = editTextPassword.getText().toString();
		String username = editTextUsername.getText().toString();

		if (email.length() == 0) {
			email = "nomail";
		}

		if (password.length() == 0 || username.length() == 0) {
			Toast.makeText(ActivityWizardSetup.this,
					getString(R.string.please_fill_in_username_and_password), Toast.LENGTH_SHORT)
					.show();
		} else {

			m_JSONHandler.userRegisterAsync(new String[] { username, password,
					email });

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
		//  for debug only
		//m_SharedPreferencesEditor.putString("editTextPreferencePrimaryServerIP","http://192.168.200.100/index.php");
		
		m_SharedPreferencesEditor.putBoolean("showFirstTimeConfiguration",
				checkBoxShowConfigurationWizard.isChecked());
		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceAllowUpdateWifi",
				checkBoxAllowUpdateUsingWifi.isChecked());
		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceShowReportQuestion",
				checkBoxShowReportQuestion.isChecked());

		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceUploadCorrectWrongUserStat",
				checkBoxUploadWrongCorrectStatistics.isChecked());

		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceCheckUpdateOnStartup",
				checkBoxCheckUpdateOnStartup.isChecked());
		
		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceQuestionLanguageHebrew",
				checkBoxQuestionLanguageHebrew.isChecked());
		
		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceQuestionLanguageEnglish",
				checkBoxQuestionLanguageEnglish.isChecked());

		m_SharedPreferencesEditor.commit();

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
			m_SharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork", isChecked);
			checkBoxAllowUpdateUsing3GOnly.setChecked(isChecked);
			checkBoxAllowUpdateUsing3GOnly.setEnabled(isChecked);
			m_SharedPreferencesEditor.commit();

			break;

		case R.id.checkBoxAllowUpdateUsing3GOnly:
			m_SharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceAllowUpdateMobileNetwork3G", isChecked);
			m_SharedPreferencesEditor.commit();

			break;

		default:
			break;

		}

	}

	private void checkBoxPlayGameSounds_Clicked() {
		//
		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferencePlayGameSounds",
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
		m_ChoseLanguage = true;
		m_SharedPreferencesEditor.putString("listPreferenceLanguages",
				m_LanguageValues[position]).commit();

		changeLanguageTo(m_SharedPreferences.getString(
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
	public void onUserLogin(String i_Response, int i_UserId) {
		//

	}

	@Override
	public void onUserRegister(String i_Response, int i_UserId) {

		if (i_UserId != -1) {
			// adding the user locally
			m_TriviaDb.insertUser(i_UserId, editTextUsername.getText()
					.toString(), editTextPassword.getText().toString());

		} 
 

		m_SharedPreferencesEditor.putInt("defaultUserId", i_UserId);
		setResult(i_UserId);
	}

}
