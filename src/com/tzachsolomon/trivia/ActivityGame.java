package com.tzachsolomon.trivia;

import java.util.ArrayList;
import java.util.Collections;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityGame extends Activity implements OnClickListener {

	public static final String TAG = ActivityGame.class.getSimpleName();

	public static final String EXTRA_GAME_TYPE = "GameType";
	public static final String EXTRA_GAME_CATEGORIES = "GameCategories";
	public static final String EXTRA_GAME_START_LEVEL = "GameStartLevel";
	
	public static final int GAMETYPE_ALL_QUESTIONS = 1;
	public static final int GAMETYPE_LEVELS = 2;
	public static final int GAMETYPE_CATEGORIES = 3;

	public static final String INTENT_EXTRA_PREVIOUS_QUESTION_ID = "previousQuestionId";
	public static final String INTENT_EXTRA_PREVIOUS_QUESTION_STRING = "previousQuestionString";
	public static final String INTENT_EXTRA_CURRENT_QUESTION_ID = "currentQuestionId";
	public static final String INTENT_EXTRA_CURRENT_QUESTION_STRING = "currentQuestionString";

	public static final String INTENT_EXTRA_GAME_TYPE = "keyGameType";

	private MyCountDownCounter m_CountDownCounter;

	private Button buttonAnswer1;
	private Button buttonAnswer2;
	private Button buttonAnswer3;
	private Button buttonAnswer4;
	private Button buttonReportMistakeInQuestion;
	private Button buttonPassQuestion;

	private TextView textViewTime;
	private TextView textViewQuestion;
	private TextView textViewNumberOfQuestionsLeft;
	private TextView textViewQuestionDifficulty;
	private TextView textViewLivesLeft;

	private ArrayList<Question> m_Questions;
	private Question m_CurrentQuestion;

	private int m_QuestionIndex;
	private int m_QuestionLength;

	private int m_DelayBetweenQuestions;
	private int m_CurrentGameType;
	private int m_CurrentQuestionInThisLevel;
	private int m_MaxNumberOfQuestionInLevel;
	private int m_NumberOfLevels;

	private int m_CurrentLevel;
	private int m_CurrentWrongAnswersCounter;
	private int m_MaxWrongAnswersAllowed;

	private int m_AllQuestionsLives;

	private TriviaDbEngine m_TriviaDb;
	private SharedPreferences m_SharedPreferences;

	private Random m_Random;

	private boolean m_GameOver;
	private boolean m_SortByNewQuestionFirst;
	private boolean m_ResumeClock;
	private boolean m_ShowCorrectAnswer;
	private boolean m_ReverseNumbersInQuestions;

	private Bundle m_Extras;

	private int[] m_Categories;

	private boolean m_ResumeFromHelp;

	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		m_Extras = getIntent().getExtras();
		m_CurrentGameType = m_Extras.getInt(ActivityGame.EXTRA_GAME_TYPE);
			
		showInstructions();
	}

	private void parseGameSetupAndStart() {
		//
		updateLivesTextView();

		switch (m_CurrentGameType) {
		case GAMETYPE_ALL_QUESTIONS:

			startGameAllQuestions();
			break;
		case GAMETYPE_CATEGORIES:
			startGameLevels();
			break;
		case GAMETYPE_LEVELS:
			startGameLevels();
			break;

		default:
			break;
		}

	}

	

	
	private void showInstructions() {
		//
		boolean showInstruction = true;

		Intent intent = new Intent(ActivityGame.this, ActivityHowToPlay.class);
		switch (m_CurrentGameType) {
		
		case ActivityGame.GAMETYPE_ALL_QUESTIONS:
			showInstruction = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceShowHelpAllQuestions", true);
			if (showInstruction) {
				intent.putExtra(
						ActivityHowToPlay.KEY_HOW_TO_PLAY_INSTRUCTIONS_MESSAGE,
						getString(R.string.howToPlayAllQuestions1)
								+ getString(R.string.howToPlayAllQuestions2)
								+ getString(R.string.howToPlayAllQuestions3)
								+ getString(R.string.howToPlayAllQuestions4));
				intent.putExtra(
						ActivityHowToPlay.KEY_HOW_TO_PLAY_INSTRUCTIONS_TITLE,
						getString(R.string.instructions));
				intent.putExtra(ActivityHowToPlay.KEY_HOW_TO_PLAY_TITLE,
						getString(R.string.howToPlayAllQuestionsTitle));

			}

			break;
		case ActivityGame.GAMETYPE_CATEGORIES:
		case ActivityGame.GAMETYPE_LEVELS:
			showInstruction = m_SharedPreferences.getBoolean(
					"checkBoxPreferenceShowHelpNewGame", true);
			if (showInstruction) {
				intent.putExtra(
						ActivityHowToPlay.KEY_HOW_TO_PLAY_INSTRUCTIONS_MESSAGE,
						getString(R.string.howToPlayNewGame1)
								+ getString(R.string.howToPlayNewGame2)
								+ getString(R.string.howToPlayNewGame3));
				intent.putExtra(
						ActivityHowToPlay.KEY_HOW_TO_PLAY_INSTRUCTIONS_TITLE,
						getString(R.string.instructions));
				intent.putExtra(ActivityHowToPlay.KEY_HOW_TO_PLAY_TITLE,
						getString(R.string.howToPlayNewGameTitle));
			}
			break;
		}

		if (showInstruction) {
			intent.putExtra(ActivityGame.INTENT_EXTRA_GAME_TYPE,
					m_CurrentGameType);
			m_ResumeFromHelp = true;
			startActivityForResult(intent, 1);
		} else {
			parseGameSetupAndStart();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//
		
		parseGameSetupAndStart();
	}

	private void startGameLevels() {
		//
		// checking if there are questions to be asked
		if (m_TriviaDb.isEmpty() == false) {
			
			if ( m_CurrentGameType == GAMETYPE_CATEGORIES){
				m_Categories = m_Extras.getIntArray(ActivityGame.EXTRA_GAME_CATEGORIES);
			}

			m_CurrentLevel = 0;
			m_NumberOfLevels = 10;
			m_MaxNumberOfQuestionInLevel = 10;
			m_MaxWrongAnswersAllowed = 3;
			m_CurrentWrongAnswersCounter = 0;

			m_SortByNewQuestionFirst = m_SharedPreferences.getBoolean(
					"checkBoxPreferencePreferNewQuestions", true);

			textViewLivesLeft
					.setText(getString(R.string.textViewLivesLeftText)
							+ (m_MaxWrongAnswersAllowed - m_CurrentWrongAnswersCounter));
			
			

			startNewRoundGameLevels();

		} else {
			Toast.makeText(this, getString(R.string.no_questions_in_database),
					Toast.LENGTH_SHORT).show();
			finish();

		}

	}

	private void startNewRoundGameLevels() {

		m_ResumeClock = false;

		m_CurrentLevel++;

		if (m_CurrentLevel <= m_NumberOfLevels && !m_GameOver) {

			if ( m_CurrentGameType == ActivityGame.GAMETYPE_LEVELS){
			m_Questions = m_TriviaDb.getQuestionByLevel(m_CurrentLevel,
					m_SortByNewQuestionFirst);
			}else if ( m_CurrentGameType == ActivityGame.GAMETYPE_CATEGORIES){
				m_Questions = m_TriviaDb.getQuestionByLevelAndCategories(m_CurrentLevel,
						m_SortByNewQuestionFirst,m_Categories);
			}

			m_QuestionLength = m_Questions.size();
			if (m_QuestionLength < 10) {
				m_MaxNumberOfQuestionInLevel = m_QuestionLength;
			} else {
				m_MaxNumberOfQuestionInLevel = 10;
			}

			m_QuestionIndex = 0;

			Collections.shuffle(m_Questions);

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					ActivityGame.this);

			alertDialog.setTitle(getString(R.string.starting_level_)
					+ m_CurrentLevel);
			alertDialog.setPositiveButton(getString(R.string.start),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//

							m_QuestionIndex = 0;
							m_CurrentQuestionInThisLevel = 0;

							new StartNewQuestionAsync().execute(0);

						}
					});
			alertDialog.setCancelable(false);

			alertDialog.show();

		} else {
			Toast.makeText(this, getString(R.string.levels_game_over),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void startGameAllQuestions() {
		//

		m_ResumeClock = false;
		m_AllQuestionsLives = 0;
		m_Questions = m_TriviaDb.getEnabledQuestions();

		// Shuffling the order of the questions
		Collections.shuffle(m_Questions);

		m_QuestionLength = m_Questions.size() - 1;

		// checking if there are questions to be asked
		if (m_Questions.size() > 0) {
			m_QuestionIndex = -1;

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					ActivityGame.this);

			alertDialog.setTitle("Start Game");
			alertDialog.setPositiveButton(getString(R.string.start),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//

							m_QuestionIndex = 0;
							m_CurrentQuestionInThisLevel = 0;

							new StartNewQuestionAsync().execute(0);

						}
					});
			alertDialog.setCancelable(false);

			alertDialog.show();

		} else {
			Toast.makeText(this, getString(R.string.no_questions_in_database),
					Toast.LENGTH_SHORT).show();
			finish();

		}

	}

	public class StartNewQuestionAsync extends AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			//

			startNewQuestion();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			//
			try {
				Thread.sleep(params[0]);
			} catch (InterruptedException e) {
				//
				e.printStackTrace();
			}
			return null;
		}

	}

	private void startNewQuestion() {

		// initialize button color to blue
		buttonAnswer1.setBackgroundResource(R.drawable.blue_button);
		buttonAnswer2.setBackgroundResource(R.drawable.blue_button);
		buttonAnswer3.setBackgroundResource(R.drawable.blue_button);
		buttonAnswer4.setBackgroundResource(R.drawable.blue_button);

		switch (m_CurrentGameType) {
		case GAMETYPE_ALL_QUESTIONS:
			startNewQuestionAllQuestions();
			break;
		case GAMETYPE_LEVELS:
			startNewQuestionLevels();
			break;
			
		case GAMETYPE_CATEGORIES:
			startNewQuestionLevels();
			break;

		default:
			break;
		}

	}


	private void startNewQuestionLevels() {
		//
		m_CurrentQuestionInThisLevel++;

		if (m_CurrentQuestionInThisLevel < m_MaxNumberOfQuestionInLevel) {

			// getting reference to the current question
			m_CurrentQuestion = getNextQuestionInLevel();

			// checking if we are out of questions for this level before the
			// level has ended

			if (m_CurrentQuestion == null) {
				// no more questions in this level
				// going to next level

				Toast.makeText(
						ActivityGame.this,
						getString(R.string.no_more_questions_in_this_level_going_to_next_level_),
						Toast.LENGTH_LONG).show();
				startNewRoundGameLevels();

			} else {
				textViewNumberOfQuestionsLeft.setText(Integer
						.toString(m_MaxNumberOfQuestionInLevel
								- m_CurrentQuestionInThisLevel));

				// setting question difficulty
				textViewQuestionDifficulty.setText(Integer
						.toString(m_CurrentQuestion.getQuestionLevel()));

				// randomize answer places (indices)
				m_CurrentQuestion.randomizeAnswerPlaces(m_Random);

				if (m_ReverseNumbersInQuestions) {
					textViewQuestion.setText(StringParser
							.reverseNumbersInString(m_CurrentQuestion
									.getQuestion()));
				} else {
					textViewQuestion.setText(m_CurrentQuestion.getQuestion());
				}
				buttonAnswer1.setText(m_CurrentQuestion.getAnswer1());
				buttonAnswer2.setText(m_CurrentQuestion.getAnswer2());
				buttonAnswer3.setText(m_CurrentQuestion.getAnswer3());
				buttonAnswer4.setText(m_CurrentQuestion.getAnswer4());
				
				m_CountDownCounter.start();

			}

		} else {
			// level ended
			startNewRoundGameLevels();

		}

	}

	private Question getNextQuestionInLevel() {

		Question ret = null;

		if (m_QuestionIndex < m_QuestionLength) {

			ret = m_Questions.get(m_QuestionIndex);

			m_QuestionIndex++;
		}

		return ret;
	}

	private void startNewQuestionAllQuestions() {

		// setting number of questions left, must be before m_QuestionIndex+=
		textViewNumberOfQuestionsLeft.setText(Integer.toString(m_QuestionLength
				- m_QuestionIndex));

		if (m_QuestionIndex < m_QuestionLength) {

			m_QuestionIndex++;

			// getting reference to the current question
			m_CurrentQuestion = m_Questions.get(m_QuestionIndex);

			// setting question difficulty
			textViewQuestionDifficulty.setText(Integer
					.toString(m_CurrentQuestion.getQuestionLevel()));

			// randomize answer places (indices)
			m_CurrentQuestion.randomizeAnswerPlaces(m_Random);

			textViewQuestion.setText(m_CurrentQuestion.getQuestion());
			buttonAnswer1.setText(m_CurrentQuestion.getAnswer1());
			buttonAnswer2.setText(m_CurrentQuestion.getAnswer2());
			buttonAnswer3.setText(m_CurrentQuestion.getAnswer3());
			buttonAnswer4.setText(m_CurrentQuestion.getAnswer4());

			m_CountDownCounter.start();

		}

	}

	private void initializeVariables() {
		//
		m_ReverseNumbersInQuestions = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceRevereseInHebrew", false);
		m_ShowCorrectAnswer = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceShowCorrectAnswer", true);
		m_ResumeClock = false;
		m_GameOver = false;
		m_TriviaDb = new TriviaDbEngine(ActivityGame.this);
		// m_Random = new Random(1);
		m_Random = new Random(System.currentTimeMillis());
		int total = 7;

		try {
			total = Integer.parseInt(m_SharedPreferences.getString(
					"editTextPreferenceCountDownTimer", "7"));
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage().toString());
		}
		try {
			m_DelayBetweenQuestions = Integer
					.parseInt(m_SharedPreferences.getString(
							"editTextPreferenceDelayBetweenQuestions", "500"));
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage().toString());
		}

		total *= 1000;

		m_CountDownCounter = new MyCountDownCounter(total, 1000);

		m_CurrentGameType = -1;

		initializeTextViews();

		initializeButtons();

	}

	private void initializeButtons() {

		buttonAnswer1 = (Button) findViewById(R.id.buttonAnswer1);
		buttonAnswer2 = (Button) findViewById(R.id.buttonAnswer2);
		buttonAnswer3 = (Button) findViewById(R.id.buttonAnswer3);
		buttonAnswer4 = (Button) findViewById(R.id.buttonAnswer4);
		buttonPassQuestion = (Button) findViewById(R.id.buttonPassQuestion);

		buttonReportMistakeInQuestion = (Button) findViewById(R.id.buttonReportMistakeInQuestion);

		buttonAnswer1.setOnClickListener(this);
		buttonAnswer2.setOnClickListener(this);
		buttonAnswer3.setOnClickListener(this);
		buttonAnswer4.setOnClickListener(this);

		buttonReportMistakeInQuestion.setOnClickListener(this);
		buttonPassQuestion.setOnClickListener(this);

	}

	private void initializeTextViews() {
		textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
		textViewTime = (TextView) findViewById(R.id.textViewTime);
		textViewNumberOfQuestionsLeft = (TextView) findViewById(R.id.textViewNumberOfQuestionsLeft);
		textViewQuestionDifficulty = (TextView) findViewById(R.id.textViewQuestionDifficulty);
		textViewLivesLeft = (TextView) findViewById(R.id.textViewLivesLeft);
	}

	public class MyCountDownCounter extends CountDownTimerWithPause {

		public MyCountDownCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			//
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//
			textViewTime.setText(Long.toString(millisUntilFinished / 1000));

		}

		@Override
		public void onFinish() {
			//
			checkAnswer(-1, null);
		}

	}

	@Override
	public void onClick(View v) {
		//

		switch (v.getId()) {

		case R.id.buttonPassQuestion:
			buttonPassQuestion_Clicked();
			break;

		case R.id.buttonReportMistakeInQuestion:
			buttonReportMistakeInQuestion_Clicked();
			break;

		case R.id.buttonAnswer1:
			checkAnswer(1, buttonAnswer1);
			break;

		case R.id.buttonAnswer2:
			checkAnswer(2, buttonAnswer2);
			break;

		case R.id.buttonAnswer3:
			checkAnswer(3, buttonAnswer3);
			break;

		case R.id.buttonAnswer4:
			checkAnswer(4, buttonAnswer4);
			break;

		default:
			break;
		}

	}

	private void buttonPassQuestion_Clicked() {
		//
		checkAnswer(-2, null);

	}

	private void buttonReportMistakeInQuestion_Clicked() {

		Intent intent = new Intent(this, ActivityReportErrorInQuestion.class);
		if (m_QuestionIndex > 1) {

			intent.putExtra(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_ID,
					m_Questions.get(m_QuestionIndex - 2).getQuestionId());
			if (m_ReverseNumbersInQuestions) {
				intent.putExtra(
						ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
						StringParser.reverseNumbersInString(m_Questions.get(
								m_QuestionIndex - 2).getQuestion()));
			} else {
				intent.putExtra(
						ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
						m_Questions.get(m_QuestionIndex - 2).getQuestion());
			}
		} else {
			intent.putExtra(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_ID, "-1");
			intent.putExtra(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
					"bla bla");
		}
		intent.putExtra(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_ID,
				m_CurrentQuestion.getQuestionId());
		if (m_ReverseNumbersInQuestions) {
			intent.putExtra(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_STRING,
					StringParser.reverseNumbersInString(m_CurrentQuestion
							.getQuestion()));
		} else {
			intent.putExtra(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_STRING,
					m_CurrentQuestion.getQuestion());
		}

		startActivityForResult(intent, -1);

	}

	private void setButtonRed(Button o_Button) {
		o_Button.setBackgroundResource(R.drawable.red_button);
	}

	private void setButtonGreen(Button o_Button) {
		o_Button.setBackgroundResource(R.drawable.green_button);
	}

	private int checkAnswer(int i, Button o_Button) {
		//
		StringBuilder sb = new StringBuilder();
		int ret = 1;
		int correctAnswerIndex;

		// stopping the counter in order to create a race condition where the
		// user already click but the timer
		// is still running

		stopCountdownCounter();

		// checking if time is up
		if (i == -1) {
			ret = -1;
			sb.append("Time is up!");
			incCurrentWrongAnswersCounter();

		} else if (i == -2) {
			// if pass question pressed
		} else {

			correctAnswerIndex = m_CurrentQuestion.getCorrectAnswerIndex();

			if (i == correctAnswerIndex) {

				ret = 0;
				setButtonGreen(o_Button);

				m_TriviaDb.incUserCorrectCounter(m_CurrentQuestion
						.getQuestionId());

				// if all questions game, inc lives
				incAllQuestionsLives();

			} else {
				setButtonRed(o_Button);
				m_TriviaDb.incUserWrongCounter(m_CurrentQuestion
						.getQuestionId());
				incCurrentWrongAnswersCounter();

				// checking if the user answer wrong and we need to show the
				// correct answer
				if (m_ShowCorrectAnswer) {
					setButtonGreen(m_CurrentQuestion.getCorrectAnswerIndex());
				}
			}

		}

		updateLivesTextView();

		if (m_QuestionIndex < m_QuestionLength && !m_GameOver) {
			// start a new question and
			new StartNewQuestionAsync().execute(m_DelayBetweenQuestions);

		} else {
			finishedQuestions();
		}

		if (sb.length() > 0) {
			Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
			sb.setLength(0);
		}

		return ret;

	}

	private void incAllQuestionsLives() {
		//
		m_AllQuestionsLives += m_CurrentQuestion.getQuestionLevel();
				

	}

	private void setButtonGreen(int correctAnswerIndex) {
		//
		switch (correctAnswerIndex) {
		case 1:
			setButtonGreen(buttonAnswer1);
			break;
		case 2:
			setButtonGreen(buttonAnswer2);
			break;
		case 3:
			setButtonGreen(buttonAnswer3);
			break;
		case 4:
			setButtonGreen(buttonAnswer4);
			break;

		default:
			break;
		}

	}

	private void finishedQuestions() {
		//
		switch (m_CurrentGameType) {
		case GAMETYPE_ALL_QUESTIONS:
			Toast.makeText(ActivityGame.this, getString(R.string.game_over),
					Toast.LENGTH_LONG).show();
			break;

		case GAMETYPE_LEVELS:
			startNewRoundGameLevels();
			break;

		default:
			break;
		}

	}

	private void incCurrentWrongAnswersCounter() {
		//
		switch (m_CurrentGameType) {
		
		case GAMETYPE_ALL_QUESTIONS:
			incCurrentWrongAnswersCounter_AllQuestions();
			break;
		
		case GAMETYPE_CATEGORIES:	
		case GAMETYPE_LEVELS:
			incCurrentWrongAnswersCounter_GameLevels();
			break;
		}

	}

	private void showGameOver() {
		m_GameOver = true;
		m_CountDownCounter.cancel();
		AlertDialog.Builder gameOverDialog = new AlertDialog.Builder(
				ActivityGame.this);
		gameOverDialog.setTitle("Game over :(");
		gameOverDialog.setCancelable(false);
		gameOverDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						finish();
					}
				});
		gameOverDialog.show();
	}

	private void updateLivesTextView() {
		switch (m_CurrentGameType) {



		case GAMETYPE_ALL_QUESTIONS:
			if (m_ReverseNumbersInQuestions) {
				textViewLivesLeft.setText(StringParser.reverseNumbersInString(getString(R.string.textViewLivesLeftText)
						+ m_AllQuestionsLives));
				
			} else {
				textViewLivesLeft.setText(getString(R.string.textViewLivesLeftText)
						+ m_AllQuestionsLives);
				
			}
			
			
			break;


		case GAMETYPE_CATEGORIES:
		case GAMETYPE_LEVELS:

			textViewLivesLeft
					.setText(getString(R.string.textViewLivesLeftText)
							+ (m_MaxWrongAnswersAllowed - m_CurrentWrongAnswersCounter));

			break;

		default:
			break;
		}
	}

	private void incCurrentWrongAnswersCounter_GameLevels() {
		//
		m_CurrentWrongAnswersCounter++;

		if (m_CurrentWrongAnswersCounter >= m_MaxWrongAnswersAllowed) {
			showGameOver();

		}

	}

	private void incCurrentWrongAnswersCounter_AllQuestions() {
		//
		m_AllQuestionsLives -= m_CurrentQuestion.getQuestionLevel();
				

		if (m_AllQuestionsLives < 0) {
			showGameOver();
		}

	}

	private void stopCountdownCounter() {
		//
		m_CountDownCounter.cancel();
	}

	@Override
	protected void onResume() {
		//
		super.onResume();

		// checking if to show the report question button
		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceShowReportQuestion", false)) {

			buttonReportMistakeInQuestion.setVisibility(View.VISIBLE);
		} else {
			buttonReportMistakeInQuestion.setVisibility(View.GONE);
		}
		
		if ( m_ResumeFromHelp ){
			m_ResumeClock = false;
			m_ResumeFromHelp = false;
		}

		if (m_ResumeClock) {
			m_CountDownCounter.resume();
		}
		
	

	}

	@Override
	protected void onPause() {
		//
		super.onPause();

		m_CountDownCounter.pause();
		m_ResumeClock = true;

	}

}
