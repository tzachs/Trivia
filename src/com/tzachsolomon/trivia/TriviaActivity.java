package com.tzachsolomon.trivia;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.ContentValues;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TriviaActivity extends Activity implements OnClickListener {
	private Button buttonNewGame;

	private TriviaDbEngine m_TriviaDb;
	private JSONHandler m_JSONHandler;
	private Button buttonUpdateDatabase;

	private SharedPreferences m_SharedPreferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		PreferenceManager.setDefaultValues(this, R.xml.prefs,true);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		checkIfFirstTime();
	}

	private void checkIfFirstTime() {
		//
		if (m_TriviaDb.isEmpty()) {
			updateDatabaseFromInternetDisplauQuestion("It seems this is the first time you are running Trivia.\n"
					+ "You must once update the datatable.\n"
					+ "Update Now? (This requires internet connection)");
		}

	}

	private void updateDatabaseFromInternet() {
		ContentValues[] values;
		String updateMethod;
		String serverUrl = m_SharedPreferences.getString(
				"editTextPreferencePrimaryServerIP", "");

		if (serverUrl.contentEquals("")) {
			Toast.makeText(this, "Error finding server URL, please check preferneces", Toast.LENGTH_SHORT).show();

		} else {

			m_TriviaDb.deleteQuestions();

			m_JSONHandler.setServerUrl(serverUrl);

			// getting the desired update method
			updateMethod = m_SharedPreferences.getString(
					"listPreferenceUpdateMethod", "async");

			if (updateMethod.contentEquals("sync")) {
				values = m_JSONHandler.updateFromInternetSync();
				if (values == null) {
					Toast.makeText(this, "Error updating from server",
							Toast.LENGTH_SHORT).show();
				} else {
					m_TriviaDb.updateFromInternet(values);
				}

			} else {
				m_JSONHandler.updateFromInternetAsync();

			}
		}

	}

	private void updateDatabaseFromInternetDisplauQuestion(String i_Message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(i_Message);
		builder.setCancelable(false);
		builder.setPositiveButton("Update",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//

						updateDatabaseFromInternet();

					}
				});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//

			}
		});

		builder.show();

	}

	private void initializeVariables() {
		//
		m_TriviaDb = new TriviaDbEngine(this);

		m_JSONHandler = new JSONHandler(TriviaActivity.this);

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonNewGame = (Button) findViewById(R.id.buttonNewGame);
		buttonUpdateDatabase = (Button) findViewById(R.id.buttonUpdateDatabase);

		buttonNewGame.setOnClickListener(this);
		buttonUpdateDatabase.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonNewGame:
			buttonNewGame_Clicked();
			break;
		case R.id.buttonUpdateDatabase:
			buttonUpdateDatabase_Clicked();
		}

	}

	private void buttonUpdateDatabase_Clicked() {
		//
		updateDatabaseFromInternetDisplauQuestion("Update Now? (This requires internet connection)");

	}

	private void buttonNewGame_Clicked() {
		//
		Intent intent = new Intent(this, Game.class);
		startActivity(intent);

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
		Intent pref = new Intent(TriviaActivity.this, Prefs.class);

		startActivity(pref);

	}

	private void menuItemAbout_Clicked() {
		// TODO Auto-generated method stub

	}

}