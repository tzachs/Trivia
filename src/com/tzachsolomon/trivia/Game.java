package com.tzachsolomon.trivia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Random;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Game extends Activity implements OnClickListener {

	public static final String TAG = Game.class.getSimpleName();

	private MyCountDownCounter m_CountDownCounter;

	private Button buttonAnswer1;
	private Button buttonAnswer2;
	private Button buttonAnswer3;
	private Button buttonAnswer4;
	private Button buttonReportMistakeInQuestion;

	private TextView textViewTime;
	private TextView textViewQuestion;
	private TextView textViewNumberOfQuestionsLeft;

	private ArrayList<Question> m_Questions;
	private Question m_CurrentQuestion;

	private int m_QuestionIndex;
	private int m_QuestionLength;

	private TriviaDbEngine m_TriviaDb;
	private SharedPreferences m_SharedPreferences;

	private Random m_Random;
	private TextView textViewQuestionDifficulty;
	private int m_DelayBetweenQuestions;

	private int m_CurrentGameType;

	private int m_CurrentQuestionInThisLevel;
	private int m_MaxNumberOfQuestionInLevel;
	private int m_NumberOfLevels;

	private int m_CurrentLevel;

	public static final int KEY_GAMETYPE_ALL_QUESTIONS = 1;
	public static final int KEY_GAMETYPE_LEVELS = 2;
	public static final int KEY_GAMETYPE_CATEGORIES = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		Bundle extras = getIntent().getExtras();

		parseGameSetupAndStart(extras);

	}

	private void parseGameSetupAndStart(Bundle extras) {
		//
		m_CurrentGameType = extras.getInt("GameType");
		switch (m_CurrentGameType) {
		case KEY_GAMETYPE_ALL_QUESTIONS:
			startGameAllQuestions();
			break;
		case KEY_GAMETYPE_CATEGORIES:

			break;
		case KEY_GAMETYPE_LEVELS:
			startGameLevels();
			break;

		default:
			break;
		}

	}

	private void startGameLevels() {
		//
		m_Questions = m_TriviaDb.getEnabledQuestions();

		m_QuestionLength = m_Questions.size();

		// checking if there are questions to be asked
		if (m_Questions.size() > 0) {
			// calculating the question level
			for (Question c : m_Questions) {
				c.calcQuestionLevel();
			}

			// Shuffling the order of the questions
			Collections.shuffle(m_Questions);

			m_CurrentLevel = 0;
			m_NumberOfLevels = 10;
			m_MaxNumberOfQuestionInLevel = 10;

			startNewRoundGameLevels();

		} else {
			Toast.makeText(this, "no questions in database", Toast.LENGTH_SHORT)
					.show();
			finish();

		}

	}

	private void startNewRoundGameLevels() {

		m_CurrentLevel++;

		if (m_CurrentLevel <= m_NumberOfLevels) {
			m_QuestionIndex = 0;
			m_CurrentQuestionInThisLevel = 0;

			Toast.makeText(this, "starting level " + m_CurrentLevel,
					Toast.LENGTH_SHORT).show();

			new StartNewQuestionAsync().execute(0);
		} else {
			Toast.makeText(this, "game over levels", Toast.LENGTH_SHORT).show();
		}
	}

	private void startGameAllQuestions() {
		//
		m_Questions = m_TriviaDb.getEnabledQuestions();

		// Shuffling the order of the questions
		Collections.shuffle(m_Questions);

		m_QuestionLength = m_Questions.size() - 1;

		// checking if there are questions to be asked
		if (m_Questions.size() > 0) {
			m_QuestionIndex = -1;

			new StartNewQuestionAsync().execute(0);
		} else {
			Toast.makeText(this, "no questions in database", Toast.LENGTH_SHORT)
					.show();
			finish();

		}

	}

	public class StartNewQuestionAsync extends AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			//

			startNewQuestion();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			//
			try {
				Thread.sleep(params[0]);
			} catch (InterruptedException e) {
				//
				e.printStackTrace();
			}
			return null;
		}

	}

	private void startNewQuestion() {

		// initialize button color to blue
		buttonAnswer1.setBackgroundResource(R.drawable.blue_button);
		buttonAnswer2.setBackgroundResource(R.drawable.blue_button);
		buttonAnswer3.setBackgroundResource(R.drawable.blue_button);
		buttonAnswer4.setBackgroundResource(R.drawable.blue_button);

		switch (m_CurrentGameType) {
		case KEY_GAMETYPE_ALL_QUESTIONS:
			startNewQuestionAllQuestions();
			break;
		case KEY_GAMETYPE_LEVELS:

			startNewQuestionLevels();
			break;

		default:
			break;
		}

	}

	private void startNewQuestionLevels() {
		//
		m_CurrentQuestionInThisLevel++;

		if (m_CurrentQuestionInThisLevel < m_MaxNumberOfQuestionInLevel) {

			// getting reference to the current question
			m_CurrentQuestion = getNextQuestionInLevel();

			// checking if we are out of questions for this level before the
			// level has ended

			if (m_CurrentQuestion == null) {
				// no more questions in this level
				// going to next level
				startNewRoundGameLevels();

			} else {
				textViewNumberOfQuestionsLeft.setText(Integer
						.toString(m_MaxNumberOfQuestionInLevel
								- m_CurrentQuestionInThisLevel));

				// setting question difficulty
				textViewQuestionDifficulty.setText(Integer
						.toString(m_CurrentQuestion.getQuestionLevel()));

				// randomize answer places (indices)
				m_CurrentQuestion.randomizeAnswerPlaces(m_Random);

				textViewQuestion.setText(m_CurrentQuestion.getQuestion());
				buttonAnswer1.setText(m_CurrentQuestion.getAnswer1());
				buttonAnswer2.setText(m_CurrentQuestion.getAnswer2());
				buttonAnswer3.setText(m_CurrentQuestion.getAnswer3());
				buttonAnswer4.setText(m_CurrentQuestion.getAnswer4());

				m_CountDownCounter.start();

			}

		} else {
			// level ended
			startNewRoundGameLevels();

		}

	}

	private Question getNextQuestionInLevel() {
		//
		boolean foundQuestion = false;
		Question ret = null;
		
		Log.v(TAG, "Current level is " + m_CurrentLevel);

		while (m_QuestionIndex < m_QuestionLength && !foundQuestion) {

			ret = m_Questions.get(m_QuestionIndex);

			

			if (ret.getQuestionLevel() == m_CurrentLevel) {
				foundQuestion = true;
			}
			m_QuestionIndex++;
		}
		
		if ( !foundQuestion){ 
			ret = null;
		}

		return ret;
	}

	private void startNewQuestionAllQuestions() {

		// setting number of questions left, must be before m_QuestionIndex+=
		textViewNumberOfQuestionsLeft.setText(Integer.toString(m_QuestionLength
				- m_QuestionIndex));

		if (m_QuestionIndex < m_QuestionLength) {

			m_QuestionIndex++;

			// getting reference to the current question
			m_CurrentQuestion = m_Questions.get(m_QuestionIndex);

			// setting question difficulty
			textViewQuestionDifficulty.setText(Integer
					.toString(m_CurrentQuestion.getQuestionLevel()));

			// randomize answer places (indices)
			m_CurrentQuestion.randomizeAnswerPlaces(m_Random);

			textViewQuestion.setText(m_CurrentQuestion.getQuestion());
			buttonAnswer1.setText(m_CurrentQuestion.getAnswer1());
			buttonAnswer2.setText(m_CurrentQuestion.getAnswer2());
			buttonAnswer3.setText(m_CurrentQuestion.getAnswer3());
			buttonAnswer4.setText(m_CurrentQuestion.getAnswer4());

			m_CountDownCounter.start();
		}

	}

	private void initializeVariables() {
		//

		m_TriviaDb = new TriviaDbEngine(Game.this);
		// m_Random = new Random(1);
		m_Random = new Random(System.currentTimeMillis());
		int total = 7;

		try {
			total = Integer.parseInt(m_SharedPreferences.getString(
					"editTextPreferenceCountDownTimer", "7"));
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage().toString());
		}
		try {
			m_DelayBetweenQuestions = Integer
					.parseInt(m_SharedPreferences.getString(
							"editTextPreferenceDelayBetweenQuestions", "500"));
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage().toString());
		}

		total *= 1000;

		m_CountDownCounter = new MyCountDownCounter(total, 1000);

		m_CurrentGameType = -1;

		initializeTextViews();

		initializeButtons();

	}

	private void initializeButtons() {
		buttonAnswer1 = (Button) findViewById(R.id.buttonAnswer1);
		buttonAnswer2 = (Button) findViewById(R.id.buttonAnswer2);
		buttonAnswer3 = (Button) findViewById(R.id.buttonAnswer3);
		buttonAnswer4 = (Button) findViewById(R.id.buttonAnswer4);

		buttonReportMistakeInQuestion = (Button) findViewById(R.id.buttonReportMistakeInQuestion);

		buttonAnswer1.setOnClickListener(this);
		buttonAnswer2.setOnClickListener(this);
		buttonAnswer3.setOnClickListener(this);
		buttonAnswer4.setOnClickListener(this);

		buttonReportMistakeInQuestion.setOnClickListener(this);

	}

	private void initializeTextViews() {
		textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
		textViewTime = (TextView) findViewById(R.id.textViewTime);
		textViewNumberOfQuestionsLeft = (TextView) findViewById(R.id.textViewNumberOfQuestionsLeft);
		textViewQuestionDifficulty = (TextView) findViewById(R.id.textViewQuestionDifficulty);
	}

	public class MyCountDownCounter extends CountDownTimer {

		public MyCountDownCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			//
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//
			textViewTime.setText(Long.toString(millisUntilFinished / 1000));

		}

		@Override
		public void onFinish() {
			//
			checkAnswer(-1, null);
		}

	}

	@Override
	public void onClick(View v) {
		//

		switch (v.getId()) {

		case R.id.buttonReportMistakeInQuestion:
			buttonReportMistakeInQuestion_Clicked();
			break;

		case R.id.buttonAnswer1:
			checkAnswer(1, buttonAnswer1);
			break;

		case R.id.buttonAnswer2:
			checkAnswer(2, buttonAnswer2);
			break;

		case R.id.buttonAnswer3:
			checkAnswer(3, buttonAnswer3);
			break;

		case R.id.buttonAnswer4:
			checkAnswer(4, buttonAnswer4);
			break;

		default:
			break;
		}

	}

	private void buttonReportMistakeInQuestion_Clicked() {
		//

		JSONHandler m_JSONHandler = new JSONHandler(Game.this);

		try {

			m_JSONHandler.reportMistakeInQuestionAsync(
					m_CurrentQuestion.getQuestionId(), "no description");
			Toast.makeText(Game.this, "sent, thanks :)", Toast.LENGTH_SHORT)
					.show();
		} catch (ClientProtocolException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		} catch (IOException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		}

	}

	private void setButtonRed(Button o_Button) {
		o_Button.setBackgroundResource(R.drawable.red_button);
	}

	private void setButtonGreen(Button o_Button) {
		o_Button.setBackgroundResource(R.drawable.green_button);
	}

	private int checkAnswer(int i, Button o_Button) {
		//
		StringBuilder sb = new StringBuilder();
		int ret = 1;

		// stopping the counter in order to create a race condition where the
		// user already click but the timer
		// is still running

		stopCountdownCounter();

		// checking if time is up
		if (i == -1) {
			ret = -1;
			sb.append("Time is up!");

		} else {

			if (m_CurrentQuestion.isCorrect(i)) {
				ret = 0;
				setButtonGreen(o_Button);

				m_TriviaDb.incUserCorrectCounter(m_CurrentQuestion
						.getQuestionId());

			} else {
				setButtonRed(o_Button);
				m_TriviaDb.incUserWrongCounter(m_CurrentQuestion
						.getQuestionId());

			}

		}

		if (m_QuestionIndex < m_QuestionLength) {
			// start a new question and
			new StartNewQuestionAsync().execute(m_DelayBetweenQuestions);
		} else {
			if (m_CurrentGameType == KEY_GAMETYPE_ALL_QUESTIONS) {

				sb.append("Finished Round!");
				// TODO: how to end this
			}
		}

		if (sb.length() > 0) {
			Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
			sb.setLength(0);
		}

		return ret;

	}

	private void stopCountdownCounter() {
		//
		m_CountDownCounter.cancel();
	}

	@Override
	protected void onResume() {
		//
		super.onResume();

		// checking if to show the report question button
		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceShowReportQuestion", false)) {

			buttonReportMistakeInQuestion.setVisibility(View.VISIBLE);
		} else {
			buttonReportMistakeInQuestion.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		//
		super.onPause();

		stopCountdownCounter();
	}

}
