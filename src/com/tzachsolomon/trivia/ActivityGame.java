package com.tzachsolomon.trivia;

import java.io.IOException;
import java.util.ArrayList;


import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

public class ActivityGame extends Activity implements OnClickListener, ViewFactory {

	public static final String TAG = ActivityGame.class.getSimpleName();

	public static final String EXTRA_GAME_TYPE = "GameType";
	public static final String EXTRA_GAME_CATEGORIES = "GameCategories";
	public static final String EXTRA_GAME_START_LEVEL = "GameStartLevel";
	public static final String EXTRA_GAME_USER_ID = "GameUserId";

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


	private TextView textViewQuestion;
	private TextView textViewNumberOfQuestionsLeft;
	private TextView textViewQuestionLevel;
	private TextView textViewLivesLeftValue;

	private Questions m_Questions;
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

	private StringParser m_StringParser;

	private int[] m_QuestionLanguages;

	private SoundPool m_SoundPool;

	// each of the sounds is initialize with -1 i order to prevent a state where
	// we try
	// to play and the sound was not yet loaded.
	private int m_SoundAnswerCorrect = -1;
	private int m_SoundAnswerWrong = -1;

	private boolean m_SoundEnabled;
	private MediaPlayer m_ClockSound;

	private TextView textViewTimesPlayedTitle;

	private int m_GameScore;

	public long m_MillisUntilFinished;

	private int m_TimeToAnswerQuestion;

	private TextView textViewGameScoreText;

	private int m_UserId;

	private TextSwitcher textSwitcherTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		m_ClockSound = MediaPlayer.create(this, R.raw.clock);
		m_ClockSound.setLooping(true);

		m_Extras = getIntent().getExtras();
		m_CurrentGameType = m_Extras.getInt(ActivityGame.EXTRA_GAME_TYPE);
		m_UserId = m_Extras.getInt(ActivityGame.EXTRA_GAME_USER_ID);
		

