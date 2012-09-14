package com.tzachsolomon.trivia;


import java.util.ArrayList;

import java.util.Collections;
import java.util.Random;

import android.content.ContentValues;


public class Question {

	public static final String TAG = Question.class.getSimpleName();
	
	private String mQuestionId;
	private String mQuestion;
	private ArrayList<String> mAnswers;

	private int mCorrectAnswerIndex;
	private int mCategory;
	private int mTimesPlayed;
	
	private int mCorrectWrongRatio;

	public Question(ContentValues i_Value) {

		mCorrectWrongRatio = -1;
		
		mAnswers = new ArrayList<String>();

		mQuestionId = i_Value.getAsString(TriviaDbEngine.KEY_QUESTIONID);
		mQuestion = i_Value.getAsString(TriviaDbEngine.KEY_QUESTION);
		mAnswers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER1));
		mAnswers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER2));
		mAnswers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER3));
		mAnswers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER4));

		mCorrectAnswerIndex = i_Value
				.getAsInteger(TriviaDbEngine.KEY_ANSWER_INDEX);

		mCategory = i_Value.getAsInteger(TriviaDbEngine.KEY_CATEGORY);
		mTimesPlayed = i_Value.getAsInteger(TriviaDbEngine.KEY_PLAYED_COUNTER);
		
		double x = i_Value.getAsDouble(TriviaDbEngine.KEY_CORRECT_WRONG_RATIO);
		
		
		x *= 10;
		x = Math.floor(x + 0.5);
		
		// 10 is the number of levels
		mCorrectWrongRatio = (int)(x);
		

	}

	public int getQuestionLevel() {
		
		if ( mCorrectWrongRatio == 0){
			mCorrectWrongRatio = 1;
		}
		return mCorrectWrongRatio;

	}
	
	

	public String getQuestion() {
		//
		return mQuestion;
	
	}

	public String getAnswer1() {
		//
		return mAnswers.get(0);
	}

	public String getAnswer2() {
		//
		return mAnswers.get(1);
	}

	public String getAnswer3() {
		//
		return mAnswers.get(2);
	}

	public String getAnswer4() {
		//
		return mAnswers.get(3);
	}
	
	public int getCorrectAnswerIndex (){
		return mCorrectAnswerIndex;
	}

	public boolean isCorrect(int i) {
		//
		boolean ret = false;

		if (i == mCorrectAnswerIndex) {
			ret = true;
		}

		return ret;
	}

	public void randomizeAnswerPlaces(Random i_Random) {
		//
		// copying the answer in order to find it after the shuffle
		String answer = mAnswers.get(mCorrectAnswerIndex - 1);
		int i = 3;

		Collections.shuffle(mAnswers, i_Random);

		// finding new answer index
		while (i > -1) {
			if (answer.contentEquals(mAnswers.get(i))) {
				mCorrectAnswerIndex = i + 1;
				i = -1;
			} else {
				i--;
			}
		}

	}

	public String getQuestionId() {
		//
		return mQuestionId;
	}

	public int getCategory() {
		// 
		return mCategory;
	}

	public int getQuestionTimesPlayed() {
		//
		return mTimesPlayed;
	}

}
