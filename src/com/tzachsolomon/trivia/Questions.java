package com.tzachsolomon.trivia;

import java.util.ArrayList;
import java.util.Collections;

public class Questions {

	// holds the questions
	public ArrayList<Question> m_Questions;
	// holds the regions for how many times played
	public ArrayList<Integer> m_RegionsHowManyTimesPlayed;
	public int m_QuestionIndex;

	public Questions() {
		m_Questions = new ArrayList<Question>();
		m_RegionsHowManyTimesPlayed = new ArrayList<Integer>();
		m_QuestionIndex = 0;
	}

	public void addNewQuestion(Question question) {
		//
		m_Questions.add(question);

	}

	private void calculateRegionsTimesPlayed() {
		int length = m_Questions.size() - 1;
		int i = 1;
		int timesPlayed;

		if (length > 2) {

			timesPlayed = m_Questions.get(0).getQuestionTimesPlayed();
			
			
			while (i < length) {
				if (timesPlayed != m_Questions.get(i).getQuestionTimesPlayed()) {
					m_RegionsHowManyTimesPlayed.add(i);
				}

				i--;
			}
		}

	}

	public int getNumberOfQustions() {
		//
		return m_Questions.size();
	}

	public void shuffle(boolean m_SortByNewQuestionFirst) {
		//
		// TODO: if m_SortByNew is true, sort with regions
		if (m_SortByNewQuestionFirst) {
			calculateRegionsTimesPlayed();
			myCollectionShuffle();
			Collections.shuffle(m_Questions);
		} else {
			Collections.shuffle(m_Questions);
		}
	}

	private void myCollectionShuffle() {
		// TODO Auto-generated method stub
		
	}

	public Question getQuestionAtIndex(int m_QuestionIndex) {
		//
		return m_Questions.get(m_QuestionIndex);
	}

}
