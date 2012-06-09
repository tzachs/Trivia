package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutActivity extends Activity implements OnClickListener {
	
	private Button buttonSendSuggestion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		buttonSendSuggestion = (Button)findViewById(R.id.buttonSendSuggestion);
		
		buttonSendSuggestion.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// 
		switch (v.getId()){
		case R.id.buttonSendSuggestion:
			buttonSendSuggestion_Clicked();
			break;
			
		default:
			break;
		}
		
	}

	private void buttonSendSuggestion_Clicked() {
		// 
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"tzach.solomon@gmail.com"});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Suggestion for Trivia");
		
		startActivity(Intent.createChooser(emailIntent, "Send suggestion in..."));
		
	}

}
