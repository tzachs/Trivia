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
	private Button buttonUpdateDatabase;

	private TriviaDbEngine m_TriviaDb;

	private UpdateManager m_UpdateManager;

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
		buttonUpdateDatabase = (Button) findViewById(R.id.buttonUpdateDatabase);
		buttonDeleteDatabase = (Button) findViewById(R.id.buttonDeleteDatabase);

		buttonUpdateDatabase.setOnClickListener(this);
		buttonDeleteDatabase.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonUpdateDatabase:
			buttonUpdateDatabase_Clicked();

			break;

		case R.id.buttonDeleteDatabase:
			buttonDeleteDatabase_Clicked();
			break;

		default:
			break;
		}

	}

	private void buttonUpdateDatabase_Clicked() {
		// checking if to upload user correct wrong statistics before
		m_UpdateManager.updateNow();

	}

	private void buttonDeleteDatabase_Clicked() {
		//
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle("Delete database?");
		dialog.setMessage("Are you sure you want to delete the database?");
		dialog.setCancelable(false);
		dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//
				m_TriviaDb.deleteQuestions();
			}
		});
		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//
				
			}
		});
		dialog.show();

	}

}
