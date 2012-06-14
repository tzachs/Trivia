package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class DatabaseMenuActivity extends Activity implements OnClickListener {

	public static final String TAG = DatabaseMenuActivity.class.getSimpleName();

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
		m_TriviaDb = new TriviaDbEngine(DatabaseMenuActivity.this);

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
		m_TriviaDb.deleteQuestions();

	}

	

	
}
