package com.tzachsolomon.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class Questions {

	private static final int SHUFFLE_THRESHOLD = 5;
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

		// checking if we have at least 2 questions
		if (length > 2) {

			// getting how many times the first questions was played
			timesPlayed = m_Questions.get(0).getQuestionTimesPlayed();

			while (i <= length) {
				// checking if the current question and the next question have been played the same times
				if (timesPlayed != m_Questions.get(i).getQuestionTimesPlayed()) {
					// if not then we we found a new region
					m_RegionsHowManyTimesPlayed.add(i);
					timesPlayed = m_Questions.get(i).getQuestionTimesPlayed();
				}

				i++;
			}
			
		}
		
		if ( m_RegionsHowManyTimesPlayed.size() == 0 ){
			m_RegionsHowManyTimesPlayed.add(length);
		}

	}

	public int getNumberOfQustions() {
		//
		return m_Questions.size();
	}

	public void shuffle(boolean m_SortByNewQuestionFirst) {
		//
		// 
		int start, end, i;
		
		// checking if to sort by least played questions
		// 
		if (m_SortByNewQuestionFirst) {
			// calculating the regions of how many times each question was played
			// then, shuffling only the questions in those regions
			calculateRegionsTimesPlayed();
			i = 0;
			start = 0;
			
			
			for ( i = 0; i < m_RegionsHowManyTimesPlayed.size(); i++ ){
				end = m_RegionsHowManyTimesPlayed.get(i);
				myShuffle(m_Questions,start,end);
				start = end;
				
			}
			
		} else {
			Collections.shuffle(m_Questions);
		}
	}

	private void myShuffle(List<Question> list, int i_Start, int i_End) {
		Random rnd = new Random();
		int size = i_End - i_Start;
		if (size < SHUFFLE_THRESHOLD || list instanceof RemoteAccess) {
			for (int i = size; i > i_Start; i--)
				swap(list, i - 1, rnd.nextInt(i));
		} else {
			Object arr[] = list.toArray();

			// SHuffle Array
			for (int i = size; i > i_Start; i--)
				swap(arr, i - 1, rnd.nextInt(i));

			// Dump array back into list
			ListIterator it = list.listIterator();
			for (int i = 0; i < arr.length; i++) {
				it.next();
				it.set(arr[i]);
			}

		}

	}

	private void swap(Object[] arr, int i, int j) {
		// 
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
		
	}

	private void swap(List<Question> list, int i, int j) {
		//

		final List l = list;
		l.set(i, l.set(j, l.get(i)));

	}

	public Question getQuestionAtIndex(int m_QuestionIndex) {
		//
		return m_Questions.get(m_QuestionIndex);
	}
	
	public interface RemoteAccess { 
	}
	

}
