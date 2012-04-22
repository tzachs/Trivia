package com.tzachsolomon.trivia;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TriviaDbEngine {

	public static final String TAG = TriviaDbEngine.class.getSimpleName();

	private static final String DATABASE_NAME = "TriviaDb";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_QUESTIONS = "tblQuestions";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_QUESTIONID = "colQuestionId";
	public static final String KEY_QUESTION = "colQuestion";
	public static final String KEY_ANSWER1 = "colAnswer1";
	public static final String KEY_ANSWER2 = "colAnswer2";
	public static final String KEY_ANSWER3 = "colAnswer3";
	public static final String KEY_ANSWER_INDEX = "colAnswerIndex";
	public static final String KEY_ANSWER4 = "colAnswer4";
	public static final String KEY_CATEGORY = "colCategory";
	public static final String KEY_SUB_CATEGORY = "colSubCategory";
	public static final String KEY_LANGUAGE = "colLanguage";
	public static final String KEY_CORRECT = "colCorrect";
	public static final String KEY_WRONG = "colWrong";
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
		
			sb.append("CREATE TABLE IF NOT EXISTS");
			sb.append(TABLE_QUESTIONS);
			sb.append(" (");
			sb.append(KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			sb.append(KEY_QUESTIONID + " REAL NOT NULL, ");
			sb.append(KEY_QUESTION + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER1 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER2 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER3 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER4 + " TEXT NOT NULL, ");
			sb.append(KEY_ANSWER_INDEX + " INTEGER NOT NULL, ");
			sb.append(KEY_CATEGORY + " TEXT, ");
			sb.append(KEY_CORRECT + " REAL NOT NULL, ");
			sb.append(KEY_LANGUAGE + " TEXT NOT NULL, ");
			sb.append(KEY_SUB_CATEGORY + " TEXT, ");
			sb.append(KEY_WRONG + " REAL NOT NULL,");
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
		}

	}
	
	public TriviaDbEngine (Context c){
		ourContext = c;
	}
	
	public TriviaDbEngine open(){
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();
		
		return this;

	}
	
	public void close() {
		ourHelper.close();
	}

	public Question[] getQuestions() {
		// 
		Question[] ret = new Question[2];
		
		ret[0] = new Question();
		ret[1] = new Question();
		
		return ret;
	}

}
