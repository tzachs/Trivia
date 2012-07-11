package com.tzachsolomon.trivia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class ActivityWhatsNew extends Activity implements OnClickListener {

	private WebView webViewWhatsNew;
	private Button buttonWhatsNewClose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_whats_new);

		initializeVariables();

	}
	
	private String readTitlesRaw() throws IOException{
		
		
		InputStream stream = getResources().openRawResource(R.raw.changelog);

	    Writer writer = new StringWriter();
	    char[] buffer = new char[10240];
	    try {
	        Reader reader = new BufferedReader(
	        new InputStreamReader(stream, "UTF-8"));
	        int n;
	        while ((n = reader.read(buffer)) != -1) {
	            writer.write(buffer, 0, n);
	        }
	    } finally {
	        stream.close();
	    }
	    return writer.toString();
	}

	private void initializeVariables() {
		//
		
		webViewWhatsNew = (WebView) findViewById(R.id.webViewWhatsNew);
		buttonWhatsNewClose = (Button) findViewById(R.id.buttonWhatsNewClose);
		
		//webview being your WebView object reference. 
		webViewWhatsNew.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);


		
		String html = "";
		try {
			html = readTitlesRaw();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		webViewWhatsNew.loadDataWithBaseURL(null, html,"text/html", "UTF-8", null);

		buttonWhatsNewClose.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonWhatsNewClose:
			buttonWhatsNewClose_Clicked();
			break;

		default:
			break;
		}
	}

	private void buttonWhatsNewClose_Clicked() {
		//
		finish();

	}

}
