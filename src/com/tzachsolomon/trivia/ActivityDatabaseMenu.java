package com.tzachsolomon.trivia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityDatabaseMenu extends Activity implements OnClickListener {

	public static final String TAG = ActivityDatabaseMenu.class.getSimpleName();

	private Button buttonDeleteDatabase;
	private Button buttonUpdateDatabaseQuestions;

	private TriviaDbEngine m_TriviaDb;

	private UpdateManager m_UpdateManager;

	private Button buttonUpdateDatabaseCategories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database);

		initializeVariables();

	}

	private void initializeVariables() {
		//

		m_UpdateManager = new UpdateManager(this);
		m_TriviaDb = new TriviaDbEngine(ActivityDatabaseMenu.this);

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonUpdateDatabaseQuestions = (Button) findViewById(R.id.buttonUpdateDatabaseQuestions);
		buttonUpdateDatabaseCategories =  (Button)findViewById(R.id.buttonUpdateDatabaseCategories);
		
		buttonDeleteDatabase = (Button) findViewById(R.id.buttonDeleteDatabase);

		buttonUpdateDatabaseQuestions.setOnClickListener(this);
		buttonUpdateDatabaseCategories.setOnClickListener(this);
		buttonDeleteDatabase.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		
		case R.id.buttonUpdateDatabaseCategories:
			buttonUpdateDatabaseCategories_Clicked();
			break;
			
		case R.id.buttonUpdateDatabaseQuestions:
			buttonUpdateDatabase_Clicked();

			break;

		case R.id.buttonDeleteDatabase:
			buttonDeleteDatabase_Clicked();
			break;

		default:
			break;
		}

	}

	private void buttonUpdateDatabaseCategories_Clicked() {
		// 
		m_UpdateManager.updateCategories();
		
	}

	private void buttonUpdateDatabase_Clicked() {
		// checking if to upload user correct wrong statistics before
		m_UpdateManager.updateQuestions();

	}

	private void buttonDeleteDatabase_Clicked() {
		//
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(getString(R.string.delete_database_));
		dialog.setMessage(getString(R.string.are_you_sure_you_want_to_delete_the_database_));
		dialog.setCancelable(false);
		dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//
				m_TriviaDb.deleteQuestions();
			}
		});
		dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//
				
			}
		});
		dialog.show();

	}

}
