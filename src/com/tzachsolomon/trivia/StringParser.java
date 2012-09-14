package com.tzachsolomon.trivia;

import android.content.SharedPreferences;


public class StringParser {

	private  SharedPreferences mSharedPreferences;
			
	public StringParser (SharedPreferences i_SharedPreferences){
		mSharedPreferences = i_SharedPreferences;
	}
	
	public  String reverseNumbersInStringHebrew(String i_String) {
		
		boolean reverse = mSharedPreferences.getBoolean(
				"checkBoxPreferenceRevereseInHebrew", false);
		String lang = mSharedPreferences.getString("listPreferenceLanguages", "iw");
		if ( reverse && lang.contentEquals("iw")){
			return reverseNumbersInString(i_String);
		}else{
			return i_String;
		}
		
		
		
	}
	
	public String reverseNumbersInString(String i_String) {
		StringBuilder ret = new StringBuilder(i_String);
		int i = ret.length() - 1;
		int endIndex;

		endIndex = -1;

		while (i > -1) {

			if (Character.isDigit(ret.charAt(i))) {
				if (endIndex == -1) {
					endIndex = i+1;
				}
			} else if (endIndex != -1) {
				reverseInString(i+1, endIndex, ret);
				endIndex = -1;
			}
			
			i--;
		}

		return ret.toString();

	}

	public void reverseInString(int i_StartIndex, int i_EndIndex,
			StringBuilder i_StringBuilder) {
		
		StringBuilder rev;
		
		
		rev = new StringBuilder(i_StringBuilder.substring(i_StartIndex, i_EndIndex));
		
		i_StringBuilder.replace(i_StartIndex, i_EndIndex, rev.reverse().toString());
		
		

	}

}
