package com.tzachsolomon.trivia;

import com.tzachsolomon.trivia.JSONHandler.SuggestQuestionListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActivitySuggestQuestion extends Activity implements
		OnClickListener, SuggestQuestionListener {

	private int m_CurrentUserID;
	private Button buttonSendSuggestion;
	private JSONHandler m_JSONHandler;
	private EditText editTextAnswerCorrect;
	private EditText editTextAnswerWrong1;
	private EditText editTextAnswerWrong2;
	private EditText editTextAnswerWrong3;
	private EditText editTextQuestion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest_question);

		initializeVariables();
	}

	private void initializeVariables() {
		//
		m_CurrentUserID = -2;
		m_JSONHandler = new JSONHandler(ActivitySuggestQuestion.this);

		m_JSONHandler.setSuggestQuestionListener(this);

		buttonSendSuggestion = (Button) findViewById(R.id.buttonSendSuggestion);

		buttonSendSuggestion.setOnClickListener(this);

		editTextAnswerCorrect = (EditText) findViewById(R.id.editTextAnswerCorrect);
		editTextAnswerWrong1 = (EditText) findViewById(R.id.editTextAnswerWrong1);
		editTextAnswerWrong2 = (EditText) findViewById(R.id.editTextAnswerWrong2);
		editTextAnswerWrong3 = (EditText) findViewById(R.id.editTextAnswerWrong3);
		editTextQuestion = (EditText) findViewById(R.id.editTextQuestion);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonSendSuggestion:
			buttonSendSuggestion_Clicked();
			break;

		default:
			break;
		}

	}

	private void buttonSendSuggestion_Clicked() {
		//
		String answerCorrect = editTextAnswerCorrect.getText().toString();
		String answerWrong1 = editTextAnswerWrong1.getText().toString();
		String answerWrong2 = editTextAnswerWrong2.getText().toString();
		String answerWrong3 = editTextAnswerWrong3.getText().toString();
		String answerQuestion = editTextQuestion.getText().toString();

		m_JSONHandler.sendQuestionSuggestionAsync(m_CurrentUserID,
				answerCorrect, answerQuestion, answerWrong1, answerWrong2,
				answerWrong3);

	}

	@Override,
	public void onSuggestionSent() {
		//
		if ( m_CurrentUserID > 0 ){
		Toast.makeText(
				ActivitySuggestQuestion.this,
				getString(R.string.thank_for_sending_a_suggestion_in_case_you_are_a_registered_user_with_email_filled_in_recieve_a_notication_when_the_question_is_enabled),
				Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(
					ActivitySuggestQuestion.this,
					getString(R.string.thank_you_for_your_contribution_to_social_trivia_),
					Toast.LENGTH_LONG).show();
				
		}
	}
};
