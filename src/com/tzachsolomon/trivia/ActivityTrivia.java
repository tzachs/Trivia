package com.tzachsolomon.trivia;

import java.util.Locale;

import com.tzachsolomon.trivia.UpdateManager.CategoriesListener;
import com.tzachsolomon.trivia.UpdateManager.QuestionsListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

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

public class ActivityTrivia extends Activity implements OnClickListener,
		CategoriesListener, QuestionsListener {

	// TODO: auto login
	// TODO: create service to update the database daily

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
	private Button buttonNewGameCategories;
	private Button buttonGameScores;
	private Button buttonManageUsers;

	private SharedPreferences m_SharedPreferences;

	private UpdateManager m_UpdateManager;

	private int m_CurrentUserId;

	private boolean m_FirstTimeStartingDoNotTryToUpdate;
	private TriviaDbEngine m_TrivaDbEngine;
	private TextView textViewCurrentUser;

	private boolean m_ButtonsLocckedDueToImportFromXML;

	protected boolean m_RegisterLater;

	private ProgressDialog m_ProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();
		initializeButtons();
		checkIfNeedToShowFirstTimeMessageOrConfiguration();
	}

	private void checkIfNeedToShowFirstTimeMessageOrConfiguration() {
		//
		PackageInfo packageInfo = null;
		String i = m_SharedPreferences.getString("showWhatsNew", "1.0");

		m_FirstTimeStartingDoNotTryToUpdate = false;

		try {
			packageInfo = getPackageManager().getPackageInfo(
					"com.tzachsolomon.trivia", PackageManager.GET_META_DATA);

			if (!packageInfo.versionName.contentEquals(i)) {

				m_FirstTimeStartingDoNotTryToUpdate = true;

				// Log.v(TAG, "Starting import questions from xml");
				Toast.makeText(this,
						"Starting importing questions from initial file",
						Toast.LENGTH_LONG).show();
				m_ButtonsLocckedDueToImportFromXML = true;

				m_UpdateManager.importQuestionsFromXml();

				showWhatsNew();
				showWizardSetup();

				m_SharedPreferences.edit()
						.putString("showWhatsNew", packageInfo.versionName)
						.commit();

			} else if (m_SharedPreferences.getBoolean(
					"showFirstTimeConfiguration", true)) {
				showWizardSetup();
			}
		} catch (NameNotFoundException e) {
			//
			Log.e(TAG, "Could not get meta data info for Trivia");
		}

	}

	private void lockButtonOnImportFromXMLFile() {
		//

		setLockReleaseButtons(false);

	}

	private void releaseButtonsOnImportFromXMLFile() {
		setLockReleaseButtons(true);

	}

	private void setLockReleaseButtons(boolean b) {
		//
		buttonGameScores.setEnabled(b);
		buttonNewGameAllQuestions.setEnabled(b);
		buttonNewGameCategories.setEnabled(b);
		// dbuttonNewGameSimple.setEnabled(b);
		buttonUpdateDatabase.setEnabled(b);
		buttonManageDatabase.setEnabled(b);
		buttonManageUsers.setEnabled(b);
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
	protected void onStart() {
		// 
		super.onStart();
		
		try{
			m_CurrentUserId = Integer.valueOf(m_SharedPreferences.getInt("defaultUserId", -2));	
		}catch (Exception e){
			m_CurrentUserId = -2;
		}
		
	}

	@Override
	protected void onResume() {
		//
		super.onResume();
		changeLanguageTo(m_SharedPreferences.getString(
				"listPreferenceLanguages", "iw"));

		if (m_CurrentUserId > 0) {

			String username = m_TrivaDbEngine.getUsername(m_CurrentUserId);
			textViewCurrentUser.setText(getString(R.string.current_user_is_)
					+ username);
		}

		if (m_ButtonsLocckedDueToImportFromXML
				&& !m_FirstTimeStartingDoNotTryToUpdate) {

			m_ProgressDialog.setCancelable(false);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_ProgressDialog.setTitle("Questions import from initial file");
			m_ProgressDialog.show();

		} else if (!m_FirstTimeStartingDoNotTryToUpdate) {
			m_UpdateManager.updateQuestions(true);
			showUserRegister();
		}

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
		if (m_ButtonsLocckedDueToImportFromXML) {
			lockButtonOnImportFromXMLFile();
		} else {
			releaseButtonsOnImportFromXMLFile();
		}

	}

	private void initializeVariables() {
		//

		m_CurrentUserId = -1;

		m_ProgressDialog = new ProgressDialog(this);
		m_TrivaDbEngine = new TriviaDbEngine(this);
		m_UpdateManager = new UpdateManager(this);
		m_UpdateManager.setCategoriesListener(this);
		m_UpdateManager.setQuestionsListener(this);

		m_RegisterLater = false;
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

		textViewCurrentUser = (TextView) findViewById(R.id.textViewCurrentUser);

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
			m_FirstTimeStartingDoNotTryToUpdate = false;
			m_CurrentUserId = resultCode;
			changeToDefault();
			break;
		case REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER:
			m_CurrentUserId = resultCode;
			changeToDefault();
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

	private void changeToDefault() {

		//
		if (m_CurrentUserId > 0) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Defaut user");
			alert.setMessage("Use user "
					+ m_TrivaDbEngine.getUsername(m_CurrentUserId)
					+ " as default?");
			alert.setPositiveButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//
							m_SharedPreferences
									.edit()
									.putString("defaultUserId",
											String.valueOf(m_CurrentUserId))
									.commit();
						}
					});
			alert.setNegativeButton("No",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//

						}
					});
			alert.show();
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
							m_RegisterLater = true;
						}
					});
			if (!m_RegisterLater) {
				alert.show();
			}
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

	@Override
	public void onCategoriesUpdated(int i_UpdateFrom) {
		//
		switch (i_UpdateFrom) {
		case TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET:
			Toast.makeText(ActivityTrivia.this,
					"Finished updating categories from internet",
					Toast.LENGTH_LONG).show();
			break;
		case TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE:

			Toast.makeText(ActivityTrivia.this,
					"Finished updating categories from initial file",
					Toast.LENGTH_LONG).show();
			m_ButtonsLocckedDueToImportFromXML = false;
			m_ProgressDialog.dismiss();
			break;

		default:
			break;
		}

	}

	@Override
	public void onQuestionsCorrectRatioSent() {
		//
		Toast.makeText(ActivityTrivia.this,
				"Thank you for making Social Trivia better! :)",
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onQuestionsUpdated(int i_UpdateFrom) {
		// //
		switch (i_UpdateFrom) {
		case TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET:
			Toast.makeText(ActivityTrivia.this,
					"Finished updating questions from internet",
					Toast.LENGTH_LONG).show();
			break;
		case TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE:
			// starting to update the categories
			releaseButtonsOnImportFromXMLFile();
			Toast.makeText(ActivityTrivia.this,
					"Finished updating questions from initial file",
					Toast.LENGTH_LONG).show();
			Toast.makeText(this,
					"Starting importing categories from initial file",
					Toast.LENGTH_LONG).show();
			// Log.v(TAG, "Starting import categories from xml");
			m_UpdateManager.importCategoriesFromXml();

			break;

		default:
			break;
		}

	}

	@Override
	public void updateQuestionProgress(int i_Progress, int i_Max) {
		//
		m_ProgressDialog.setMax(i_Max);
		m_ProgressDialog.setProgress(i_Progress);
	}

}