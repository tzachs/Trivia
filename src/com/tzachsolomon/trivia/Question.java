package com.tzachsolomon.trivia;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Random;

import android.content.ContentValues;
import android.util.Log;

public class Question {

	public static final String TAG = Question.class.getSimpleName();
	
	private String m_QuestionId;
	private String m_Question;
	private ArrayList<String> m_Answers;

	private int m_CorrectAnswerIndex;
	private int m_Category;
	private String m_SubCategory;
	private String m_Language;
	

	private int m_CorrectWrongRatio;

	public Question(ContentValues i_Value) {

		m_CorrectWrongRatio = -1;
		
		m_Answers = new ArrayList<String>();

		m_QuestionId = i_Value.getAsString(TriviaDbEngine.KEY_QUESTIONID);
		m_Question = i_Value.getAsString(TriviaDbEngine.KEY_QUESTION);
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER1));
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER2));
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER3));
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER4));

		m_CorrectAnswerIndex = i_Value
				.getAsInteger(TriviaDbEngine.KEY_ANSWER_INDEX);

		m_Category = i_Value.getAsInteger(TriviaDbEngine.KEY_CATEGORY);
		
		double x = i_Value.getAsDouble(TriviaDbEngine.KEY_CORRECT_WRONG_RATIO);
		
		
		x *= 10;
		x = Math.floor(x + 0.5);
		
		// 10 is the number of levels
		m_CorrectWrongRatio = (int)(x);
		

	}

	public int getQuestionLevel() {
		
		Log.v(TAG,"getQuestionLevel(): Question level is "+ m_CorrectWrongRatio);
		Log.v(TAG,"getQuestionLevel(): Question category is "+ m_Category);

		
		return m_CorrectWrongRatio;

	}
	

	public String getQuestion() {
		//
		return m_Question;
	}

	public String getAnswer1() {
		//
		return m_Answers.get(0);
	}

	public String getAnswer2() {
		//
		return m_Answers.get(1);
	}

	public String getAnswer3() {
		//
		return m_Answers.get(2);
	}

	public String getAnswer4() {
		//
		return m_Answers.get(3);
	}

	public boolean isCorrect(int i) {
		//
		boolean ret = false;

		if (i == m_CorrectAnswerIndex) {
			ret = true;
		}

		return ret;
	}

	public void randomizeAnswerPlaces(Random i_Random) {
		//
		// copying the answer in order to find it after the shuffle
		String answer = m_Answers.get(m_CorrectAnswerIndex - 1);
		int i = 3;

		Collections.shuffle(m_Answers, i_Random);

		// finding new answer index
		while (i > -1) {
			if (answer.contentEquals(m_Answers.get(i))) {
				m_CorrectAnswerIndex = i + 1;
				i = -1;
			} else {
				i--;
			}
		}

	}

	public String getQuestionId() {
		//
		return m_QuestionId;
	}

	public int getCategory() {
		// 
		return m_Category;
	}

}
