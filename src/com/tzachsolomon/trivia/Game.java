package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;

public class Game extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
	}

}
