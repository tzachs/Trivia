package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ViewFlipper;

public class WizardSetup extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemClickListener {

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
	private boolean m_Direction; // true - next page, false - back page
	private int m_NumberOfPages;
	private int m_CurrentPageIndex;
	private Button buttonFinish;
	private ListView listViewLanguages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_setup);

		initializeVariables();

	}

	private void initializeVariables() {
		//

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		m_SharedPreferencesEditor = m_SharedPreferences.edit();
		viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipperWizardSetup);

		m_NumberOfPages = viewFlipper.getChildCount();
		m_CurrentPageIndex = 1;
		m_Direction = true;

		buttonNext = (Button) findViewById(R.id.buttonNext);
		buttonFinish = (Button)findViewById(R.id.buttonSetupFinish);

		buttonNext.setOnClickListener(this);
		buttonFinish.setOnClickListener(this);
		
		//listViewLanguages = (ListView)findViewById(R.id.listViewLanguages);
		
		//listViewLanguages.setOnItemClickListener(this);

		initializeCheckBoxes();

	}

	private void initializeCheckBoxes() {
		
		checkBoxAllowUpdateUsingWifi = (CheckBox) findViewById(R.id.checkBoxAllowUpdateUsingWifi);
		checkBoxAllowUpdateUsingMobileNetwork = (CheckBox) findViewById(R.id.checkBoxAllowUpdateUsingMobileNetwork);
		checkBoxAllowUpdateUsing3GOnly = (CheckBox) findViewById(R.id.checkBoxAllowUpdateUsing3GOnly);
		checkBoxShowReportQuestion = (CheckBox) findViewById(R.id.checkBoxShowReportQuestion);
		checkBoxUploadWrongCorrectStatistics = (CheckBox) findViewById(R.id.checkBoxUploadWrongCorrectStatistics);
		checkBoxCheckUpdateOnStartup = (CheckBox) findViewById(R.id.checkBoxCheckUpdateOnStartup);

		checkBoxAllowUpdateUsingWifi.setOnCheckedChangeListener(this);
		checkBoxAllowUpdateUsingMobileNetwork.setOnCheckedChangeListener(this);
		checkBoxAllowUpdateUsing3GOnly.setOnCheckedChangeListener(this);
		checkBoxShowReportQuestion.setOnCheckedChangeListener(this);
		checkBoxUploadWrongCorrectStatistics.setOnCheckedChangeListener(this);
		checkBoxCheckUpdateOnStartup.setOnCheckedChangeListener(this);
		
		checkBoxAllowUpdateUsingWifi.setChecked(m_SharedPreferences.getBoolean("checkBoxPreferenceAllowUpdateWifi",true));
		checkBoxAllowUpdateUsingMobileNetwork.setChecked(m_SharedPreferences.getBoolean("checkBoxPreferenceAllowUpdateMobileNetwork",
				false));
		checkBoxAllowUpdateUsing3GOnly.setChecked(checkBoxAllowUpdateUsingMobileNetwork.isChecked());
		checkBoxAllowUpdateUsing3GOnly.setEnabled(checkBoxAllowUpdateUsingMobileNetwork.isChecked());

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonNext:
			buttonNext_Clicked();
			break;
			
		case R.id.buttonSetupFinish:
			buttonSetupFinish_Clicked();
			break;
			
		

		default:
			break;
		}

	}

	private void buttonSetupFinish_Clicked() {
		// 
		finish();
		
	}

	private void buttonNext_Clicked() {
		//
		if (m_Direction) {
			m_CurrentPageIndex++;
			if (m_CurrentPageIndex == m_NumberOfPages) {
				m_Direction = false;
				buttonNext.setText("Back");
			}
			viewFlipper.showNext();

		} else {
			m_CurrentPageIndex--;
			if (m_CurrentPageIndex == 1) {
				m_Direction = true;
				buttonNext.setText("Next");
			}
			viewFlipper.showPrevious();
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//
		switch (buttonView.getId()) {
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 
		m_SharedPreferencesEditor.putString("listPreferenceLanguages",Integer.toString(position+1)).commit();
		
		
	}

}
