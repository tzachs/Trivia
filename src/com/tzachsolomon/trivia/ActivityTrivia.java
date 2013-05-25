package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;
import java.util.Locale;

import com.tzachsolomon.trivia.UpdateManager.CategoriesListener;
import com.tzachsolomon.trivia.UpdateManager.QuestionsListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityTrivia extends Activity implements OnClickListener,
		CategoriesListener, QuestionsListener {

	// TODO: create service to update the database daily
	// TODO: activity menu for tablet
	// TODO: check if needed to update from XML file using last update
	// TODO: check user authentication key expired
	// TODO: content provider for DB
	// TODO: separate sounds

	public static final String TAG = ActivityTrivia.class.getSimpleName();



	private StringParser mStringParser;

	private Button buttonNewGameAllQuestions;
	private Button buttonManageDatabase;
	private Button buttonPreferences;
	private Button buttonNewGameSimple;
	private Button buttonUpdateDatabase;
	private Button buttonNewGameCategories;
	private Button buttonGameScores;
	private Button buttonManageUsers;
	private Button buttonSuggestQuestion;
	private Button buttonSendSuggesion;

	private SharedPreferences mSharedPreferences;

	private UpdateManager mUpdateManager;

	private int mCurrentUserId;

	private boolean mFirstTimeStartingDoNotTryToUpdate;
	private TriviaDbEngine mTrivaDbEngine;
	private TextView textViewCurrentUser;

	private boolean mButtonsLockedDueToImportFromXML;
	protected boolean mLaterRegisterUser;

	private ProgressDialog mProgressDialog;

	private SlidingDrawer slidingDrawer;

	private boolean mUpdateQuestionsLater;
	private boolean mUpdateCategoriesLater;

	private int mLaterRegisterUserCounter;
	private int mLaterUpdateQuestionsCounter;
	private AlertDialog.Builder mAlertDialogBuilderRegisterUser;
	private AlertDialog.Builder mAlertDialogBuilderUpdate;
	private Dialog mDialogRegisterUser;
	private Dialog mDialogUpdate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trivia);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();
		initializeButtons();
		initializeDialogs();
		checkIfNeedToShowFirstTimeMessageOrConfiguration();
	}

	private void initializeDialogs() {
		//
		mAlertDialogBuilderRegisterUser = new AlertDialog.Builder(
				ActivityTrivia.this);

		mAlertDialogBuilderRegisterUser
				.setTitle(getString(R.string.register_user));
		mAlertDialogBuilderRegisterUser
				.setMessage(getString(R.string.register_a_user_));
		mAlertDialogBuilderRegisterUser.setPositiveButton(
				getString(R.string.yes), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						buttonManagerUsers_Clicked();

					}

				});
		mAlertDialogBuilderRegisterUser.setNegativeButton(
				getString(R.string.later),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						Toast.makeText(
								ActivityTrivia.this,
								getString(R.string.registering_a_user_gives_you_the_ability_to_publish_scores_play_against_other_players_etc),
								Toast.LENGTH_LONG).show();
						mLaterRegisterUser = true;

					}
				});

		mAlertDialogBuilderUpdate = new AlertDialog.Builder(ActivityTrivia.this);
		mAlertDialogBuilderUpdate.setCancelable(false);

		mAlertDialogBuilderUpdate.setPositiveButton(getString(R.string.update),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (mUpdateManager.getUpdateType()) {
						case JSONHandler.TYPE_UPDATE_CATEGORIES:
							mUpdateManager.updateCategoriesNow();
							break;
						case JSONHandler.TYPE_UPDATE_QUESTIONS:
							mUpdateManager.updateQuestionsNow();
							break;
						}

					}
				});
		mAlertDialogBuilderUpdate.setNegativeButton(getString(R.string.later),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						switch (mUpdateManager.getUpdateType()) {
						case JSONHandler.TYPE_UPDATE_CATEGORIES:
							mUpdateCategoriesLater = true;
							break;
						case JSONHandler.TYPE_UPDATE_QUESTIONS:
							mUpdateQuestionsLater = true;
							break;
						}
					}
				});

	}

	private void checkIfNeedToShowFirstTimeMessageOrConfiguration() {
		//
		PackageInfo packageInfo = null;
		String whatsNewVersion = mSharedPreferences.getString("showWhatsNew",
				"1.0");
		String showConfigVersion = mSharedPreferences.getString(
				"showConfigWizard", "1");

		mFirstTimeStartingDoNotTryToUpdate = false;

		try {
			packageInfo = getPackageManager().getPackageInfo(
					"com.tzachsolomon.trivia", PackageManager.GET_META_DATA);
			

			if (!packageInfo.versionName.contentEquals(whatsNewVersion)) {
				
				// due to change in web service from amazon to my web server
				if ( packageInfo.versionName.contentEquals("0.14")){
					mSharedPreferences.edit().putString("editTextPreferencePrimaryServerIP", 
							"http://tzachs.no-ip.biz/websrv/trivia/index.php").commit();
				}

				mFirstTimeStartingDoNotTryToUpdate = true;

				// Log.v(TAG, "Starting import questions from XML");
				Toast.makeText(
						this,
						getString(R.string.importing_questions_from_initial_file),
						Toast.LENGTH_LONG).show();
				mButtonsLockedDueToImportFromXML = true;

				mUpdateManager.importQuestionsFromXml();

				showWhatsNew();
				if (showConfigVersion.contentEquals("1")) {
					showWizardSetup();
					mSharedPreferences.edit()
							.putString("showConfigWizard", "0").commit();
				}

				mSharedPreferences.edit()
						.putString("showWhatsNew", packageInfo.versionName)
						.commit();

			} else if (mSharedPreferences.getBoolean(
					"showFirstTimeConfiguration", true)) {
				showWizardSetup();
			}
		} catch (NameNotFoundException e) {
			//
			Log.e(TAG, "Could not get meta data info for Trivia");
		}

	}

	private void sendSuggestion() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "tzach.solomon@gmail.com" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Suggestion for Trivia");

		startActivity(Intent.createChooser(emailIntent,
				getString(R.string.send_suggestion_in_)));

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
		mTrivaDbEngine.openForFirstTime();
		buttonWizardSetup_Clicked();

	}

	@Override
	protected void onStart() {
		//
		super.onStart();

	}

	private void setUser() {
		if (mCurrentUserId > 0) {

			String username = mTrivaDbEngine.getUsername(mCurrentUserId);
			textViewCurrentUser.setText(getString(R.string.current_user_is_)
					+ username);
		}

	}

	@Override
	protected void onResume() {
		//

		super.onResume();

		changeLanguageTo(mSharedPreferences.getString(
				"listPreferenceLanguages", "iw"));

		setUser();

		if (mButtonsLockedDueToImportFromXML
				&& !mFirstTimeStartingDoNotTryToUpdate) {

			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog
					.setTitle(getString(R.string.importing_questions_from_initial_file));
			mProgressDialog.show();

		} else if (!mFirstTimeStartingDoNotTryToUpdate) {
			if (mUpdateQuestionsLater) {
				// checking if user asked to update questions later
				if (mLaterUpdateQuestionsCounter > 0) {
					mLaterUpdateQuestionsCounter--;
				} else {
					mUpdateManager.updateQuestions(true);
				}
			} else {
				mUpdateManager.updateQuestions(true);
			}

			showUserRegister();
		}

	}

	private void changeLanguageTo(String string) {
		//

		Locale locale = new Locale(string);
		Locale.setDefault(locale);
		Log.d(TAG, "Changed language to " + string);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.activity_trivia);

		initializeButtons();
		if (mButtonsLockedDueToImportFromXML) {
			lockButtonOnImportFromXMLFile();
		} else {
			releaseButtonsOnImportFromXMLFile();
		}

	}

	private void initializeVariables() {
		//

		try {
			mCurrentUserId = Integer.valueOf(mSharedPreferences.getString(
					"defaultUserId", "-2"));
		} catch (Exception e) {
			mCurrentUserId = -2;
		}

		mStringParser = new StringParser(mSharedPreferences);

		mProgressDialog = new ProgressDialog(this);
		mTrivaDbEngine = new TriviaDbEngine(this);
		mUpdateManager = new UpdateManager(this);
		mUpdateManager.setCategoriesListener(this);
		mUpdateManager.setQuestionsListener(this);

		slidingDrawer = (SlidingDrawer) findViewById(R.id.sd);

		mLaterRegisterUser = false;
		mLaterRegisterUserCounter = 0;
		mUpdateQuestionsLater = false;
		mLaterUpdateQuestionsCounter = 5;
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
		buttonSuggestQuestion = (Button) findViewById(R.id.buttonSuggestQuestion);
		buttonSendSuggesion = (Button) findViewById(R.id.buttonSendSuggestion);

		buttonNewGameAllQuestions.setOnClickListener(this);
		buttonManageDatabase.setOnClickListener(this);
		buttonPreferences.setOnClickListener(this);
		buttonNewGameSimple.setOnClickListener(this);
		buttonUpdateDatabase.setOnClickListener(this);
		buttonNewGameCategories.setOnClickListener(this);
		buttonManageUsers.setOnClickListener(this);
		buttonGameScores.setOnClickListener(this);
		buttonSuggestQuestion.setOnClickListener(this);
		buttonSendSuggesion.setOnClickListener(this);

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
		case R.id.buttonSendSuggestion:
			sendSuggestion();
			break;

		case R.id.buttonSuggestQuestion:
			buttonSuggestQuestion_Clicked();
			break;

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

	private void buttonSuggestQuestion_Clicked() {
		//
		Intent intent = new Intent(ActivityTrivia.this,
				ActivitySuggestQuestion.class);
		startActivity(intent);

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
			mFirstTimeStartingDoNotTryToUpdate = false;
			mCurrentUserId = resultCode;
			changeToDefault();
			break;
		case REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER:
			mCurrentUserId = resultCode;
			// Log.v(TAG, "m_CurrentUserId --> " + m_CurrentUserId);
			changeToDefault();
			break;
		case REQUEST_CODE_BACK_FROM_PREFERENCES:
			mUpdateManager.updateServerIpFromPreferences();
			break;
		case REQUEST_CODE_START_GAME_CATEGORIES:
			if (data != null) {

				Bundle extras = data.getExtras();

				extras.putInt(EXTRA_GAME_TYPE,
						GAMETYPE_CATEGORIES);
				startNewGame(extras);
			}

			break;

		}
	}

	private void changeToDefault() {

		//
		if (mCurrentUserId > 0) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(getString(R.string.defaut_user));
			alert.setMessage(getString(R.string.use_user_)
					+ mTrivaDbEngine.getUsername(mCurrentUserId)
					+ getString(R.string._as_default_));
			alert.setPositiveButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//
							mSharedPreferences
									.edit()
									.putString("defaultUserId",
											String.valueOf(mCurrentUserId))
									.commit();
						}
					});
			alert.setNegativeButton(getString(R.string.no),
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
		if (mTrivaDbEngine.isUsersEmpty()) {
			if (mLaterRegisterUser) {
				mLaterRegisterUserCounter++;

				// after 5 times showing the screen displaying again the
				// register user
				if (mLaterRegisterUserCounter == 5) {
					mLaterRegisterUserCounter = 0;
					mLaterRegisterUser = false;
				}
			} else {
				if (mDialogRegisterUser == null) {
					mDialogRegisterUser = mAlertDialogBuilderRegisterUser
							.show();
				} else if (!mDialogRegisterUser.isShowing()) {
					mDialogRegisterUser.show();
				}
			}
		}

	}

	private void startNewGame(Bundle extras) {
		//
		Intent intent = new Intent(this, ActivityGame.class);
		intent.putExtra(EXTRA_GAME_USER_ID, mCurrentUserId);
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

		mUpdateManager.updateQuestions(false);

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
		extras.putInt(EXTRA_GAME_TYPE,
				GAMETYPE_LEVELS);
		extras.putInt(EXTRA_GAME_START_LEVEL, 1);

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

		extras.putInt(EXTRA_GAME_TYPE,
				GAMETYPE_ALL_QUESTIONS);
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
			Toast.makeText(
					ActivityTrivia.this,
					getString(R.string.finished_updating_categories_from_internet),
					Toast.LENGTH_LONG).show();
			break;
		case TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE:

			Toast.makeText(
					ActivityTrivia.this,
					getString(R.string.finished_importing_categories_from_initial_file),
					Toast.LENGTH_LONG).show();
			mButtonsLockedDueToImportFromXML = false;
			mProgressDialog.dismiss();

			showUserRegister();

			break;

		default:
			break;
		}

	}

	@Override
	public void onQuestionsCorrectRatioSent() {
		//
		Toast.makeText(ActivityTrivia.this,
				getString(R.string.thank_you_for_making_social_trivia_better_),
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onQuestionsUpdated(int i_UpdateFrom) {
		// //
		switch (i_UpdateFrom) {
		case TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET:
			Toast.makeText(
					ActivityTrivia.this,
					getString(R.string.finished_importing_questions_from_internet),
					Toast.LENGTH_LONG).show();
			break;
		case TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE:
			// starting to update the categories
			releaseButtonsOnImportFromXMLFile();
			Toast.makeText(
					ActivityTrivia.this,
					getString(R.string.finished_importing_questions_from_initial_file),
					Toast.LENGTH_LONG).show();
			Toast.makeText(this,
					getString(R.string.importing_categories_from_initial_file),
					Toast.LENGTH_LONG).show();
			// Log.v(TAG, "Starting import categories from XML");
			mUpdateManager.importCategoriesFromXml();

			break;

		default:
			break;
		}

	}

	@Override
	public void updateQuestionProgress(int i_Progress, int i_Max) {
		//
		mProgressDialog.setMax(i_Max);
		mProgressDialog.setProgress(i_Progress);
	}

	@Override
	public void onUpdateQuestionsPostponed() {
		//
		mUpdateQuestionsLater = true;
		mLaterUpdateQuestionsCounter = 5;

	}

	@Override
	public void onUpdateCategoriesPostponed() {
		//
		mUpdateCategoriesLater = true;

	}

	@Override
	public void onCheckIfQuestionUpdateAvailablePost(Bundle result) {
		//
		boolean silentMode = mUpdateManager.getSilentMode();
		int numberOfNewQuestions = 0;
		int numberOfUpdatedQuestions = 0;

		if (result != null) {
			numberOfNewQuestions = result.getInt("newQuestions");
			numberOfUpdatedQuestions = result.getInt("updatedQuestions");
		}

		if ((numberOfNewQuestions + numberOfUpdatedQuestions) > 0) {
			StringBuilder message = new StringBuilder();

			message.append(getString(R.string.there_are_));
			message.append(numberOfNewQuestions);
			message.append(getString(R.string._new_questions_and_));
			message.append(numberOfUpdatedQuestions - numberOfNewQuestions);
			message.append(getString(R.string._updated_questions));

			mAlertDialogBuilderUpdate.setMessage(mStringParser
					.reverseNumbersInStringHebrew(message.toString()));

			if (mDialogUpdate == null) {
				mDialogUpdate = mAlertDialogBuilderUpdate.show();
			} else if (mDialogUpdate.isShowing() == false) {
				mDialogUpdate.show();
			}

		} else if (silentMode == false) {
			Toast.makeText(ActivityTrivia.this,
					getString(R.string.no_update_available), Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onCheckIfCategoriesUpdateAvailablePost(Bundle result) {

		boolean silentMode = mUpdateManager.getSilentMode();
		int numberOfNewCategories = 0;
		int numberOfUpdatedCategories = 0;

		if (result != null) {
			numberOfNewCategories = result.getInt("newCategories");
			numberOfUpdatedCategories = result.getInt("updatedCategories");
		}

		if ((numberOfNewCategories + numberOfUpdatedCategories) > 0) {
			StringBuilder message = new StringBuilder();

			message.append(R.string.there_are_);
			message.append(numberOfNewCategories);
			message.append(" new categories and ");
			message.append(numberOfNewCategories);
			message.append(" updaed categories");

			mAlertDialogBuilderUpdate.setMessage(mStringParser
					.reverseNumbersInStringHebrew(message.toString()));

			if (mDialogUpdate == null) {
				mDialogUpdate = mAlertDialogBuilderUpdate.show();
			} else if (mDialogUpdate.isShowing() == false) {
				mDialogUpdate.show();
			}

		} else if (silentMode == false) {
			Toast.makeText(ActivityTrivia.this,
					getString(R.string.no_update_available), Toast.LENGTH_SHORT)
					.show();
		}
		//

	}

}
