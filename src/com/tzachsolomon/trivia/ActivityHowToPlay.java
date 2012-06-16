package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

public class ActivityHowToPlay extends Activity implements OnClickListener {

	public static final String KEY_HOW_TO_PLAY_TITLE = "1";
	public static final String KEY_HOW_TO_PLAY_INSTRUCTIONS_TITLE = "2";
	public static final String KEY_HOW_TO_PLAY_INSTRUCTIONS_MESSAGE = "3";

	private TextView textViewHowToPlayInstructionsMessage;
	private TextView textViewHowToPlayInstructionsTitle;
	private TextView textViewHowToPlayTitle;
	private CheckBox checkBoxShowGameInstructionsEveryTime;

	private int m_GameType;
	private SharedPreferences m_SharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_how_to_play);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();
		setTexts();
		
		
	}

	private void setTexts() {
		//
		Bundle extras = getIntent().getExtras();

		textViewHowToPlayInstructionsMessage.setText(extras
				.getString(KEY_HOW_TO_PLAY_INSTRUCTIONS_MESSAGE));
		textViewHowToPlayInstructionsTitle.setText(extras
				.getString(KEY_HOW_TO_PLAY_INSTRUCTIONS_TITLE));
		textViewHowToPlayTitle.setText(extras.getString(KEY_HOW_TO_PLAY_TITLE));

		m_GameType = extras.getInt(ActivityGame.INTENT_EXTRA_GAME_TYPE);
		
		

	}

	private void initializeVariables() {
		//
		textViewHowToPlayInstructionsMessage = (TextView) findViewById(R.id.textViewHowToPlayInstructionsMessage);
		textViewHowToPlayInstructionsTitle = (TextView) findViewById(R.id.textViewHowToPlayInstructionsTitle);
		textViewHowToPlayTitle = (TextView) findViewById(R.id.textViewHowToPlayTitle);

		checkBoxShowGameInstructionsEveryTime = (CheckBox) findViewById(R.id.checkBoxShowGameInstructionsEveryTime);

		checkBoxShowGameInstructionsEveryTime.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {

		case R.id.checkBoxShowGameInstructionsEveryTime:
			checkBoxShowGameInstructionsEveryTime_Clicked();
			break;

		}

	}
	
	@Override
	protected void onStop() {
		// 
		super.onStop();
		setResult(1);
	}

	private void checkBoxShowGameInstructionsEveryTime_Clicked() {
		//
		switch (m_GameType) {
		case ActivityGame.GAMETYPE_ALL_QUESTIONS:
			m_SharedPreferences.edit().putBoolean(
					"checkBoxPreferenceShowHelpAllQuestions",
					checkBoxShowGameInstructionsEveryTime.isChecked()).commit();

			break;
		case ActivityGame.GAMETYPE_CATEGORIES:

			break;
		case ActivityGame.GAMETYPE_LEVELS:
			m_SharedPreferences.edit().putBoolean(
					"checkBoxPreferenceShowHelpNewGame",
					checkBoxShowGameInstructionsEveryTime.isChecked()).commit();
			break;

		default:
			break;
		}

	}

}
