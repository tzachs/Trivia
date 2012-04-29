package com.tzachsolomon.trivia;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TriviaDbEngine {

	public static final String TAG = TriviaDbEngine.class.getSimpleName();

	private static final String DATABASE_NAME = "TriviaDb";
	private static final int DATABASE_VERSION = 2;
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
	public static final String KEY_CORRECT_FROM_DB = "colCorrect";
	public static final String KEY_WRONG_FROM_DB = "colWrong";
	public static final String KEY_CORRECT_USER = "colCorrectUser"; // the correct guesses the user did of a question
	public static final String KEY_WRONG_USER = "colWrongUser"; // the wrong guesses the user did of a question
	public static final String KEY_DATE_CREATED = "colDateCreated";
	public static final String KEY_LAST_UPDATE = "colLastUpdate";
	public static final String KEY_ENABLED = "colEnabled";
	
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
			sb.append(KEY_CATEGORY + " TEXT, ");
			sb.append(KEY_CORRECT_FROM_DB + " INTEGER NOT NULL, ");
			sb.append(KEY_CORRECT_USER + " INTEGER NOT NULL, ");
			sb.append(KEY_LANGUAGE + " TEXT NOT NULL, ");
			sb.append(KEY_SUB_CATEGORY + " TEXT, ");
			sb.append(KEY_WRONG_FROM_DB + " INTEGER NOT NULL,");
			sb.append(KEY_WRONG_USER + " INTEGER NOT NULL,");
			sb.append(KEY_ENABLED + " BOOLEAN NOT NULL, ");
			sb.append(KEY_DATE_CREATED + " TEXT NOT NULL, ");
			sb.append(KEY_LAST_UPDATE + " TEXT NOT NULL ");
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
	
	public TriviaDbEngine (Context c){
		ourContext = c;
	}
	
	private TriviaDbEngine openDbWritable(){
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
	
	public long insertQuestion(String i_QuestionId, String i_Question, String i_Answer1, String i_Answer2,
			String i_Answer3, String i_Answer4, int i_AnswerIndex, String i_Category, String i_SubCategory, 
			String i_Language, long i_Correct, long i_Wrong, String i_DateCreated, String i_LastUpdate, boolean i_Enabled,
			long i_CorrectUser, long i_WrongUser){
		
		long ret;
		
		this.openDbWritable();
		
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_ANSWER1, i_Answer1);
		cv.put(KEY_ANSWER2, i_Answer2);
		cv.put(KEY_ANSWER3, i_Answer3);
		cv.put(KEY_ANSWER4, i_Answer4);
		cv.put(KEY_ANSWER_INDEX, i_AnswerIndex);
		cv.put(KEY_CATEGORY, i_Category);
		cv.put(KEY_CORRECT_FROM_DB, i_Correct);
		cv.put(KEY_CORRECT_USER, i_CorrectUser);
		cv.put(KEY_DATE_CREATED, i_DateCreated);
		cv.put(KEY_ENABLED, i_Enabled);
		cv.put(KEY_LANGUAGE, i_Language);
		cv.put(KEY_LAST_UPDATE, i_LastUpdate);
		cv.put(KEY_QUESTION, i_Question);
		cv.put(KEY_QUESTIONID, i_QuestionId);
		cv.put(KEY_SUB_CATEGORY, i_SubCategory);
		cv.put(KEY_WRONG_FROM_DB, i_Wrong);
		cv.put(KEY_WRONG_USER, i_WrongUser);
		
		ret = ourDatabase.insert(TABLE_QUESTIONS, null, cv);
		
		this.closeDb();
		
		return ret;
	}
	
	public void createSampleQuestions(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		insertQuestion(Long.toString(System.currentTimeMillis()), "Answer to the universe","42","Michael Jordan", "Nothing", "Checking long answer if fit",
				1, "Litrature", "", "English", 0, 0, calendar.getTime().toString(), calendar.getTime().toString(), true,
				0,0);
		
		
		insertQuestion(Long.toString(System.currentTimeMillis()), "Who wrote Lord of the rings","No1","Jrr", "Shlomo Oz", "Michale Crichton",
				2, "Litrature", "", "English", 0, 0, calendar.getTime().toString(), calendar.getTime().toString(), true,
				0,0);
		
	}

	public Question[] getEnabledQuestions() {
		//
		ContentValues map;
		String[] columns = { KEY_ANSWER1, KEY_ANSWER2, KEY_ANSWER3, KEY_ANSWER4, KEY_ANSWER_INDEX, KEY_CATEGORY,
				 KEY_CORRECT_FROM_DB, KEY_CORRECT_USER, KEY_DATE_CREATED, KEY_ENABLED, KEY_LANGUAGE, KEY_LAST_UPDATE, KEY_QUESTION, KEY_QUESTIONID,
				KEY_ROWID, KEY_SUB_CATEGORY, KEY_WRONG_FROM_DB, KEY_WRONG_USER };
		
		Cursor cursor;
		int numberOfQuestions = -1;
		int i;
		this.openDbReadable();
		
		 map = new ContentValues();
		cursor = ourDatabase.query(TABLE_QUESTIONS, columns, KEY_ENABLED + "=1", null, null, null, null);
		numberOfQuestions = cursor.getCount();
		
		Question[] ret = new Question[numberOfQuestions];
		i = 0;
		
		for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			 	
			 	DatabaseUtils.cursorRowToContentValues(cursor, map);
			 	
				ret[i] = new Question(map);
				i++;
			
		}
		
		cursor.close();
		
		this.closeDb();
		
		return ret;
	}

	public boolean isEmpty() {
		// 
		boolean ret = true;
		// the column KEY_ANSWER1 doesn't matter, Just need to check if there are any rows
		// any other column could be chosen
		String[] columns = { KEY_ANSWER1 };
		
		this.openDbReadable();
		Cursor cursor = ourDatabase.query(TABLE_QUESTIONS, columns, null, null, null,null,null);
		if ( cursor.getCount() > 0 ){
			ret = false;
		}
		
		cursor.close();
		
		this.closeDb();
		
		return ret;
	}

	public int deleteQuestions() {
		//
		int ret;
		openDbWritable();
		ret = ourDatabase.delete(TABLE_QUESTIONS, null, null);
		closeDb();
		
		return ret;
		
		
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
		
		Log.v(TAG, "Executing " + sb.toString());
		ourDatabase.execSQL(sb.toString());
		
		this.closeDb();
		
		sb.setLength(0);
		
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
		
	}

	public void updateFromInternet(ContentValues[] values) {
		// 
		this.openDbWritable();
		
		for ( ContentValues cv : values) {
			cv.put(TriviaDbEngine.KEY_CORRECT_USER, 0);
			cv.put(TriviaDbEngine.KEY_WRONG_USER, 0);
			 ourDatabase.insert(TABLE_QUESTIONS, null, cv);
		}
		
		this.closeDb();
		
	}

}
