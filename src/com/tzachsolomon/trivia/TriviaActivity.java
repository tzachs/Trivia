package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TriviaActivity extends Activity implements OnClickListener {
    private Button buttonNewGame;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeVariables();
    }

	private void initializeVariables() {
		// 
		
		initializeButtons();
		
	}

	private void initializeButtons() {
		// 
		buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
		
		buttonNewGame.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		// 
		switch ( v.getId()){
		case R.id.buttonNewGame:
			buttonNewGame_Clicked();
			break;
		}
		
	}

	private void buttonNewGame_Clicked() {
		//
		Intent intent = new Intent(this, Game.class);
		startActivity(intent);
		
	}
}