package com.tzachsolomon.trivia;


import static com.tzachsolomon.trivia.ClassCommonUtils.*;
import java.util.Date;

import com.tzachsolomon.trivia.JSONHandler.ScoreListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityHighScores extends Activity implements ScoreListener,
		OnClickListener {

	private TableLayout table;
	private TriviaDbEngine mTriviaDb;

	private SparseArray<String> mGameTypes;
	private JSONHandler mJSONHandler;
	private Button buttonGameTypeAll;
	private Button buttonGameTypeCategory;
	private Button buttonGameTypeLevel;
	private Button buttonGameTypeSoviet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_scores);

		initializeVariables();
		uploadLocalScores();
		buttonGameTypeAll_Clicked();

	}

	public class AsyncTaskDownloadScoresAndDisplay extends
			AsyncTask<Integer, Integer, ContentValues[]> {

		ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			//
			mProgressDialog = new ProgressDialog(ActivityHighScores.this);
			mProgressDialog.setTitle(getString(R.string.downloding_game_scores));
			mProgressDialog.setMessage(getString(R.string.downloading_game_scores_from_server));
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(ContentValues[] result) {
			//

			int row = 1;
			TableRow tableRow;

			mProgressDialog.dismiss();

			if (result != null) {

				for (ContentValues gameScore : result) {
					tableRow = convertRowToTableRowView(row, gameScore);
					table.addView(tableRow);
					row++;

				}
			} else {
				Toast.makeText(ActivityHighScores.this,
						"Error downloading scores from server",
						Toast.LENGTH_LONG).show();

			}

		}

		@Override
		protected ContentValues[] doInBackground(Integer... params) {
			//
			return mJSONHandler.getGameScores(params[0]);

		}

	}

	private void downloadScoresAndDisplay(int gameType) {
		//

		AsyncTaskDownloadScoresAndDisplay a = new AsyncTaskDownloadScoresAndDisplay();

		clearTableView();
		a.execute(gameType);

	}

	public class AsyncTaskUploadLocalScores extends
			AsyncTask<ContentValues, Integer, Void> {

		ProgressDialog m_ProgressDialog;

		public AsyncTaskUploadLocalScores() {
			m_ProgressDialog = new ProgressDialog(ActivityHighScores.this);
		}

		@Override
		protected void onPostExecute(Void result) {
			//
			super.onPostExecute(result);

			m_ProgressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {

			m_ProgressDialog.setCancelable(false);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_ProgressDialog.setTitle("Uploading local scores");
			m_ProgressDialog.setMax(100);

			m_ProgressDialog.show();

		}

		@Override
		protected Void doInBackground(ContentValues... params) {
			//

			int i = 0;
			m_ProgressDialog.setMax(params.length);
			// sending all local scores, will delete every uploaded score on the
			// call back
			// function
			for (ContentValues row : params) {

				mJSONHandler.uploadScoreToDatabase(
						row.getAsString(TriviaDbEngine.KEY_COL_USER_ID),
						row.getAsString(TriviaDbEngine.KEY_COL_GAME_TYPE),
						row.getAsString(TriviaDbEngine.KEY_COL_GAME_SCORE),
						row.getAsString(TriviaDbEngine.KEY_COL_GAME_TIME),
						row.getAsInteger(TriviaDbEngine.KEY_ROWID)

				);
				i++;
				publishProgress(i);

			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//
			super.onProgressUpdate(values);
			m_ProgressDialog.setProgress(values[0]);
		}

	}

	private void uploadLocalScores() {
		//
		AsyncTaskUploadLocalScores a = new AsyncTaskUploadLocalScores();

		a.execute(mTriviaDb.getGameScores());

	}

	private void initializeVariables() {

		//
		mJSONHandler = new JSONHandler(this);

		mJSONHandler.setScoreUpdateListener(this);

		mTriviaDb = new TriviaDbEngine(this);

		mGameTypes = new SparseArray<String>();

		//
		mGameTypes.put(GAMETYPE_ALL_QUESTIONS,
				getString(R.string.soviet));
		mGameTypes.put(GAMETYPE_CATEGORIES,
				getString(R.string.category));
		mGameTypes
				.put(GAMETYPE_LEVELS, getString(R.string.levels));

		table = (TableLayout) findViewById(R.id.tableLayoutHighScores);

		initializeButtons();

	}

	private void initializeButtons() {
		//
		buttonGameTypeAll = (Button) findViewById(R.id.buttonGameTypeAll);
		buttonGameTypeCategory = (Button) findViewById(R.id.buttonGameTypeCategory);
		buttonGameTypeLevel = (Button) findViewById(R.id.buttonGameTypeLevel);
		buttonGameTypeSoviet = (Button) findViewById(R.id.buttonGameTypeSoviet);

		buttonGameTypeAll.setOnClickListener(this);
		buttonGameTypeCategory.setOnClickListener(this);
		buttonGameTypeLevel.setOnClickListener(this);
		buttonGameTypeSoviet.setOnClickListener(this);

	}

	private void clearTableView() {
		table.removeViews(1, table.getChildCount() - 1);
	}

	private TableRow convertRowToTableRowView(int i_RowCounter,
			ContentValues row) {

		TableRow rowView;
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

		type.setText(getGameTypeStringByGameTypeId(row
				.getAsInteger(TriviaDbEngine.KEY_COL_GAME_TYPE)));
		user.setText(row.getAsString(TriviaDbEngine.KEY_COL_USERNAME));
		score.setText(row.getAsString(TriviaDbEngine.KEY_COL_GAME_SCORE));
		dateFormat = new Date(row.getAsLong(TriviaDbEngine.KEY_COL_GAME_TIME));
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
		String ret = mGameTypes.get(gameType);

		if (ret != null) {
			return ret;
		} else {
			return "";
		}

	}

	@Override
	public void onScoreAdded(int i_Result) {
		//

	}

	@Override
	public void deleteScoreFromDatabase(int rowInDatabase) {
		//
		// for debug
		mTriviaDb.deleteScoreFromDatabase(rowInDatabase);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonGameTypeAll:
			buttonGameTypeAll_Clicked();
			break;
		case R.id.buttonGameTypeCategory:
			buttonGameTypeCategory_Clicked();
			break;
		case R.id.buttonGameTypeLevel:
			buttonGameTypeLevel_Clicked();
			break;
		case R.id.buttonGameTypeSoviet:
			buttonGameTypeSoviet_Clicked();
			break;

		default:
			break;
		}

	}

	private void buttonGameTypeAll_Clicked() {
		//
		downloadScoresAndDisplay(0);

	}

	private void buttonGameTypeCategory_Clicked() {
		//
		downloadScoresAndDisplay(GAMETYPE_CATEGORIES);

	}

	private void buttonGameTypeLevel_Clicked() {
		//
		downloadScoresAndDisplay(GAMETYPE_LEVELS);

	}

	private void buttonGameTypeSoviet_Clicked() {
		//
		downloadScoresAndDisplay(GAMETYPE_ALL_QUESTIONS);

	}

}
