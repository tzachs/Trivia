package com.tzachsolomon.trivia;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.os.AsyncTask;
import android.util.Log;

public class TriviaDbEngine {

	public static final String TAG = TriviaDbEngine.class.getSimpleName();

	private static final String DATABASE_NAME = "TriviaDb";
	private static final int DATABASE_VERSION = 6;
	private static final String TABLE_QUESTIONS = "tblQuestions";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_QUESTIONID = "colQuestionId";
	public static final String KEY_QUESTION = "colQuestion";
	public static final String KEY_ANSWER1 = "colAnswer1";
	public static final String KEY_ANSWER2 = "colAnswer2";
	public static final String KEY_ANSWER3 = "colAnswer3";
	public static final String KEY_ANSWER4 = "colAnswer4";
	public static final String KEY_ANSWER_INDEX = "colAnswerIndex";
	public static final String KEY_CATEGORY = "colCategory";
	public static final String KEY_SUB_CATEGORY = "colSubCategory";
	public static final String KEY_LANGUAGE = "colLanguage";
	public static final String KEY_CORRECT_WRONG_RATIO = "colCorrectWrongRatio";
	public static final String KEY_CORRECT_USER = "colCorrectUser"; // the
																	// correct
																	// guesses
																	// the user
																	// did of a
																	// question
	public static final String KEY_WRONG_USER = "colWrongUser"; // the wrong
																// guesses the
																// user did of a
																// question

	public static final String KEY_LAST_UPDATE = "colLastUpdate";
	public static final String KEY_ENABLED = "colEnabled";
	public static final String KEY_PLAYED_COUNTER = "colPlayedCounter";

