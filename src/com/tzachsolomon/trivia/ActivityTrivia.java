package com.tzachsolomon.trivia;

import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityTrivia extends Activity implements OnClickListener {

	// TODO: auto login
	// TODO: animation
	// TODO: create service to update the database daily
	// TODO: initial settings with XML file
	// 

	public static final String TAG = ActivityTrivia.class.getSimpleName();

	private static final int REQUEST_CODE_START_GAME_CATEGORIES = 1;
	private static final int REQUEST_CODE_BACK_FROM_PREFERENCES = 2;
	private static final int REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER = 3;
	private static final int REQUEST_CODE_BACK_FROM_ACTIVITY_WIZARD_SETUP = 4;

	private Button buttonNewGameAllQuestions;
	private Button buttonManageDatabase;
	private Button buttonPreferences;
	private Button buttonNewGameSimple;
	private Button buttonUpdateDatabase;

	private SharedPreferences m_SharedPreferences;

	private UpdateManager m_UpdateManager;

	private Button buttonNewGameCategories;

	private Button buttonManageUsers;
	private int m_CurrentUserId;

	private Button buttonGameScores;

	private boolean m_FirstTimeStartingDoNotTryToUpdate;

	private TriviaDbEngine m_TrivaDbEngine;

	private TextView textViewCurrentUser;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		checkIfNeedToShowFirstTimeMessageOrConfiguration();
	}

	private void checkIfNeedToShowFirstTimeMessageOrConfiguration() {
		//
		PackageInfo packageInfo = null;
		String i = m_SharedPreferences.getString("showFirstTimeMessageVersion",
				"1.0");

		m_FirstTimeStartingDoNotTryToUpdate = false;

		try {
			packageInfo = getPackageManager().getPackageInfo(
					"com.tzachsolomon.trivia", PackageManager.GET_META_DATA);

			if (!packageInfo.versionName.contentEquals(i)) {
				try {
					m_TrivaDbEngine.importFromXml();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				showWhatsNew();
				showWizardSetup();

				m_FirstTimeStartingDoNotTryToUpdate = true;

				m_SharedPreferences
						.edit()
						.putString("showFirstTimeMessageVersion",
								packageInfo.versionName).commit();

			} else if (m_SharedPreferences.getBoolean(
					"showFirstTimeConfiguration", true)) {
				showWizardSetup();
			}
		} catch (NameNotFoundException e) {
			//
			Log.e(TAG, "Could not get meta data info for Trivia");
		}

	}

	private void showWhatsNew() {
		//
		Intent intent = new Intent(this, ActivityWhatsNew.class);
		startActivity(intent);

	}

	private void showWizardSetup() {
		//
		buttonWizardSetup_Clicked();

	}
	

	@Override
	protected void onResume() {
		//
		super.onResume();
		changeLanguageTo(m_SharedPreferences.getString(
				"listPreferenceLanguages", "iw"));

		if (m_FirstTimeStartingDoNotTryToUpdate == false) {

			m_UpdateManager.updateQuestions(true);
			showUserRegister();
		}
		
		
		
		String username = m_TrivaDbEngine.getUsername(m_CurrentUserId);
		
		textViewCurrentUser.setText(getString(R.string.current_user_is_) + username);
		
		

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

		setContentView(R.layout.main);

		initializeButtons();

	}

	private void initializeVariables() {
		//
		
		m_CurrentUserId = -1;
		
		m_TrivaDbEngine = new TriviaDbEngine(this);
		m_UpdateManager = new UpdateManager(this);


	}

	private void initializeButtons() {
		//
		buttonNewGameAllQuestions = (Button) findViewById(R.id.buttonNewGameAllQuestions);
		buttonManageDatabase = (Button) findViewById(R.id.buttonManageDatabase);
		buttonPreferences = (Button) findViewById(R.id.buttonPreferences);
		buttonNewGameSimple = (Button) findViewById(R.id.buttonNewGameSimple);
		buttonUpdateDatabase = (Button) findViewById(R.id.buttonUpdateDatabase);
		buttonNewGameCategories = (Button) findViewById(R.id.buttonNewGameCategories);
		buttonManageUsers = (Button) findViewById(R.id.buttonManageUsers);
		buttonGameScores = (Button) findViewById(R.id.buttonGameScores);

		buttonNewGameAllQuestions.setOnClickListener(this);
		buttonManageDatabase.setOnClickListener(this);
		buttonPreferences.setOnClickListener(this);
		buttonNewGameSimple.setOnClickListener(this);
		buttonUpdateDatabase.setOnClickListener(this);
		buttonNewGameCategories.setOnClickListener(this);
		buttonManageUsers.setOnClickListener(this);
		buttonGameScores.setOnClickListener(this);
		
		textViewCurrentUser = (TextView)findViewById(R.id.textViewCurrentUser);

		// checking if the device is with API 11 and earlier,
		// if so, hide the preferences button since it can be done through menu
		// option
		if (android.os.Build.VERSION.SDK_INT < 11) {
			buttonPreferences.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonGameScores:
			buttonGameScores_Clicked();
			break;

		case R.id.buttonManageUsers:
			buttonManagerUsers_Clicked();
			break;

		case R.id.buttonNewGameCategories:
			buttonNewGameCategories_Clicked();
			break;

		case R.id.buttonUpdateDatabase:
			buttonUpdateDatabase_Clicked();
			break;

		case R.id.buttonNewGameAllQuestions:
			buttonNewGameAllQuestions_Clicked();
			break;

		case R.id.buttonNewGameSimple:
			buttonNewGameSimple_Clicked();
			break;

		case R.id.buttonManageDatabase:
			buttonManageDatabase_Clicked();
			break;

		case R.id.buttonPreferences:
			buttonPreferences_Clicked();
			break;
		}

	}

	private void buttonGameScores_Clicked() {
		//
		Intent intent = new Intent(this, ActivityHighScores.class);

		startActivity(intent);
	}

	private void buttonManagerUsers_Clicked() {
		//
		Intent intent = new Intent(this, ActivityManageUsers.class);

		startActivityForResult(intent,
				REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//
		switch (requestCode) {
		case REQUEST_CODE_BACK_FROM_ACTIVITY_WIZARD_SETUP:
			m_CurrentUserId = resultCode;
			// checking if there is an update of questions
			buttonUpdateDatabase_Clicked();
			break;
		case REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER:
			m_CurrentUserId = resultCode;
			break;
		case REQUEST_CODE_BACK_FROM_PREFERENCES:
			m_UpdateManager.updateServerIpFromPreferences();
			break;
		case REQUEST_CODE_START_GAME_CATEGORIES:
			if (data != null) {

				Bundle extras = data.getExtras();

				extras.putInt(ActivityGame.EXTRA_GAME_TYPE,
						ActivityGame.GAMETYPE_CATEGORIES);
				startNewGame(extras);
			}

			break;

		}
	}

	private void showUserRegister() {
		//
		

		if (m_TrivaDbEngine.isUsersEmpty()) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Register user");
			alert.setMessage("Register a user?");
			alert.setPositiveButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//
							buttonManagerUsers_Clicked();

						}

					});
			alert.setNegativeButton("Later",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//
							Toast.makeText(
									ActivityTrivia.this,
									"Registering a user gives you the ability to publish scores, play against other players, etc",
									Toast.LENGTH_LONG).show();
						}
					});
			alert.show();
		}

	}

	private void startNewGame(Bundle extras) {
		//
		Intent intent = new Intent(this, ActivityGame.class);
		intent.putExtra(ActivityGame.EXTRA_GAME_USER_ID, m_CurrentUserId);
		intent.putExtras(extras);
		startActivity(intent);
	}

	private void buttonNewGameCategories_Clicked() {
		//
		//
		Intent intent = new Intent(this, ActivityShowCategoryForGame.class);
		startActivityForResult(intent, REQUEST_CODE_START_GAME_CATEGORIES);
	}

	private void buttonUpdateDatabase_Clicked() {
		//

		m_UpdateManager.updateQuestions(false);

	}

	private void buttonWizardSetup_Clicked() {
		//
		Intent intent = new Intent(ActivityTrivia.this,
				ActivityWizardSetup.class);

		startActivityForResult(intent,
				REQUEST_CODE_BACK_FROM_ACTIVITY_WIZARD_SETUP);

	}

	private void buttonNewGameSimple_Clicked() {
		//
		Bundle extras = new Bundle();
		extras.putInt(ActivityGame.EXTRA_GAME_TYPE,
				ActivityGame.GAMETYPE_LEVELS);
		extras.putInt(ActivityGame.EXTRA_GAME_START_LEVEL, 1);

		startNewGame(extras);

	}

	private void buttonPreferences_Clicked() {
		//
		menuItemPreferences_Clicked();

	}

	private void buttonManageDatabase_Clicked() {
		//
		Intent intent = new Intent(this, ActivityManageDatabase.class);

		startActivity(intent);

	}

	private void buttonNewGameAllQuestions_Clicked() {

		//
		Bundle extras = new Bundle();

		extras.putInt(ActivityGame.EXTRA_GAME_TYPE,
				ActivityGame.GAMETYPE_ALL_QUESTIONS);
		startNewGame(extras);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//
		boolean ret = true;

		switch (item.getItemId()) {

		case R.id.menuItemAbout:
			menuItemAbout_Clicked();
			break;

		case R.id.menuItemExit:
			finish();
			break;

		case R.id.menuItemPreferences:
			menuItemPreferences_Clicked();
			break;

		default:
			ret = super.onOptionsItemSelected(item);

		}

		return ret;

	}

	private void menuItemPreferences_Clicked() {
		//
		Intent pref = new Intent(ActivityTrivia.this, ActivityPrefs.class);

		startActivityForResult(pref, REQUEST_CODE_BACK_FROM_PREFERENCES);

	}

	private void menuItemAbout_Clicked() {
		//
		Intent intent = new Intent(ActivityTrivia.this, ActivityAbout.class);
		startActivity(intent);

	}

}