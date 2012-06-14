package com.tzachsolomon.trivia;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityAbout extends Activity implements OnClickListener {

	private Button buttonSendSuggestion;
	private TextView textViewAboutVersion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		textViewAboutVersion = (TextView) findViewById(R.id.textViewAboutVersion);

		buttonSendSuggestion = (Button) findViewById(R.id.buttonSendSuggestion);

		buttonSendSuggestion.setOnClickListener(this);

		setAppVersion();
	}

	private void setAppVersion() {
		//

		PackageInfo packageInfo;
		try {
			packageInfo = getPackageManager().getPackageInfo(
					"com.tzachsolomon.trivia", PackageManager.GET_META_DATA);
			textViewAboutVersion
			.setText(getString(R.string.textViewAboutVersionText)
					+ packageInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

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
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "tzach.solomon@gmail.com" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Suggestion for Trivia");

		startActivity(Intent
				.createChooser(emailIntent, "Send suggestion in..."));

	}

}
