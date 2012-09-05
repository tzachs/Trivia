
package com.tzachsolomon.trivia;

import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;


import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ActivityHighScores extends Activity {

	private TableLayout table;
	private TriviaDbEngine m_TriviaDb;
	private SparseArray<String> m_Users;
	private SparseArray<String> m_GameTypes;
	private JSONHandler m_JSONHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		// TODO: 1. send local scores to database
		// TODO: 2. fetch public scores from database and display them
		// TODO: 3. divide by game type / all game types combined
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_scores);

		initializeVariables();
		sendLocalScores();
	}

	private void sendLocalScores() {
		// 
		// TODO: 1. check if there are local scores that were not uploaded
		
		
	}

	private void initializeVariables() {

		//
		m_JSONHandler = new JSONHandler(this);
		
		
		m_TriviaDb = new TriviaDbEngine(this);

		m_Users = m_TriviaDb.getUserNames();
		
		m_GameTypes = new SparseArray<String>();

		// 
		m_GameTypes.put(ActivityGame.GAMETYPE_ALL_QUESTIONS, getString(R.string.buttonNewGameAllQuestionsText));
		m_GameTypes.put(ActivityGame.GAMETYPE_CATEGORIES, getString(R.string.buttonNewGameCategoriesText));
		m_GameTypes.put(ActivityGame.GAMETYPE_LEVELS, getString(R.string.buttonNewGameSimpleText));

		table = (TableLayout) findViewById(R.id.tableLayoutHighScores);

		fillTableRows();

	}

	private void fillTableRows() {
		//
		ContentValues[] rows = m_TriviaDb.getGameScores();
		TableRow rowView;
		int rowCounter = 1;

		for (ContentValues row : rows) {

			rowView = convertRowToTableRowView(rowCounter, row);
			table.addView(rowView);
			rowCounter++;

		}

	}

	private TableRow convertRowToTableRowView(int i_RowCounter,
			ContentValues row) {
		TableRow rowView;
		int gameType;
		int userId;
		Date dateFormat;
		rowView = new TableRow(this);
		TextView user = new TextView(this);
		TextView score = new TextView(this);
		TextView type = new TextView(this);
		TextView date = new TextView(this);
		TextView rowCounter = new TextView(this);

		user.setGravity(Gravity.CENTER);
		score.setGravity(Gravity.CENTER);
		type.setGravity(Gravity.CENTER);
		date.setGravity(Gravity.CENTER);

		gameType = row.getAsInteger(TriviaDbEngine.KEY_COL_GAME_TYPE);
		userId = row.getAsInteger(TriviaDbEngine.KEY_COL_USER_ID);

		user.setText(getUsernameByUserId(userId));
		type.setText(getGameTypeStringByGameTypeId(gameType));
		score.setText(row.getAsString(TriviaDbEngine.KEY_COL_GAME_SCORE));
		dateFormat = new Date(row.getAsLong(TriviaDbEngine.KEY_COL_GAME_ID));
		date.setText(dateFormat.toLocaleString());

		rowCounter.setText(String.valueOf(i_RowCounter));

		rowView.addView(rowCounter);
		rowView.addView(user);
		rowView.addView(score);
		rowView.addView(type);
		rowView.addView(date);
		
		return rowView;
	}

	private CharSequence getGameTypeStringByGameTypeId(int gameType) {
		//
		String ret =m_GameTypes.get(gameType); 

		if (ret != null) {
			return ret;
		} else {
			return "";
		}

	}

	private CharSequence getUsernameByUserId(int userId) {
		//
		String ret =m_Users.get(userId); 
		if (ret != null) {
			return ret;
		} else {
			return "Anonymous";
		}

	}

}
