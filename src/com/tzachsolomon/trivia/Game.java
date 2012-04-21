package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Game extends Activity implements OnClickListener {

	private MyCountDownCounter m_CountDownCounter;
	private TextView textViewTime;
	private Button buttonAnswer1;
	private Button buttonAnswer2;
	private Button buttonAnswer3;
	private Button buttonAnswer4;
	
	private Question m_Questions;
	private TextView textViewQuestion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		initializeVariables();
		
		m_Questions = new Question();

		startNewQuestion();

	}

	private void startNewQuestion() {
		//
		textViewQuestion.setText(m_Questions.getQuestion());
		buttonAnswer1.setText(m_Questions.getAnswer1());
		buttonAnswer2.setText(m_Questions.getAnswer2());
		buttonAnswer3.setText(m_Questions.getAnswer3());
		buttonAnswer4.setText(m_Questions.getAnswer4());
		
		m_CountDownCounter.start();

	}

	private void initializeVariables() {
		//
		m_CountDownCounter = new MyCountDownCounter(5000, 1000);
		
		textViewQuestion = (TextView)findViewById(R.id.textViewQuestion);
		textViewTime = (TextView) findViewById(R.id.textViewTime);
		
		
		buttonAnswer1 = (Button) findViewById(R.id.buttonAnswer1);
		buttonAnswer2 = (Button) findViewById(R.id.buttonAnswer2);
		buttonAnswer3 = (Button) findViewById(R.id.buttonAnswer3);
		buttonAnswer4 = (Button) findViewById(R.id.buttonAnswer4);

		buttonAnswer1.setOnClickListener(this);
		buttonAnswer2.setOnClickListener(this);
		buttonAnswer3.setOnClickListener(this);
		buttonAnswer4.setOnClickListener(this);

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
			checkAnswer(-1);

		}

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonAnswer1:
			checkAnswer(1);

			break;
		case R.id.buttonAnswer2:
			checkAnswer(2);

			break;
		case R.id.buttonAnswer3:
			checkAnswer(3);

			break;
		case R.id.buttonAnswer4:
			checkAnswer(4);

			break;

		default:
			break;
		}

	}

	private void checkAnswer(int i) {
		// 
		StringBuilder stringBuilder =  new StringBuilder();
		if ( m_Questions.isCorrect(i)) {
			stringBuilder.append("correct");
			
		}else {
			stringBuilder.append("wrong");
		}
		
		Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
		
		startNewQuestion();
	}
	
	@Override
	protected void onPause() {
		// 
		super.onPause();
		
		m_CountDownCounter.cancel();
	}

}
