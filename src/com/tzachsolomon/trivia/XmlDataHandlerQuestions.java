package com.tzachsolomon.trivia;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.util.Log;

public class XmlDataHandlerQuestions extends DefaultHandler {

	public static final String TAG = XmlDataHandlerQuestions.class
			.getSimpleName();

	private boolean inQuestionsData;

	private boolean inQuestionsDataRow;

	ArrayList<ContentValues> m_Questions;
	ContentValues m_Question;
	private boolean inColAnswer1;
	private boolean inColAnswer2;
	private boolean inColAnswer3;
	private boolean inColAnswer4;
	private boolean inColAnswerIndex;
	private boolean inColCategory;
	private boolean inColLanguage;
	private boolean inColCorrect;
	private boolean inColWrong;
	private boolean inColCorrectWrongRatio;
	private boolean inColLastUpdate;
	private boolean inColEnabled;
	private boolean inColQuestionId;
	private boolean inColQuestion;
	
	private StringBuilder currentQuestion;

	private XmlDataHandlerListener m_Listener;

	public XmlDataHandlerQuestions() {
		m_Questions = new ArrayList<ContentValues>();
	}

	public ContentValues[] getQuestions() {
		//
		ContentValues[] ret = new ContentValues[m_Questions.size()];

		m_Questions.toArray(ret);

		return ret;
	}

	@Override
	public void startDocument() throws SAXException {
		//
		super.startDocument();
		if (m_Listener != null) {
			m_Listener.onStartDocument();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		//
		super.endDocument();
		if (m_Listener != null) {
			m_Listener.onEndDocument();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		//
		if (localName.contentEquals("table_data")) {

			inQuestionsData = false;

		} else if (localName.contentEquals("row")) {

			if (inQuestionsDataRow) {
				m_Questions.add(m_Question);
			}

			setInColFalse();

		}
	}

	private void setInColFalse() {

		inQuestionsDataRow = false;
		inColAnswer1 = false;
		inColAnswer2 = false;
		inColAnswer3 = false;
		inColAnswer4 = false;
		inColAnswerIndex = false;
		inColCategory = false;
		inColCorrect = false;
		inColCorrectWrongRatio = false;
		inColEnabled = false;
		inColLanguage = false;
		inColLastUpdate = false;
		inColQuestion = false;
		inColQuestionId = false;
		inColWrong = false;

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		//
		// Log.v(TAG, "start element " + localName);
		try {
			if (localName.contentEquals("table_data")) {

				inQuestionsData = false;

				if (attributes.getValue(0).contentEquals("questions")) {
					inQuestionsData = true;
				}
			} else if (localName.contentEquals("row")) {

				inQuestionsDataRow = false;

				setInColFalse();

				if (inQuestionsData) {
					inQuestionsDataRow = true;
					m_Question = new ContentValues();
				}
			} else if (localName.contentEquals("field")) {
				if (inQuestionsDataRow) {

					if (attributes.getValue(0).contentEquals("colQuestionId")) {
						inColQuestionId = true;

					} else if (attributes.getValue(0).contentEquals(
							"colQuestion")) {

						inColQuestion = true;

					}

					else if (attributes.getValue(0).contentEquals("colAnswer1")) {

						inColAnswer1 = true;

					} else if (attributes.getValue(0).contentEquals(
							"colAnswer2")) {

						inColAnswer2 = true;

					} else if (attributes.getValue(0).contentEquals(
							"colAnswer3")) {

						inColAnswer3 = true;

					} else if (attributes.getValue(0).contentEquals(
							"colAnswer4")) {
						//
						inColAnswer4 = true;

					} else if (attributes.getValue(0).contentEquals(
							"colAnswerIndex")) {

						inColAnswerIndex = true;

					} else if (attributes.getValue(0).contentEquals(
							"colCategory")) {
						inColCategory = true;

					} else if (attributes.getValue(0).contentEquals(
							"colLanguage")) {
						inColLanguage = true;

					} else if (attributes.getValue(0).contentEquals(
							"colCorrect")) {
						inColCorrect = true;

					} else if (attributes.getValue(0).contentEquals("colWrong")) {
						inColWrong = true;

					} else if (attributes.getValue(0).contentEquals(
							"colCorrectWrongRatio")) {

						inColCorrectWrongRatio = true;

					} else if (attributes.getValue(0).contentEquals(
							"colLastUpdate")) {

						inColLastUpdate = true;

					} else if (attributes.getValue(0).contentEquals(
							"colEnabled")) {
						inColEnabled = true;
					}

				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage().toString());
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		//

		String chars = new String(ch, start, length);
		// replacing the &quot; with "

		

		if (inColAnswer1) {
			// TODO: same solution of question with quotation mark
			m_Question.put(TriviaDbEngine.KEY_ANSWER1, chars);
			inColAnswer1 = false;
		} else if (inColAnswer2) {
			m_Question.put(TriviaDbEngine.KEY_ANSWER2, chars);
			inColAnswer2 = false;
		} else if (inColAnswer3) {
			m_Question.put(TriviaDbEngine.KEY_ANSWER3, chars);
			inColAnswer3 = false;
		} else if (inColAnswer4) {
			m_Question.put(TriviaDbEngine.KEY_ANSWER4, chars);
			inColAnswer4 = false;

		} else if (inColAnswerIndex) {
			m_Question.put(TriviaDbEngine.KEY_ANSWER_INDEX, chars);
			inColAnswerIndex = false;
		} else if (inColCategory) {
			m_Question.put(TriviaDbEngine.KEY_CATEGORY, chars);
			inColCategory = false;
		} else if (inColCorrect) {

			inColCorrect = false;
		} else if (inColCorrectWrongRatio) {
			m_Question.put(TriviaDbEngine.KEY_CORRECT_WRONG_RATIO, chars);
			inColCorrectWrongRatio = false;
		} else if (inColEnabled) {
			m_Question.put(TriviaDbEngine.KEY_ENABLED, chars);
			inColEnabled = false;
		} else if (inColLanguage) {
			m_Question.put(TriviaDbEngine.KEY_LANGUAGE, chars);
			inColLanguage = false;
		} else if (inColLastUpdate) {
			m_Question.put(TriviaDbEngine.KEY_LAST_UPDATE, chars);
			inColLastUpdate = false;
		} else if (inColQuestion) {
			
			// only \n means the question parser ended, this section can be called multiple times if
			// there are special characters such as &quot; that is why we need to check \n
			if ( chars.contentEquals("\n")){
				m_Question.put(TriviaDbEngine.KEY_QUESTION, currentQuestion.toString());
				currentQuestion = null;
				inColQuestion = false;
			} else{
				// checking if this is the start of the question, if so, create the StringBuilder
				if ( currentQuestion == null ){
					currentQuestion = new StringBuilder();
				}
				// append the current part
				currentQuestion.append(chars);
			}
			
			
		} else if (inColQuestionId) {
			m_Question.put(TriviaDbEngine.KEY_QUESTIONID, chars);
			inColQuestionId = false;
		} else if (inColWrong) {
			inColWrong = false;
		}

	}

	public static interface XmlDataHandlerListener {
		public void onEndDocument();

		public void onStartDocument();
	}

	public void setXmlDataHandlerListener(XmlDataHandlerListener i_Listener) {
		m_Listener = i_Listener;
	}

}
