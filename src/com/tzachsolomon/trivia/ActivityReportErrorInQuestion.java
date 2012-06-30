package com.tzachsolomon.trivia;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class ActivityReportErrorInQuestion extends Activity implements OnCheckedChangeListener, OnClickListener {
	
	
	public static final String TAG = ActivityReportErrorInQuestion.class.getSimpleName();
	
	private Button buttonSendError;
	private RadioButton radioButtonPreviousQuestion;
	private RadioButton radioButtonCurrentQuestion;
	private JSONHandler m_JSONHandler;
	private String m_CurrentQuestionId;
	private String m_PreviousQuestionId;
	private String m_SentQuestionId;

	private EditText editTextErrorDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_error_in_question);
		
		initializeVariables();
	}

	private void initializeVariables() {
		// 
		m_SentQuestionId = "-1";
		
		m_JSONHandler  = new JSONHandler(ActivityReportErrorInQuestion.this);
		initializeButtons();
		initializeRadioButtons();
		initializeEditText();
	}

	private void initializeEditText() {
		// 
		editTextErrorDetails = (EditText)findViewById(R.id.editTextErrorDetails);
		
	}

	private void initializeRadioButtons() {
		// 
		radioButtonPreviousQuestion = (RadioButton)findViewById(R.id.radioButtonPreviousQuestion);
		radioButtonCurrentQuestion = (RadioButton)findViewById(R.id.radioButtonCurrentQuestion);
		
		radioButtonPreviousQuestion.setOnCheckedChangeListener(this);
		radioButtonCurrentQuestion.setOnCheckedChangeListener(this);
		
		Bundle extras = getIntent().getExtras();
		
		radioButtonCurrentQuestion.setText(extras.getString(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_STRING));
		radioButtonPreviousQuestion.setText(extras.getString(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING));
		m_CurrentQuestionId = extras.getString(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_ID);
		m_PreviousQuestionId = extras.getString(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_ID);
		
		radioButtonCurrentQuestion.setChecked(true);
		radioButtonPreviousQuestion.setChecked(false);
		
		m_SentQuestionId = m_CurrentQuestionId;
		
		// checking if sending error report without previous question
		if ( m_PreviousQuestionId.contentEquals("-1")){
			radioButtonPreviousQuestion.setVisibility(View.GONE);
			
		}
		
	}

	private void initializeButtons() {
		// 
		buttonSendError = (Button)findViewById(R.id.buttonSendError);
		buttonSendError.setOnClickListener(this);
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// 
		switch (buttonView.getId()) {
		case R.id.radioButtonPreviousQuestion:
			m_SentQuestionId = m_PreviousQuestionId;
			
			break;
		case R.id.radioButtonCurrentQuestion:
			m_SentQuestionId = m_CurrentQuestionId;
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onClick(View v) {

		String description = editTextErrorDetails.getText().toString();
		
		try {

			m_JSONHandler.reportMistakeInQuestionAsync(
					m_SentQuestionId, description);
			Toast.makeText(ActivityReportErrorInQuestion.this, getString(R.string.error_sent_thanks_), Toast.LENGTH_SHORT)
					.show();
		} catch (ClientProtocolException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		} catch (IOException e) {
			//
			Log.e(TAG, e.getMessage().toString());
		}
		
	}

}
