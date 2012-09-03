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

	private static final int SKIP_QUESTION_DONT_SKIP = 0;
	private static final int SKIP_QUESTION_DUE_TO_ERROR = 1;
	private static final int SKIP_QUESTION_DUE_LAST_UPDATE = 2;

	private StringBuilder currentString;

	private XmlDataHandlerQuestionListener m_Listener;

	private int m_SkipQuestion;

	private long m_LastUpdate;

	public XmlDataHandlerQuestions(long i_LastUpdate) {
		m_LastUpdate = i_LastUpdate;
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

				switch (m_SkipQuestion) {
				case SKIP_QUESTION_DONT_SKIP:
					m_Questions.add(m_Question);
					break;
				case SKIP_QUESTION_DUE_LAST_UPDATE:
					break;
				case SKIP_QUESTION_DUE_TO_ERROR:
					// error was detected, checking if we need to send event
					if (m_Listener != null) {
						m_Listener.errorQuestionParsingDetected(m_Question);
					}
					break;
				default:
					break;
				}

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
					m_SkipQuestion = SKIP_QUESTION_DONT_SKIP;

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

		try {
			String chars = new String(ch, start, length);
			// Log.v(TAG, chars);

			if (inColAnswer1) {

				// only \n means the question parser ended, this section can be
				// called multiple times if
				// there are special characters such as &quot; that is why we
				// need to check \n
				if (chars.contentEquals("\n")) {

					inColAnswer1 = false;
					m_Question.put(TriviaDbEngine.KEY_ANSWER1,
							currentString.toString());
					currentString = null;

				} else {
					// checking if this is the start of t he question, if so,
					// create the StringBuilder
					if (currentString == null) {
						currentString = new StringBuilder();
					}
					// append the current part
					currentString.append(chars);
				}

			} else if (inColAnswer2) {
				// only \n means the question parser ended, this section can be
				// called multiple times if
				// there are special characters such as &quot; that is why we
				// need
				// to check \n
				if (chars.contentEquals("\n")) {

					inColAnswer2 = false;
					m_Question.put(TriviaDbEngine.KEY_ANSWER2,
							currentString.toString());
					currentString = null;

				} else {
					// checking if this is the start of the question, if so,
					// create
					// the StringBuilder
					if (currentString == null) {
						currentString = new StringBuilder();
					}
					// append the current part
					currentString.append(chars);
				}

			} else if (inColAnswer3) {
				// only \n means the question parser ended, this section can be
				// called multiple times if
				// there are special characters such as &quot; that is why we
				// need
				// to check \n
				if (chars.contentEquals("\n")) {

					inColAnswer3 = false;
					m_Question.put(TriviaDbEngine.KEY_ANSWER3,
							currentString.toString());
					currentString = null;

				} else {
					// checking if this is the start of the question, if so,
					// create
					// the StringBuilder
					if (currentString == null) {
						currentString = new StringBuilder();
					}
					// append the current part
					currentString.append(chars);
				}

			} else if (inColAnswer4) {

				// only \n means the question parser ended, this section can be
				// called multiple times if
				// there are special characters such as &quot; that is why we
				// need
				// to check \n
				if (chars.contentEquals("\n")) {

					inColAnswer4 = false;
					m_Question.put(TriviaDbEngine.KEY_ANSWER4,
							currentString.toString());
					currentString = null;

				} else {
					// checking if this is the start of the question, if so,
					// create
					// the StringBuilder
					if (currentString == null) {
						currentString = new StringBuilder();
					}
					// append the current part
					currentString.append(chars);
				}

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
				if (m_LastUpdate < Long.parseLong(chars)) {
					m_Question.put(TriviaDbEngine.KEY_LAST_UPDATE, chars);
				}else{
					m_SkipQuestion = SKIP_QUESTION_DUE_LAST_UPDATE;
				}
				
				inColLastUpdate = false;
			} else if (inColQuestion) {

				// only \n means the question parser ended, this section can be
				// called multiple times if
				// there are special characters such as &quot; that is why we
				// need
				// to check \n
				if (chars.contentEquals("\n")) {
					m_Question.put(TriviaDbEngine.KEY_QUESTION,
							currentString.toString());
					currentString = null;
					inColQuestion = false;
				} else {
					// checking if this is the start of the question, if so,
					// create
					// the StringBuilder
					if (currentString == null) {
						currentString = new StringBuilder();
					}
					// append the current part
					currentString.append(chars);
				}

			} else if (inColQuestionId) {
				m_Question.put(TriviaDbEngine.KEY_QUESTIONID, chars);
				inColQuestionId = false;
			} else if (inColWrong) {
				inColWrong = false;
			}
		} catch (NullPointerException e) {
			// there is an error parsing the question
			m_SkipQuestion = SKIP_QUESTION_DUE_TO_ERROR;
			// Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			m_SkipQuestion = SKIP_QUESTION_DUE_TO_ERROR;
			// Log.e(TAG, e.getMessage());
		}
	}

	public static interface XmlDataHandlerQuestionListener {
		public void onEndDocument();

		public void onStartDocument();

		public void errorQuestionParsingDetected(ContentValues i_Question);
	}

	public void setXmlDataHandlerListener(
			XmlDataHandlerQuestionListener i_Listener) {
		m_Listener = i_Listener;
	}

}
