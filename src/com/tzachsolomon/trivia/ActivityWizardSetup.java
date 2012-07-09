package com.tzachsolomon.trivia;

import java.util.Locale;

import android.app.Activity;
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
import android.widget.Toast;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ViewFlipper;

public class ActivityWizardSetup extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemClickListener {

	public static final String TAG = ActivityWizardSetup.class.getSimpleName();

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

	private Button buttonFinish;
	private ListView listViewLanguages;
	private Button buttonBack;
	private String[] m_LanguageValues;

	private CheckBox checkBoxShowConfigurationWizard;
	private CheckBox checkBoxQuestionLanguageEnglish;
	private CheckBox checkBoxQuestionLanguageHebrew;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_setup);

		m_LanguageValues = getResources().getStringArray(
				R.array.integerArrayLanguagesValues);

		initializeVariables();

	}

	private void initializeVariables() {
		//

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		m_SharedPreferencesEditor = m_SharedPreferences.edit();
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperWizardSetup);

		buttonNext = (Button) findViewById(R.id.buttonNext);
		buttonBack = (Button) findViewById(R.id.buttonBack);
		buttonFinish = (Button) findViewById(R.id.buttonSetupFinish);

		buttonNext.setOnClickListener(this);
		buttonBack.setOnClickListener(this);
		buttonFinish.setOnClickListener(this);

		listViewLanguages = (ListView) findViewById(R.id.listViewLanguages);

		listViewLanguages.setOnItemClickListener(this);

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

		checkBoxShowConfigurationWizard = (CheckBox) findViewById(R.id.checkBoxShowConfigurationWizard);

		checkBoxAllowUpdateUsingWifi.setOnCheckedChangeListener(this);
		checkBoxAllowUpdateUsingMobileNetwork.setOnCheckedChangeListener(this);
		checkBoxAllowUpdateUsing3GOnly.setOnCheckedChangeListener(this);
		checkBoxShowReportQuestion.setOnCheckedChangeListener(this);
		checkBoxUploadWrongCorrectStatistics.setOnCheckedChangeListener(this);
		checkBoxCheckUpdateOnStartup.setOnCheckedChangeListener(this);
		checkBoxQuestionLanguageEnglish.setOnCheckedChangeListener(this);
		checkBoxQuestionLanguageHebrew.setOnCheckedChangeListener(this);

		checkBoxAllowUpdateUsingWifi.setChecked(m_SharedPreferences.getBoolean(
				"checkBoxPreferenceAllowUpdateWifi", true));
		checkBoxAllowUpdateUsingMobileNetwork
				.setChecked(m_SharedPreferences.getBoolean(
						"checkBoxPreferenceAllowUpdateMobileNetwork", false));
		checkBoxAllowUpdateUsing3GOnly
				.setChecked(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxAllowUpdateUsing3GOnly
				.setEnabled(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxQuestionLanguageEnglish.setChecked(m_SharedPreferences
				.getBoolean("checkBoxPreferenceQuestionLanguageEnglish", true));
		checkBoxQuestionLanguageHebrew.setChecked(m_SharedPreferences
				.getBoolean("checkBoxPreferenceQuestionLanguageHebrew", true));

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {

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

	private void buttonBack_Clicked() {
		//
		viewFlipper.showPrevious();

		showHideNextBackButtons();

	}

	private void buttonSetupFinish_Clicked() {
		//
		m_SharedPreferencesEditor.putBoolean("showFirstTimeConfiguration",
				checkBoxShowConfigurationWizard.isChecked()).commit();

		finish();

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
		showHideNextBackButtons();

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//
		switch (buttonView.getId()) {
		case R.id.checkBoxQuestionLanguageEnglish:
			checkBoxQuestionLanguageEnglish_Clicked();
			break;

		case R.id.checkBoxQuestionLanguageHebrew:
			checkBoxQuestionLanguageHebrew_Clicked();
			break;
		case R.id.checkBoxAllowUpdateUsingWifi:
			m_SharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceAllowUpdateWifi", isChecked);
			m_SharedPreferencesEditor.commit();

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

		case R.id.checkBoxShowReportQuestion:
			m_SharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceShowReportQuestion", isChecked);
			m_SharedPreferencesEditor.commit();

			break;

		case R.id.checkBoxUploadWrongCorrectStatistics:
			m_SharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceUploadCorrectWrongUserStat", isChecked);
			m_SharedPreferencesEditor.commit();

			break;

		case R.id.checkBoxCheckUpdateOnStartup:
			m_SharedPreferencesEditor.putBoolean(
					"checkBoxPreferenceCheckUpdateOnStartup", isChecked);
			m_SharedPreferencesEditor.commit();

			break;

		default:
			break;

		}

	}

	private void checkBoxQuestionLanguageHebrew_Clicked() {
		//
		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceQuestionLanguageHebrew",
				checkBoxQuestionLanguageHebrew.isChecked());

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

		m_SharedPreferencesEditor.putBoolean(
				"checkBoxPreferenceQuestionLanguageEnglish",
				checkBoxQuestionLanguageEnglish.isChecked());
		
		checkBoxQuestionLanguageChangeNextVisibilty();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//

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
		Log.i(TAG, "Changed lang to " + string);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.wizard_setup);
		initializeVariables();

	}

}
