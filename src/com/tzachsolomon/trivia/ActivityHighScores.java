package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ActivityHighScores extends Activity implements OnClickListener {
	
	private Button buttonCloseHighScore;
	private TableLayout table;
	private TriviaDbEngine m_TriviaDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_scores);
		
		initializeVariables();
	}

	private void initializeVariables() {
		
		//
		
		m_TriviaDb = new TriviaDbEngine(this);
		
		
		
		table = (TableLayout)findViewById(R.id.tableLayoutHighScores);
		
		fillTableRows();
		
		
		
		buttonCloseHighScore = (Button)findViewById(R.id.buttonCloseHighScore);
		
		
		buttonCloseHighScore.setOnClickListener(this);
		
	}

	private void fillTableRows() {
		// 
		ContentValues[] rows = 	m_TriviaDb.getGameScores();
		TableRow rowView;
		
		for (ContentValues row : rows) {
			rowView = new TableRow(this);
			TextView user = new TextView(this);
			TextView score = new TextView(this);
			TextView type = new TextView(this);
			TextView date = new TextView(this);
			
			user.setGravity(Gravity.CENTER);
			score.setGravity(Gravity.CENTER);
			type.setGravity(Gravity.CENTER);
			date.setGravity(Gravity.CENTER);
			
			user.setText(row.getAsString(TriviaDbEngine.KEY_COL_USER_ID));
			score.setText(row.getAsString(TriviaDbEngine.KEY_COL_USER_ID));
			type.setText(row.getAsString(TriviaDbEngine.KEY_COL_GAME_TYPE));
			date.setText(row.getAsString(TriviaDbEngine.KEY_COL_GAME_ID));
			
			rowView.addView(user);
			rowView.addView(score);
			rowView.addView(type);
			rowView.addView(date);
			
			table.addView(rowView);
			
		}
		
	}

	@Override
	public void onClick(View v) {
		// 
		switch (v.getId()){
		case R.id.buttonCloseHighScore:
			finish();
			break;
		}
		
	}

}
