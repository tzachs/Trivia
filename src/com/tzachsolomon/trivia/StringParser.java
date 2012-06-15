package com.tzachsolomon.trivia;

public class StringParser {

	public static String reverseNumbersInString(String i_String) {
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

	public static void reverseInString(int i_StartIndex, int i_EndIndex,
			StringBuilder i_StringBuilder) {
		
		StringBuilder rev;
		
		
		rev = new StringBuilder(i_StringBuilder.substring(i_StartIndex, i_EndIndex));
		
		i_StringBuilder.replace(i_StartIndex, i_EndIndex, rev.reverse().toString());
		
		

	}

}
