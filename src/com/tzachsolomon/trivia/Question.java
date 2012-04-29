package com.tzachsolomon.trivia;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Random;

import android.content.ContentValues;

public class Question {

	private String m_QuestionId;
	private String m_Question;
	private ArrayList<String> m_Answers;

	private int m_CorrectAnswerIndex;
	private String m_Category;
	private String m_SubCategory;
	private String m_Language;
	private Long m_Correct; // number of times user answered this question
							// correctly
	private Long m_Wrong; // number of times user answer this question wrong

	public Question(ContentValues i_Value) {

		m_Answers = new ArrayList<String>();

		m_QuestionId = i_Value.getAsString(TriviaDbEngine.KEY_QUESTIONID);
		m_Question = i_Value.getAsString(TriviaDbEngine.KEY_QUESTION);
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER1));
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER2));
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER3));
		m_Answers.add(i_Value.getAsString(TriviaDbEngine.KEY_ANSWER4));

		m_CorrectAnswerIndex = i_Value
				.getAsInteger(TriviaDbEngine.KEY_ANSWER_INDEX);

		m_Category = i_Value.getAsString(TriviaDbEngine.KEY_CATEGORY);

		m_Correct = i_Value.getAsLong(TriviaDbEngine.KEY_CORRECT_USER);
		m_Correct += i_Value.getAsLong(TriviaDbEngine.KEY_CORRECT_FROM_DB);

		m_Wrong = i_Value.getAsLong(TriviaDbEngine.KEY_WRONG_USER);
		m_Wrong += i_Value.getAsLong(TriviaDbEngine.KEY_WRONG_FROM_DB);

	}

	public int getQuestionDifficultyLevel() {
		// question difficulty is scaled between 1 - 10
		//
		// questions difficulty is measured by how much time the user was
		// correct divided by how much this question was answer
		// 
		double ret;
		double correct, wrong;

		correct = m_Correct.doubleValue();
		wrong = m_Wrong.doubleValue();

		ret = correct + wrong;

		// checking if this question was ever answered
		if (correct == 0 || wrong == 0) {
			if (wrong == 0) {
				ret = 1;

			} else {
				ret = 10;
			}
		} else {
			ret = wrong / ret;
		}
		
		return (int) (ret*10);

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

}
