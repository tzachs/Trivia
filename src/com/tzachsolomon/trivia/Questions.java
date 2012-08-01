package com.tzachsolomon.trivia;

import java.util.ArrayList;
import java.util.Collections;

public class Questions {

	// holds the questions
	public ArrayList<Question> m_Questions;
	// holds the regions for how many times played
	public ArrayList<Integer> m_RegionsHowManyTimesPlayed;

	public Questions() {
		m_Questions = new ArrayList<Question>();
		m_RegionsHowManyTimesPlayed = new ArrayList<Integer>();
	}

	public void addNewQuestion(Question question) {
		//
		m_Questions.add(question);

	}

	public int getNumberOfQustions() {
		// 
		return m_Questions.size();
	}

	public void shuffle(boolean m_SortByNewQuestionFirst) {
		// 
		// TODO: if m_SortByNew is true, sort with regions
		Collections.shuffle(m_Questions);
	}

	public Question getQuestionAtIndex(int m_QuestionIndex) {
		// 
		return m_Questions.get(m_QuestionIndex);
	}

}
