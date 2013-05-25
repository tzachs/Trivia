package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;

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
	private JSONHandler mJSONHandler;
	private String mCurrentQuestionId;
	private String mPreviousQuestionId;
	private String mSentQuestionId;

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
		mSentQuestionId = "-1";
		
		mJSONHandler  = new JSONHandler(ActivityReportErrorInQuestion.this);
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
		
		radioButtonCurrentQuestion.setText(extras.getString(INTENT_EXTRA_CURRENT_QUESTION_STRING));
		radioButtonPreviousQuestion.setText(extras.getString(INTENT_EXTRA_PREVIOUS_QUESTION_STRING));
		mCurrentQuestionId = extras.getString(INTENT_EXTRA_CURRENT_QUESTION_ID);
		mPreviousQuestionId = extras.getString(INTENT_EXTRA_PREVIOUS_QUESTION_ID);
		
		radioButtonCurrentQuestion.setChecked(true);
		radioButtonPreviousQuestion.setChecked(false);
		
		mSentQuestionId = mCurrentQuestionId;
		
		// checking if sending error report without previous question
		if ( mPreviousQuestionId.contentEquals("-1")){
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
			mSentQuestionId = mPreviousQuestionId;
			
			break;
		case R.id.radioButtonCurrentQuestion:
			mSentQuestionId = mCurrentQuestionId;
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onClick(View v) {

		String description = editTextErrorDetails.getText().toString();
		
		try {

			mJSONHandler.reportMistakeInQuestionAsync(
					mSentQuestionId, description);
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
