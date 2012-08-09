package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityHighScores extends Activity implements OnClickListener {
	
	private Button buttonCloseHighScore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_scores);
		
		initializeVariables();
	}

	private void initializeVariables() {
		// 
		buttonCloseHighScore = (Button)findViewById(R.id.buttonCloseHighScore);
		
		buttonCloseHighScore.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		// 
		switch (v.getId()){
		case R.id.buttonCloseHighScore:
			finish();
			break;
		}
		
	}

}