	private DbHelper ourHelper;
	private Context ourContext;
	private SQLiteDatabase ourDatabase;

	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			//
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			//
			createTableQuestions(db);

		}

		private void createTableQuestions(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();

			sb.append("CREATE TABLE ");
			sb.append(TABLE_QUESTIONS);
			sb.append(" (");
			sb.append(KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			sb.append(KEY_QUESTIONID + " INTEGER NOT NULL, ");
			sb.append(KEY_QUESTION + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER1 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER2 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER3 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER4 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER_INDEX + " INTEGER NOT NULL, ");
			sb.append(KEY_CATEGORY + " INTEGER NOT NULL, ");
			sb.append(KEY_CORRECT_WRONG_RATIO + " INTEGER NOT NULL, ");
			sb.append(KEY_CORRECT_USER + " INTEGER NOT NULL, ");
			sb.append(KEY_PLAYED_COUNTER + " INTEGER NOT NULL, ");
			sb.append(KEY_LANGUAGE + " INTEGER NOT NULL, ");
			sb.append(KEY_SUB_CATEGORY + " TEXT, ");
			sb.append(KEY_WRONG_USER + " INTEGER NOT NULL,");
			sb.append(KEY_ENABLED + " BOOLEAN NOT NULL, ");
			sb.append(KEY_LAST_UPDATE + " INTEGER NOT NULL ");
			sb.append(");");

			Log.v(TAG, sb.toString());

			db.execSQL(sb.toString());

			sb.setLength(0);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//
			db.execSQL("DROP TABLE " + TABLE_QUESTIONS);
			createTableQuestions(db);
		}

	}

	public TriviaDbEngine(Context c) {
		ourContext = c;
	}

	private TriviaDbEngine openDbWritable() {
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();

		return this;

	}

	private TriviaDbEngine openDbReadable() {
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getReadableDatabase();

		return this;
	}

	private void closeDb() {
		ourHelper.close();
	}

	public long insertQuestion(String i_QuestionId, String i_Question,
			String i_Answer1, String i_Answer2, String i_Answer3,
			String i_Answer4, int i_AnswerIndex, int i_Category,
			String i_SubCategory, String i_Language,
			double i_CorrectWrongRatio, String i_DateCreated,
			String i_LastUpdate, boolean i_Enabled, long i_CorrectUser,
			long i_WrongUser) {

		long ret;

		this.openDbWritable();

		ContentValues cv = new ContentValues();

		cv.put(KEY_ANSWER1, i_Answer1);
		cv.put(KEY_ANSWER2, i_Answer2);
		cv.put(KEY_ANSWER3, i_Answer3);
		cv.put(KEY_ANSWER4, i_Answer4);
		cv.put(KEY_ANSWER_INDEX, i_AnswerIndex);
		cv.put(KEY_CATEGORY, i_Category);
		cv.put(KEY_CORRECT_WRONG_RATIO, i_CorrectWrongRatio);
		cv.put(KEY_CORRECT_USER, i_CorrectUser);
		cv.put(KEY_ENABLED, i_Enabled);
		cv.put(KEY_LANGUAGE, i_Language);
		cv.put(KEY_LAST_UPDATE, i_LastUpdate);
		cv.put(KEY_QUESTION, i_Question);
		cv.put(KEY_QUESTIONID, i_QuestionId);
		cv.put(KEY_SUB_CATEGORY, i_SubCategory);

		cv.put(KEY_WRONG_USER, i_WrongUser);

		ret = ourDatabase.insert(TABLE_QUESTIONS, null, cv);

		this.closeDb();

		return ret;
	}

	public ArrayList<Question> getEnabledQuestions() {
		//
		ContentValues map;
		String[] columns = { KEY_ANSWER1, KEY_ANSWER2, KEY_ANSWER3,
				KEY_ANSWER4, KEY_ANSWER_INDEX, KEY_CATEGORY,
				KEY_CORRECT_WRONG_RATIO, KEY_CORRECT_USER, KEY_ENABLED,
				KEY_LANGUAGE, KEY_LAST_UPDATE, KEY_QUESTION, KEY_QUESTIONID,
				KEY_ROWID, KEY_SUB_CATEGORY, KEY_WRONG_USER, KEY_PLAYED_COUNTER};

		Cursor cursor;

		ArrayList<Question> ret;
		this.openDbReadable();

		map = new ContentValues();
		cursor = ourDatabase.query(TABLE_QUESTIONS, columns,
				KEY_ENABLED + "=1", null, null, null, null);

		ret = new ArrayList<Question>();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			DatabaseUtils.cursorRowToContentValues(cursor, map);

			ret.add(new Question(map));

		}

		cursor.close();

		this.closeDb();

		return ret;
	}

	public ArrayList<Question> getQuestionByLevel(int i_Level,
			boolean i_SortByNewQuestionsFirst) {
		//

		StringBuilder orderBy = new StringBuilder();
		ContentValues map;
		String sum = "SUM(" + KEY_CORRECT_USER + " + " + KEY_WRONG_USER
				+ ") as sumUserCorrectWrong";
		String[] columns = { sum, KEY_ANSWER1, KEY_ANSWER2, KEY_ANSWER3,
				KEY_ANSWER4, KEY_ANSWER_INDEX, KEY_CATEGORY,
				KEY_CORRECT_WRONG_RATIO, KEY_CORRECT_USER, KEY_ENABLED,
				KEY_LANGUAGE, KEY_LAST_UPDATE, KEY_QUESTION, KEY_QUESTIONID,
				KEY_ROWID, KEY_SUB_CATEGORY, KEY_WRONG_USER, KEY_PLAYED_COUNTER};

		Cursor cursor;
		double i_MaxLevel = ((double) i_Level + 0.5) / 10;
		double i_MinLevel = i_MaxLevel - 0.1;

		// this correction is done since the ratio is in double from 0 - 1 and
		// the level are in integer from 1 - 10
		// in order to convert ratio to level we do plus 0.5 and floor the
		// answer meaning:
		// level 1 is [0,0.15)
		// level 2 is [0.15,0.25)
		// level 10 is [0.95,1]
		if (i_Level == 1) {
			i_MinLevel = 0;
		}

		String where = KEY_ENABLED + "=1 AND " + KEY_CORRECT_WRONG_RATIO
				+ " <  " + i_MaxLevel + " AND " + KEY_CORRECT_WRONG_RATIO
				+ " >= " + i_MinLevel;

		ArrayList<Question> ret;

		// ordering by
		// the category
		if (i_SortByNewQuestionsFirst) {
			orderBy.append(KEY_PLAYED_COUNTER);
		} else {
			orderBy.append(KEY_CATEGORY);
		}

		// the least answered questions by the user (meaning the user didn't
		// already answer these questions)
		// notice this is cleared every update

		this.openDbReadable();

		map = new ContentValues();
		cursor = ourDatabase.query(TABLE_QUESTIONS, columns, where, null,
				KEY_QUESTIONID, null, orderBy.toString());

		orderBy.setLength(0);

		ret = new ArrayList<Question>();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			DatabaseUtils.cursorRowToContentValues(cursor, map);

			ret.add(new Question(map));

		}

		cursor.close();

		this.closeDb();

		return ret;

	}



	public boolean isEmpty() {
		//
		boolean ret = true;
		// the column KEY_ANSWER1 doesn't matter, Just need to check if there
		// are any rows
		// any other column could be chosen
		String[] columns = { KEY_ANSWER1 };

		this.openDbReadable();
		Cursor cursor = ourDatabase.query(TABLE_QUESTIONS, columns, null, null,
				null, null, null);
		if (cursor.getCount() > 0) {
			ret = false;
		}

		cursor.close();

		this.closeDb();

		return ret;
	}

	public int deleteQuestions() {
		//
		int ret;
		this.openDbWritable();
		ret = ourDatabase.delete(TABLE_QUESTIONS, null, null);
		this.closeDb();

		return ret;

	}
	
	public void incPlayedCounter (String i_QuestionId){
		StringBuilder sb = new StringBuilder();

		sb.append("UPDATE ");
		sb.append(TABLE_QUESTIONS);
		sb.append(" SET ");
		sb.append(KEY_PLAYED_COUNTER);
		sb.append(" = ");
		sb.append(KEY_PLAYED_COUNTER);
		sb.append(" + 1 WHERE ");
		sb.append(KEY_QUESTIONID);
		sb.append(" = '");
		sb.append(i_QuestionId);
		sb.append("'");

		this.openDbWritable();
		
		ourDatabase.execSQL(sb.toString());

		this.closeDb();

		sb.setLength(0);
		
		
	}

	public void incUserCorrectCounter(String i_QuestionId) {
		//
		StringBuilder sb = new StringBuilder();

		sb.append("UPDATE ");
		sb.append(TABLE_QUESTIONS);
		sb.append(" SET ");
		sb.append(KEY_CORRECT_USER);
		sb.append(" = ");
		sb.append(KEY_CORRECT_USER);
		sb.append(" + 1 WHERE ");
		sb.append(KEY_QUESTIONID);
		sb.append(" = '");
		sb.append(i_QuestionId);
		sb.append("'");

		this.openDbWritable();

		
		ourDatabase.execSQL(sb.toString());

		this.closeDb();

		sb.setLength(0);
		
		incPlayedCounter(i_QuestionId);
	}

	public void incUserWrongCounter(String i_QuestionId) {
		//
		StringBuilder sb = new StringBuilder();

		sb.append("UPDATE ");
		sb.append(TABLE_QUESTIONS);
		sb.append(" SET ");
		sb.append(KEY_WRONG_USER);
		sb.append(" = ");
		sb.append(KEY_WRONG_USER);
		sb.append(" + 1 WHERE ");
		sb.append(KEY_QUESTIONID);
		sb.append(" = '");
		sb.append(i_QuestionId);
		sb.append("'");

		this.openDbWritable();

		Log.v(TAG, "Executing " + sb.toString());
		ourDatabase.execSQL(sb.toString());

		this.closeDb();

		sb.setLength(0);
		
		incPlayedCounter(i_QuestionId);

	}

	private boolean isQuestionExist(String i_QuestionId) {
		//
		boolean ret = false;
		String query = String.format("SELECT 1 FROM %1$s WHERE %2$s =%3$s",
				TABLE_QUESTIONS, KEY_QUESTIONID, i_QuestionId);

		Log.v(TAG, "isQuestionExist " + query);

		Cursor cursor = ourDatabase.rawQuery(query, null);

		ret = (cursor.getCount() > 0);
		cursor.close();

		return ret;
	}

	public ContentValues[] getWrongCorrectStat() {
		//
		ContentValues[] ret = null;
		String[] columns = { KEY_CORRECT_USER, KEY_QUESTIONID, KEY_WRONG_USER };
		Cursor cursor;
		int i;

		this.openDbReadable();

		cursor = ourDatabase.query(TABLE_QUESTIONS, columns, KEY_CORRECT_USER
				+ ">0 OR " + KEY_WRONG_USER + ">0", null, null, null, null);

		ret = new ContentValues[cursor.getCount()];

		i = 0;

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			ret[i] = new ContentValues();
			DatabaseUtils.cursorRowToContentValues(cursor, ret[i]);

			i++;

		}

		cursor.close();

		this.closeDb();

		return ret;
	}

	public void updateFromInternetAsync(ContentValues[] values) {
		//
		new UpdateFromInternetAsyncTask().execute(values);

	}

	public class UpdateFromInternetAsyncTask extends
			AsyncTask<ContentValues, Integer, Void> {

		private ProgressDialog m_ProgressDialog;

		@Override
		protected void onPreExecute() {
			//
			m_ProgressDialog = new ProgressDialog(ourContext);
			m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			m_ProgressDialog.setTitle("Inserting questions to DB");
			m_ProgressDialog.show();

		}

		@Override
		protected void onPostExecute(Void result) {
			//
			m_ProgressDialog.dismiss();

		}

		@Override
		protected Void doInBackground(ContentValues... params) {
			//
			ourHelper = new DbHelper(ourContext);
			ourDatabase = ourHelper.getWritableDatabase();

			if (params != null) {

				int i = 0;

				m_ProgressDialog.setMax(params.length);

				for (ContentValues cv : params) {

					try {

						// if the questions exits then it only updates
						String questionId = cv.getAsString(KEY_QUESTIONID);
						if (isQuestionExist(questionId)) {
							ourDatabase.update(TABLE_QUESTIONS, cv,
									KEY_QUESTIONID + "=?",
									new String[] { questionId });
						} else {
							// only if its a new question then insert the wrong
							// correct
							// user statistics to the database
							// this isn't insert in update process since this is
							// taken
							// care in function uploadCorrectWrong
							cv.put(TriviaDbEngine.KEY_CORRECT_USER, 0);
							cv.put(TriviaDbEngine.KEY_WRONG_USER, 0);
							cv.put(TriviaDbEngine.KEY_PLAYED_COUNTER, 0);
							// Log.v(TAG,
							// cv.getAsString(TriviaDbEngine.KEY_WRONG_USER));
							ourDatabase.insert(TABLE_QUESTIONS, null, cv);
						}

					} catch (Exception e) {
						String message = e.getMessage().toString();
						if (message != null) {
							Log.e(TAG, message);
						}

					}

					i++;
					publishProgress(i);
				}
			} else {
				Log.e(TAG, "params are null, database wasn't updated");
			}

			ourHelper.close();
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//
			m_ProgressDialog.setProgress(values[0]);
		}

	}

	public void clearUserCorrectWrongStat(String i_QuestionId) {
		//
		this.openDbWritable();
		ContentValues values = new ContentValues();
		values.put(KEY_WRONG_USER, 0);
		values.put(KEY_CORRECT_USER, 0);

		ourDatabase.update(TABLE_QUESTIONS, values, KEY_QUESTIONID + "=?",
				new String[] { i_QuestionId });

		this.closeDb();

	}

	public long getLastUpdate() {
		//
		String[] columns = new String[] { "MAX(" + KEY_QUESTIONID + ")",
				"MAX(" + KEY_LAST_UPDATE + ")" };
		long ret = 0;
		int columnIndex0, columnIndex1;
		long qid, qlastupdate;
		this.openDbReadable();

		Cursor cursor = ourDatabase.query(TABLE_QUESTIONS, columns, null, null,
				null, null, null);

		if (cursor.getCount() > 0) {

			columnIndex0 = cursor.getColumnIndex(columns[0]);
			columnIndex1 = cursor.getColumnIndex(columns[1]);

			cursor.moveToFirst();

			qid = cursor.getLong(columnIndex0);
			qlastupdate = cursor.getLong(columnIndex1);

			ret = (qid >= qlastupdate) ? qid : qlastupdate;

		}

		cursor.close();

		this.closeDb();
		return ret;
	}

}