		showInstructions();
	}

	private void restartGame() {
		initializeVariables();

		m_Extras = getIntent().getExtras();
		m_CurrentGameType = m_Extras.getInt(ActivityGame.EXTRA_GAME_TYPE);

		parseGameSetupAndStart();
	}

	private void parseGameSetupAndStart() {
		//
		updateLivesTextView();
		m_GameScore = 0;
		textViewGameScoreText.setText("0");

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
						getString(R.string.howToPlayAllQuestions1) + "\n"
								+ getString(R.string.howToPlayAllQuestions2)
								+ "\n"
								+ getString(R.string.howToPlayAllQuestions3)
								+ "\n"
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
						getString(R.string.howToPlayNewGame1) + "\n"
								+ getString(R.string.howToPlayNewGame2) + "\n"
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

			if (m_CurrentGameType == GAMETYPE_CATEGORIES) {
				m_Categories = m_Extras
						.getIntArray(ActivityGame.EXTRA_GAME_CATEGORIES);
			}

			m_CurrentLevel = 0;
			m_NumberOfLevels = 10;
			m_MaxNumberOfQuestionInLevel = 10;
			m_MaxWrongAnswersAllowed = 3;
			m_CurrentWrongAnswersCounter = 0;

			m_SortByNewQuestionFirst = m_SharedPreferences.getBoolean(
					"checkBoxPreferencePreferNewQuestions", true);

			textViewLivesLeftValue
					.setText(""	+ (m_MaxWrongAnswersAllowed - m_CurrentWrongAnswersCounter));

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

			if (m_CurrentGameType == ActivityGame.GAMETYPE_LEVELS) {
				m_Questions = m_TriviaDb.getQuestionsByLevel(m_CurrentLevel,
						m_SortByNewQuestionFirst, m_QuestionLanguages);
			} else if (m_CurrentGameType == ActivityGame.GAMETYPE_CATEGORIES) {
				m_Questions = m_TriviaDb.getQuestionsByLevelAndCategories(
						m_CurrentLevel, m_SortByNewQuestionFirst, m_Categories,
						m_QuestionLanguages);
			}

			m_QuestionLength = m_Questions.getNumberOfQustions();
			if (m_QuestionLength < 10) {
				m_MaxNumberOfQuestionInLevel = m_QuestionLength;
			} else {
				m_MaxNumberOfQuestionInLevel = 10;
			}

			m_QuestionIndex = 0;

			m_Questions.shuffle(m_SortByNewQuestionFirst);

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
		m_Questions = m_TriviaDb.getQuestionsEnabled(m_SortByNewQuestionFirst,
				m_QuestionLanguages);

		// Shuffling the order of the questions
		m_Questions.shuffle(m_SortByNewQuestionFirst);

		m_QuestionLength = m_Questions.getNumberOfQustions() - 1;

		// checking if there are questions to be asked
		if (m_Questions.getNumberOfQustions() > 0) {
			m_QuestionIndex = -1;

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					ActivityGame.this);

			alertDialog.setTitle(getString(R.string.start_game));
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
		resetAnswerButtonBackground(R.drawable.blue_button);
		enableAnswerButtons();

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

	private void resetAnswerButtonBackground(int blueButton) {
		//
		buttonAnswer1.setBackgroundResource(blueButton);
		buttonAnswer2.setBackgroundResource(blueButton);
		buttonAnswer3.setBackgroundResource(blueButton);
		buttonAnswer4.setBackgroundResource(blueButton);

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

				// initialize common text views
				initializeQuestionTextViews();

				startOrResumeCountDownTimer(true);

			}

		} else {
			// level ended
			startNewRoundGameLevels();

		}

	}

	private void initializeQuestionTextViews() {
		//

		if (m_ReverseNumbersInQuestions) {
			textViewQuestion.setText(m_StringParser
					.reverseNumbersInStringHebrew(m_CurrentQuestion
							.getQuestion()));

			textViewTimesPlayedTitle
					.setText(m_StringParser
							.reverseNumbersInStringHebrew(getString(R.string.textViewTimesPlayedTitleText)
									+ m_CurrentQuestion
											.getQuestionTimesPlayed()));
		} else {
			textViewQuestion.setText(m_CurrentQuestion.getQuestion());
			textViewTimesPlayedTitle
					.setText(getString(R.string.textViewTimesPlayedTitleText)
							+ " " + m_CurrentQuestion.getQuestionTimesPlayed());
		}
		// setting question difficulty
		textViewQuestionLevel.setText(Integer.toString(m_CurrentQuestion
				.getQuestionLevel()));

		// randomize answer places (indices)
		m_CurrentQuestion.randomizeAnswerPlaces(m_Random);

		buttonAnswer1.setText(m_CurrentQuestion.getAnswer1());
		buttonAnswer2.setText(m_CurrentQuestion.getAnswer2());
		buttonAnswer3.setText(m_CurrentQuestion.getAnswer3());
		buttonAnswer4.setText(m_CurrentQuestion.getAnswer4());

	}

	private void startOrResumeCountDownTimer(boolean i_Start) {
		//
		if (i_Start) {
			m_CountDownCounter.start();
		} else {
			m_CountDownCounter.resume();
		}
		if (m_SoundEnabled) {
			m_ClockSound.start();
		}

	}

	private Question getNextQuestionInLevel() {

		Question ret = null;

		if (m_QuestionIndex < m_QuestionLength) {

			ret = m_Questions.getQuestionAtIndex(m_QuestionIndex);

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
			m_CurrentQuestion = m_Questions.getQuestionAtIndex(m_QuestionIndex);

			initializeQuestionTextViews();

			startOrResumeCountDownTimer(true);

		}

	}

	private void initializeVariables() {
		//
		// initialize the sounds

		m_UserId = -1;
		
		m_SoundEnabled = m_SharedPreferences.getBoolean(
				"checkBoxPreferencePlayGameSounds", true);

		m_StringParser = new StringParser(m_SharedPreferences);
		m_ReverseNumbersInQuestions = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceRevereseInHebrew", false);
		m_ShowCorrectAnswer = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceShowCorrectAnswer", true);
		m_ResumeClock = false;
		m_GameOver = false;
		m_TriviaDb = new TriviaDbEngine(ActivityGame.this);
		// m_Random = new Random(1);
		m_Random = new Random(System.currentTimeMillis());
		m_TimeToAnswerQuestion = 10;

		try {
			m_TimeToAnswerQuestion = Integer.parseInt(m_SharedPreferences
					.getString("editTextPreferenceCountDownTimer", "10"));
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

		m_TimeToAnswerQuestion *= 1000;

		m_CountDownCounter = new MyCountDownCounter(m_TimeToAnswerQuestion,
				1000);

		m_CurrentGameType = -1;

		initializeQuestionsLanguages();

		initializeTextViews();

		initializeButtons();
		
		

	}

	private void initializeQuestionsLanguages() {
		//
		boolean hebrew = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceQuestionLanguageHebrew", true);
		boolean english = m_SharedPreferences.getBoolean(
				"checkBOxPreferenceQuestionLanguageEnglish", true);
		ArrayList<Integer> array = new ArrayList<Integer>();
		int i;

		// TODO: change this to add from the database

		if (hebrew) {
			array.add(2);
		}

		if (english) {
			array.add(1);
		}

		m_QuestionLanguages = new int[array.size()];

		for (i = 0; i < array.size(); i++) {
			m_QuestionLanguages[i] = array.get(i);
		}

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
		
		textViewNumberOfQuestionsLeft = (TextView) findViewById(R.id.textViewNumberOfQuestionsLeft);
		textViewQuestionLevel = (TextView) findViewById(R.id.textViewQuestionLevel);
		textViewLivesLeftValue = (TextView) findViewById(R.id.textViewLivesLeftValue);
		textViewTimesPlayedTitle = (TextView) findViewById(R.id.textViewTimesPlayedTitle);
		textViewGameScoreText = (TextView) findViewById(R.id.textViewGameScoreText);
		
		textSwitcherTime = (TextSwitcher) findViewById(R.id.textViewTime);
		
		textSwitcherTime.setFactory(this);
		
		Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
		Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
		
		textSwitcherTime.setInAnimation(inAnimation);
		textSwitcherTime.setOutAnimation(outAnimation);
	}

	public class MyCountDownCounter extends CountDownTimerWithPause {

		public MyCountDownCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			//
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//
			m_MillisUntilFinished = millisUntilFinished;
			updateTime(Long.toString(millisUntilFinished / 1000));
			

		}

		private void updateTime(String string) {
			// 
			textSwitcherTime.setText(string);
			
		}

		@Override
		public void onFinish() {
			//
			updateLivesTextView();
			updateTime("0");
			m_MillisUntilFinished = 0;
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
		// checking if we are on the first question

		int currentQuestionIndex = m_QuestionIndex - 1;
		int previousQuestionIndex = currentQuestionIndex - 1;

		if (previousQuestionIndex > -1) {
			// we are not in the first question
			intent.putExtra(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_ID,
					m_Questions.getQuestionAtIndex(previousQuestionIndex)
							.getQuestionId());
			if (m_ReverseNumbersInQuestions) {
				intent.putExtra(
						ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
						m_StringParser.reverseNumbersInStringHebrew(m_Questions
								.getQuestionAtIndex(previousQuestionIndex)
								.getQuestion()));
			} else {
				intent.putExtra(
						ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
						m_Questions.getQuestionAtIndex(previousQuestionIndex)
								.getQuestion());
			}

		} else {
			// we are in the first question ,filling the previous question with
			// dud value
			intent.putExtra(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_ID,
					"-1");
			intent.putExtra(ActivityGame.INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
					"bla bla");

		}

		intent.putExtra(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_ID,
				m_CurrentQuestion.getQuestionId());

		if (m_ReverseNumbersInQuestions) {
			intent.putExtra(ActivityGame.INTENT_EXTRA_CURRENT_QUESTION_STRING,
					m_StringParser
							.reverseNumbersInStringHebrew(m_CurrentQuestion
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

		stopOrPauseCountdownTimer(true);
		// this is implemented in order to prevent double click
		disableAnswerButtons();

		// checking if time is up
		if (i == -1) {
			ret = -1;
			// 
			sb.append(getString(R.string.time_is_up_));
			incCurrentWrongAnswersCounter();
			startSoundFromSoundPool(m_SoundAnswerWrong, 0);

		} else if (i == -2) {
			// if pass question pressed
		} else {

			correctAnswerIndex = m_CurrentQuestion.getCorrectAnswerIndex();

			if (i == correctAnswerIndex) {

				ret = 0;
				setButtonGreen(o_Button);

				startSoundFromSoundPool(m_SoundAnswerCorrect, 0);

				m_TriviaDb.incUserCorrectCounter(m_CurrentQuestion
						.getQuestionId());

				// if all questions game, increment lives
				incAllQuestionsLives();

				addScore();

			} else {
				startSoundFromSoundPool(m_SoundAnswerWrong, 0);

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

	private void addScore() {
		//
		int addScore = 0;
		int questionLevel = m_CurrentQuestion.getQuestionLevel() * 10;
		// adding game score according to question level ( difficulty)
		addScore += questionLevel;

		// adding time bonus
		double bonus = (double) m_MillisUntilFinished
				/ (double) m_TimeToAnswerQuestion;
		bonus *= questionLevel;
		addScore += bonus;

		m_GameScore += addScore;

		// TODO: find better solution, have custom toast
		
		Toast t = Toast.makeText(this, "" + "(+" + addScore + ")",Toast.LENGTH_SHORT);
		
		t.setGravity(Gravity.TOP, textViewGameScoreText.getLeft(), textViewGameScoreText.getTop());
		t.setMargin(0, 0);
		
		t.show();
		
		setGameScoreText("" + m_GameScore);

	}

	private void setGameScoreText(String i_Text) {
		//
		textViewGameScoreText.setText(i_Text);

	}

	private void startSoundFromSoundPool(int i_Sound, int i_LoopEnabled) {
		//
		// checking if the user has enabled the sound and the sound is loaded.
		if (m_SoundEnabled && i_Sound != -1) {

			m_SoundPool.play(i_Sound, 1, 1, 0, i_LoopEnabled, 1);
		}

	}

	private void disableAnswerButtons() {
		//
		buttonAnswer1.setEnabled(false);
		buttonAnswer2.setEnabled(false);
		buttonAnswer3.setEnabled(false);
		buttonAnswer4.setEnabled(false);

	}

	private void enableAnswerButtons() {
		//
		buttonAnswer1.setEnabled(true);
		buttonAnswer2.setEnabled(true);
		buttonAnswer3.setEnabled(true);
		buttonAnswer4.setEnabled(true);

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
		stopOrPauseCountdownTimer(true);
		
		// if m_UserId is of logged in user than enter it to the database
		// 
		if ( m_UserId > -1 ){
			m_TriviaDb.addScoreToDatabase(m_UserId, m_CurrentGameType, m_GameScore);
			
		}

		AlertDialog.Builder gameOverDialog = new AlertDialog.Builder(
				ActivityGame.this);
		gameOverDialog.setTitle(getString(R.string.game_over));
		gameOverDialog.setCancelable(false);
		gameOverDialog.setPositiveButton(getString(R.string.exit),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						finish();
					}
				});
		gameOverDialog.setNegativeButton(getString(R.string.new_game),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						restartGame();
					}
				});
		gameOverDialog.show();
	}

	private void updateLivesTextView() {
		switch (m_CurrentGameType) {

		case GAMETYPE_ALL_QUESTIONS:
			textViewLivesLeftValue.setText(String.valueOf(m_AllQuestionsLives));
			
			break;

		case GAMETYPE_CATEGORIES:
		case GAMETYPE_LEVELS:

			textViewLivesLeftValue.setText(String.valueOf(m_MaxWrongAnswersAllowed - m_CurrentWrongAnswersCounter));

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

	private void stopOrPauseCountdownTimer(boolean i_StopCounter) {
		//
		m_ClockSound.stop();
		try {
			m_ClockSound.prepare();
		} catch (IllegalStateException e) {
			//
			e.printStackTrace();
		} catch (IOException e) {
			//
			e.printStackTrace();
		}
		if (i_StopCounter) {
			m_CountDownCounter.cancel();
		} else {
			m_CountDownCounter.pause();
		}

	}

	@Override
	protected void onResume() {
		//
		super.onResume();

		loadSoundPool();

		// checking if to show the report question button
		if (m_SharedPreferences.getBoolean(
				"checkBoxPreferenceShowReportQuestion", true)) {

			buttonReportMistakeInQuestion.setVisibility(View.VISIBLE);
		} else {
			buttonReportMistakeInQuestion.setVisibility(View.GONE);
		}

		if (m_ResumeFromHelp) {
			m_ResumeClock = false;
			m_ResumeFromHelp = false;
		}

		if (m_ResumeClock) {

			startOrResumeCountDownTimer(false);
		}

	}

	private void loadSoundPool() {
		//
		m_SoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		m_SoundAnswerCorrect = m_SoundPool.load(this, R.raw.correct, 0);
		m_SoundAnswerWrong = m_SoundPool.load(this, R.raw.wrong, 0);

	}

	@Override
	protected void onPause() {
		//
		super.onPause();

		m_SoundPool.release();

		stopOrPauseCountdownTimer(false);
		m_ResumeClock = true;

	}

	@Override
	public View makeView() {
		// 
		TextView t = new TextView(this);
		t.setGravity(Gravity.CENTER);
		t.setTextSize(18);
		
		return t;
		
	}

}
