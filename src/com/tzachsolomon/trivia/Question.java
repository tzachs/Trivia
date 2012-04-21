package com.tzachsolomon.trivia;

public class Question {
	
	private String m_Id;
	private String m_Question;
	private String m_Answer1;
	private String m_Answer2;
	private String m_Answer3;
	private String m_Answer4;
	private int m_CorrectAnswer;
	private String m_Category;
	private String m_SubCategory;
	private String m_Language;
	private long m_Correct; // number of times answered this question correctly
	private long m_Wrong; // number of times answer this question wrong
	
	public Question (){
		
		m_Id = "1";
		m_Question = "What is the answer to the universe?";
		m_Answer1 = "42";
		m_Answer2 = "Michel Jordan";
		m_Answer3 = "Money";
		m_Answer4 = "?";
		
		m_CorrectAnswer = 1;
		
		m_Category = "Litrature";
		
		m_Correct = 0;
		m_Wrong = 0;
		
	}

	public CharSequence getQuestion() {
		// 
		return m_Question;
	}

	public CharSequence getAnswer1() {
		// 
		return m_Answer1;
	}

	public CharSequence getAnswer2() {
		//
		return m_Answer2;
	}

	public CharSequence getAnswer3() {
		// 
		return m_Answer3;
	}
	
	public CharSequence getAnswer4() {
		// 
		return m_Answer4;
	}

	public boolean isCorrect(int i) {
		// 
		boolean ret = false;
		
		if ( i == m_CorrectAnswer ){
			ret = true;
		}
		
		return ret;
	}
	
	

}
