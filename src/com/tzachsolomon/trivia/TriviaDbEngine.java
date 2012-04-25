package com.tzachsolomon.trivia;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
	
	public long insertQuestion(long i_QuestionId, String i_Question, String i_Answer1, String i_Answer2,
			String i_Answer3, String i_Answer4, int i_AnswerIndex, String i_Category, String i_SubCategory, 
			String i_Language, long i_Correct, long i_Wrong, String i_DateCreated, String i_LastUpdate, boolean i_Enabled){
		
		long ret;
		
		this.open();
		
		ContentValues cv = new ContentValues();
		
		cv.put(KEY_ANSWER1, i_Answer1);
		cv.put(KEY_ANSWER2, i_Answer2);
		cv.put(KEY_ANSWER3, i_Answer3);
		cv.put(KEY_ANSWER4, i_Answer4);
		cv.put(KEY_ANSWER_INDEX, i_AnswerIndex);
		cv.put(KEY_CATEGORY, i_Category);
		cv.put(KEY_CORRECT, i_Correct);
		cv.put(KEY_DATE_CREATED, i_DateCreated);
		cv.put(KEY_ENABLED, i_Enabled);
		cv.put(KEY_LANGUAGE, i_Language);
		cv.put(KEY_LAST_UPDATE, i_LastUpdate);
		cv.put(KEY_QUESTION, i_Question);
		cv.put(KEY_QUESTIONID, i_QuestionId);
		cv.put(KEY_SUB_CATEGORY, i_SubCategory);
		cv.put(KEY_WRONG, i_Wrong);
		
		ret = ourDatabase.insert(TABLE_QUESTIONS, null, cv);
		
		this.close();
		
		return ret;
	}
	
	public void createSampleQuestions(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		insertQuestion(System.currentTimeMillis(), "Answer to the universe","42","Michael Jordan", "Nothing", "Checking long answer if fit",
				1, "Litrature", "", "English", 0, 0, calendar.getTime().toString(), calendar.getTime().toString(), true);
		insertQuestion(System.currentTimeMillis(), "Who wrote Lord of the rings","No1","Jrr", "Shlomo Oz", "Michale Crichton",
				1, "Litrature", "", "English", 0, 0, calendar.getTime().toString(), calendar.getTime().toString(), true);
		
	}

	public Question[] getQuestions() {
		//
		String[] columns = { KEY_ANSWER1, KEY_ANSWER2, KEY_ANSWER3, KEY_ANSWER4, KEY_ANSWER_INDEX, KEY_CATEGORY,
				KEY_CORRECT, KEY_DATE_CREATED, KEY_ENABLED, KEY_LANGUAGE, KEY_LAST_UPDATE, KEY_QUESTION, KEY_QUESTIONID,
				KEY_ROWID, KEY_SUB_CATEGORY, KEY_WRONG };
		
		Cursor cursor;
		int numberOfQuestions = -1;
		this.open();
		
		cursor = ourDatabase.query(TABLE_QUESTIONS, columns, null, null, null, null, null);
		numberOfQuestions = cursor.getCount();
		
		Question[] ret = new Question[numberOfQuestions];
		
		ret[0] = new Question();
		ret[1] = new Question();
		
		this.close();
		
		return ret;
	}

}
