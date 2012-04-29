package com.tzachsolomon.trivia;

import java.util.Random;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.preference.PreferenceManager;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Game extends Activity implements OnClickListener {

	public static final String TAG = Game.class.getSimpleName();
	
	
	private MyCountDownCounter m_CountDownCounter;
	private TextView textViewTime;
	private Button buttonAnswer1;
	private Button buttonAnswer2;
	private Button buttonAnswer3;
	private Button buttonAnswer4;

	private Question[] m_Questions;
	private TextView textViewQuestion;
	private int m_QuestionIndex;
	private int m_QuestionLength;

	private TriviaDbEngine m_TriviaDb;
	private SharedPreferences m_SharedPreferences;
	private TextView textViewNumberOfQuestionsLeft;

	private Random m_Random;
	private TextView textViewQuestionDifficulty;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		m_Questions = m_TriviaDb.getEnabledQuestions();

		m_QuestionLength = m_Questions.length - 1;

		// checking if there are questions to be asked
		if (m_Questions.length > 0) {
			m_QuestionIndex = -1;

			startNewQuestion();
		} else {
			finish();

		}

	}

	private void startNewQuestion() {
		//
		// setting number of questions left, must be before m_QuestionIndex+=
		textViewNumberOfQuestionsLeft.setText(Integer.toString(m_QuestionLength
				- m_QuestionIndex));
		

		m_QuestionIndex++;

		// setting question difficulty
		textViewQuestionDifficulty.setText(Double
				.toString(m_Questions[m_QuestionIndex]
						.getQuestionDifficultyLevel()));

		m_Questions[m_QuestionIndex].randomizeAnswerPlaces(m_Random);

		textViewQuestion.setText(m_Questions[m_QuestionIndex].getQuestion());
		buttonAnswer1.setText(m_Questions[m_QuestionIndex].getAnswer1());
		buttonAnswer2.setText(m_Questions[m_QuestionIndex].getAnswer2());
		buttonAnswer3.setText(m_Questions[m_QuestionIndex].getAnswer3());
		buttonAnswer4.setText(m_Questions[m_QuestionIndex].getAnswer4());

		m_CountDownCounter.start();

	}

	private void initializeVariables() {
		//
		m_TriviaDb = new TriviaDbEngine(Game.this);
		// m_Random = new Random(1);
		m_Random = new Random(System.currentTimeMillis());

		long total = m_SharedPreferences.getLong(
				"editTextPreferenceDefaultCountDownTimer", 7);
		total *= 1000;

		m_CountDownCounter = new MyCountDownCounter(total, 1000);

		initializeTextViews();

		initializeButtons();

	}

	private void initializeButtons() {
		buttonAnswer1 = (Button) findViewById(R.id.buttonAnswer1);
		buttonAnswer2 = (Button) findViewById(R.id.buttonAnswer2);
		buttonAnswer3 = (Button) findViewById(R.id.buttonAnswer3);
		buttonAnswer4 = (Button) findViewById(R.id.buttonAnswer4);

		buttonAnswer1.setOnClickListener(this);
		buttonAnswer2.setOnClickListener(this);
		buttonAnswer3.setOnClickListener(this);
		buttonAnswer4.setOnClickListener(this);
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

	private void checkAnswer(int i, Button o_Button) {
		//
		StringBuilder stringBuilder = new StringBuilder();

		// checking if time is up
		if (i == -1) {
			stringBuilder.append("Time is up!");

		} else {

			if (m_Questions[m_QuestionIndex].isCorrect(i)) {

				//o_Button.setBackgroundResource(R.drawable.green_button);
				stringBuilder.append("correct");
				m_TriviaDb.incUserCorrectCounter(m_Questions[m_QuestionIndex]
						.getQuestionId());

			} else {
				//o_Button.setBackgroundResource(R.drawable.red_button);
				stringBuilder.append("wrong");
				m_TriviaDb.incUserWrongCounter(m_Questions[m_QuestionIndex]
						.getQuestionId());
			}
			
		}
		// checking if we need to move to the next question
		if (m_QuestionIndex < m_QuestionLength) {
			
			startNewQuestion();
		} else {
			// round finished
			stopCountdownCounter();
		}
		
		Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
		
		stringBuilder.setLength(0);


	}

	private void stopCountdownCounter() {
		//
		m_CountDownCounter.cancel();
	}

	@Override
	protected void onPause() {
		//
		super.onPause();

		stopCountdownCounter();
	}

}
