package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Random;

import com.tzachsolomon.trivia.JSONHandler.ScoreListener;

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

public class ActivityGame extends Activity implements OnClickListener,
		ViewFactory, ScoreListener {

	public static final String TAG = ActivityGame.class.getSimpleName();


	private MyCountDownCounter mCountDownCounter;

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

	private Questions mQuestions;
	private Question mCurrentQuestion;

	private int mQuestionIndex;
	private int mQuestionLength;

	private int mDelayBetweenQuestions;
	private int mCurrentGameType;
	private int mCurrentQuestionInThisLevel;
	private int mMaxNumberOfQuestionInLevel;
	private int mNumberOfLevels;

	private int mCurrentLevel;
	private int mCurrentWrongAnswersCounter;
	private int mMaxWrongAnswersAllowed;

	private int mAllQuestionsLives;

	private TriviaDbEngine m_TriviaDb;
	private SharedPreferences m_SharedPreferences;

	private Random m_Random;

	private boolean mGameOver;
	private boolean mSortByNewQuestionFirst;
	private boolean mResumeClock;
	private boolean mShowCorrectAnswer;
	private boolean mReverseNumbersInQuestions;

	private Bundle mExtras;

	private int[] mCategories;

	private boolean mResumeFromHelp;

	private StringParser mStringParser;

	private int[] mQuestionLanguages;

	private SoundPool mSoundPool;

	// each of the sounds is initialize with -1 i order to prevent a state where
	// we try
	// to play and the sound was not yet loaded.
	private int mSoundAnswerCorrect = -1;
	private int mSoundAnswerWrong = -1;

	private boolean mSoundEnabled;
	private MediaPlayer mClockSound;

	private TextView textViewTimesPlayedTitle;

	private int mGameScore;

	public long mMillisUntilFinished;

	private int mTimeToAnswerQuestion;

	private TextView textViewGameScoreText;

	private int mUserId;

	private TextSwitcher textSwitcherTime;

	private JSONHandler mJSONHandler;
    private TextView textViewHowManyTimesQuestionsBeenAsked;
    private boolean mRestartLivesEachLevel;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_new);

		m_SharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initializeVariables();

		mClockSound = MediaPlayer.create(this, R.raw.clock);
		mClockSound.setLooping(true);

		mExtras = getIntent().getExtras();
		mCurrentGameType = mExtras.getInt(EXTRA_GAME_TYPE);
		mUserId = mExtras.getInt(EXTRA_GAME_USER_ID);

		showInstructions();
	}

	private void restartGame() {

		initializeGameSettings();

		mExtras = getIntent().getExtras();
		mCurrentGameType = mExtras.getInt(EXTRA_GAME_TYPE);

		parseGameSetupAndStart();
	}

	private void parseGameSetupAndStart() {
		//
		updateLivesTextView();
		mGameScore = 0;
		textViewGameScoreText.setText("0");

		switch (mCurrentGameType) {
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
		switch (mCurrentGameType) {

		case GAMETYPE_ALL_QUESTIONS:
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
		case GAMETYPE_CATEGORIES:
		case GAMETYPE_LEVELS:
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
			intent.putExtra(INTENT_EXTRA_GAME_TYPE,
					mCurrentGameType);
			mResumeFromHelp = true;
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

			if (mCurrentGameType == GAMETYPE_CATEGORIES) {
				mCategories = mExtras
						.getIntArray(EXTRA_GAME_CATEGORIES);
			}

			mCurrentLevel = mExtras.getInt("EXTRA_GAME_START_LEVEL");
			mNumberOfLevels = 10;
			mMaxNumberOfQuestionInLevel = 10;
			mMaxWrongAnswersAllowed = 3;
			mCurrentWrongAnswersCounter = 0;

			mSortByNewQuestionFirst = m_SharedPreferences.getBoolean(
					"checkBoxPreferencePreferNewQuestions", true);

			textViewLivesLeftValue.setText(""
					+ (mMaxWrongAnswersAllowed - mCurrentWrongAnswersCounter));

			startNewRoundGameLevels();

		} else {
			Toast.makeText(this, getString(R.string.no_questions_in_database),
					Toast.LENGTH_SHORT).show();
			finish();

		}

	}

	private void startNewRoundGameLevels() {

		mResumeClock = false;
        if (mRestartLivesEachLevel){
            mCurrentWrongAnswersCounter = 0;
            textViewLivesLeftValue.setText(""
                    + (mMaxWrongAnswersAllowed - mCurrentWrongAnswersCounter));
        }

		mCurrentLevel++;

		if (mCurrentLevel <= mNumberOfLevels && !mGameOver) {

			if (mCurrentGameType == GAMETYPE_LEVELS) {
				mQuestions = m_TriviaDb.getQuestionsByLevel(mCurrentLevel,
						mSortByNewQuestionFirst, mQuestionLanguages);
			} else if (mCurrentGameType == GAMETYPE_CATEGORIES) {
				mQuestions = m_TriviaDb.getQuestionsByLevelAndCategories(
						mCurrentLevel, mSortByNewQuestionFirst, mCategories,
						mQuestionLanguages);
			}

			mQuestionLength = mQuestions.getNumberOfQustions();
			if (mQuestionLength < 10) {
				mMaxNumberOfQuestionInLevel = mQuestionLength;
			} else {
				mMaxNumberOfQuestionInLevel = 10;
			}

			mQuestionIndex = 0;

			mQuestions.shuffle(mSortByNewQuestionFirst);

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					this);

			alertDialog.setTitle(getString(R.string.starting_level_)
					+ mCurrentLevel);
			alertDialog.setPositiveButton(getString(R.string.start),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//

							mQuestionIndex = 0;
							mCurrentQuestionInThisLevel = 0;

							new StartNewQuestionAsync().execute(0);

						}
					});
			alertDialog.setCancelable(false);

			alertDialog.show();

		} else if (mGameOver){
			Toast.makeText(this, getString(R.string.levels_game_over),
					Toast.LENGTH_SHORT).show();

		} else {
            Toast.makeText(this, getString(R.string.finishedAllQuestions),
                    Toast.LENGTH_SHORT).show();
            mCurrentLevel = 1;

        }

        m_SharedPreferences.edit().putInt("lastLevelPlayed",mCurrentLevel).commit();
	}

	private void startGameAllQuestions() {
		//

		mResumeClock = false;
		mAllQuestionsLives = 0;

		textViewLivesLeftValue.setText(String.valueOf(mAllQuestionsLives));

		mQuestions = m_TriviaDb.getQuestionsEnabled(mSortByNewQuestionFirst,
				mQuestionLanguages);

		// Shuffling the order of the questions
		mQuestions.shuffle(mSortByNewQuestionFirst);

		mQuestionLength = mQuestions.getNumberOfQustions() - 1;

		// checking if there are questions to be asked
		if (mQuestions.getNumberOfQustions() > 0) {
			mQuestionIndex = -1;

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					this);

			alertDialog.setTitle(getString(R.string.start_game));
			alertDialog.setPositiveButton(getString(R.string.start),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//

							mQuestionIndex = 0;
							mCurrentQuestionInThisLevel = 0;

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

		switch (mCurrentGameType) {
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
		mCurrentQuestionInThisLevel++;

		if (mCurrentQuestionInThisLevel < mMaxNumberOfQuestionInLevel) {

			// getting reference to the current question
			mCurrentQuestion = getNextQuestionInLevel();

			// checking if we are out of questions for this level before the
			// level has ended

			if (mCurrentQuestion == null) {
				// no more questions in this level
				// going to next level

				Toast.makeText(
						this,
						getString(R.string.no_more_questions_in_this_level_going_to_next_level_),
						Toast.LENGTH_LONG).show();
				startNewRoundGameLevels();

			} else {
				textViewNumberOfQuestionsLeft.setText(Integer
						.toString(mMaxNumberOfQuestionInLevel
								- mCurrentQuestionInThisLevel));

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

		if (mReverseNumbersInQuestions) {
			textViewQuestion.setText(mStringParser
					.reverseNumbersInStringHebrew(mCurrentQuestion
							.getQuestion()));

			textViewTimesPlayedTitle
					.setText(mStringParser
							.reverseNumbersInStringHebrew(getString(R.string.textViewTimesPlayedTitleText)
									+ mCurrentQuestion.getQuestionTimesPlayed()));
		} else {
			textViewQuestion.setText(mCurrentQuestion.getQuestion());
			textViewTimesPlayedTitle
					.setText(getString(R.string.textViewTimesPlayedTitleText)
							+ " " + mCurrentQuestion.getQuestionTimesPlayed());
		}
		// setting question difficulty
		textViewQuestionLevel.setText(Integer.toString(mCurrentQuestion
				.getQuestionLevel()));
        textViewHowManyTimesQuestionsBeenAsked.setText(Integer.toString(mCurrentQuestion.getQuestionTimesPlayed()));

		// randomize answer places (indices)
		mCurrentQuestion.randomizeAnswerPlaces(m_Random);

		buttonAnswer1.setText(mCurrentQuestion.getAnswer1());
		buttonAnswer2.setText(mCurrentQuestion.getAnswer2());
		buttonAnswer3.setText(mCurrentQuestion.getAnswer3());
		buttonAnswer4.setText(mCurrentQuestion.getAnswer4());

	}

	private void startOrResumeCountDownTimer(boolean i_Start) {
		//
		if (i_Start) {
			mCountDownCounter.start();
		} else {
			mCountDownCounter.resume();
		}
		if (mSoundEnabled) {
			mClockSound.start();
		}

	}

	private Question getNextQuestionInLevel() {

		Question ret = null;

		if (mQuestionIndex < mQuestionLength) {

			ret = mQuestions.getQuestionAtIndex(mQuestionIndex);

			mQuestionIndex++;
		}

		return ret;
	}

	private void startNewQuestionAllQuestions() {

		// setting number of questions left, must be before m_QuestionIndex+=
		textViewNumberOfQuestionsLeft.setText(Integer.toString(mQuestionLength
				- mQuestionIndex));

		if (mQuestionIndex < mQuestionLength) {

			mQuestionIndex++;

			// getting reference to the current question
			mCurrentQuestion = mQuestions.getQuestionAtIndex(mQuestionIndex);

			initializeQuestionTextViews();

			startOrResumeCountDownTimer(true);

		}

	}

	private void initializeVariables() {
		//
		// initialize the sounds

		m_TriviaDb = new TriviaDbEngine(this);
		mJSONHandler = new JSONHandler(this);

		mJSONHandler.setScoreUpdateListener(this);

		mUserId = -1;

		mSoundEnabled = m_SharedPreferences.getBoolean(
				"checkBoxPreferencePlayGameSounds", true);

		mStringParser = new StringParser(m_SharedPreferences);
		mReverseNumbersInQuestions = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceRevereseInHebrew", false);
		mShowCorrectAnswer = m_SharedPreferences.getBoolean(
				"checkBoxPreferenceShowCorrectAnswer", true);
        mRestartLivesEachLevel = m_SharedPreferences.getBoolean("checkBoxPreferenceRestartLivesEachLevel", true);

		initializeQuestionsLanguages();

		initializeTextViews();

		initializeButtons();

		initializeGameSettings();

	}

	private void initializeGameSettings() {
		mResumeClock = false;
		mGameOver = false;


		// m_Random = new Random(1);
		m_Random = new Random(System.currentTimeMillis());
		mTimeToAnswerQuestion = 10;

		try {
			mTimeToAnswerQuestion = Integer.parseInt(m_SharedPreferences
					.getString("editTextPreferenceCountDownTimer", "10"));
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage().toString());
		}
		try {
			mDelayBetweenQuestions = Integer
					.parseInt(m_SharedPreferences.getString(
							"editTextPreferenceDelayBetweenQuestions", "500"));
		} catch (ClassCastException e) {
			Log.e(TAG, e.getMessage().toString());
		}

		mTimeToAnswerQuestion *= 1000;

		mCountDownCounter = new MyCountDownCounter(mTimeToAnswerQuestion, 1000);

		mCurrentGameType = -1;

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

		mQuestionLanguages = new int[array.size()];

		for (i = 0; i < array.size(); i++) {
			mQuestionLanguages[i] = array.get(i);
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
        textViewHowManyTimesQuestionsBeenAsked = (TextView)findViewById(R.id.textViewHowManyTimesQuestionsBeenAsked);

		textSwitcherTime = (TextSwitcher) findViewById(R.id.textViewTime);

		textSwitcherTime.setFactory(this);

		Animation inAnimation = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in);
		Animation outAnimation = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out);

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
			mMillisUntilFinished = millisUntilFinished;
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
			mMillisUntilFinished = 0;
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

		int currentQuestionIndex = mQuestionIndex - 1;
		int previousQuestionIndex = currentQuestionIndex - 1;

		if (previousQuestionIndex > -1) {
			// we are not in the first question
			intent.putExtra(INTENT_EXTRA_PREVIOUS_QUESTION_ID,
					mQuestions.getQuestionAtIndex(previousQuestionIndex)
							.getQuestionId());
			if (mReverseNumbersInQuestions) {
				intent.putExtra(
						INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
						mStringParser.reverseNumbersInStringHebrew(mQuestions
								.getQuestionAtIndex(previousQuestionIndex)
								.getQuestion()));
			} else {
				intent.putExtra(
						INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
						mQuestions.getQuestionAtIndex(previousQuestionIndex)
								.getQuestion());
			}

		} else {
			// we are in the first question ,filling the previous question with
			// dud value
			intent.putExtra(INTENT_EXTRA_PREVIOUS_QUESTION_ID,
					"-1");
			intent.putExtra(INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
					"bla bla");

		}

		intent.putExtra(INTENT_EXTRA_CURRENT_QUESTION_ID,
				mCurrentQuestion.getQuestionId());

		if (mReverseNumbersInQuestions) {
			intent.putExtra(INTENT_EXTRA_CURRENT_QUESTION_STRING,
					mStringParser.reverseNumbersInStringHebrew(mCurrentQuestion
							.getQuestion()));
		} else {
			intent.putExtra(INTENT_EXTRA_CURRENT_QUESTION_STRING,
					mCurrentQuestion.getQuestion());
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
			startSoundFromSoundPool(mSoundAnswerWrong, 0);

		} else if (i == -2) {
			// if pass question pressed
		} else {

			correctAnswerIndex = mCurrentQuestion.getCorrectAnswerIndex();

			if (i == correctAnswerIndex) {

				ret = 0;
				setButtonGreen(o_Button);

				startSoundFromSoundPool(mSoundAnswerCorrect, 0);

				m_TriviaDb.incUserCorrectCounter(mCurrentQuestion
						.getQuestionId());

				// if all questions activity_game.xml, increment lives
				incAllQuestionsLives();

				addScore();

			} else {
				startSoundFromSoundPool(mSoundAnswerWrong, 0);

				setButtonRed(o_Button);
				m_TriviaDb
						.incUserWrongCounter(mCurrentQuestion.getQuestionId());
				incCurrentWrongAnswersCounter();

				// checking if the user answer wrong and we need to show the
				// correct answer
				if (mShowCorrectAnswer) {
					setButtonGreen(mCurrentQuestion.getCorrectAnswerIndex());
				}
			}

		}

		updateLivesTextView();

		if (mQuestionIndex < mQuestionLength && !mGameOver) {
			// start a new question and
			new StartNewQuestionAsync().execute(mDelayBetweenQuestions);

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
		int questionLevel = mCurrentQuestion.getQuestionLevel() * 10;
		// adding activity_game.xml score according to question level ( difficulty)
		addScore += questionLevel;

		// adding time bonus
		double bonus = (double) mMillisUntilFinished
				/ (double) mTimeToAnswerQuestion;
		bonus *= questionLevel;
		addScore += bonus;

		mGameScore += addScore;

		// TODO: find better solution, have custom toast

		Toast t = Toast.makeText(this, "" + "(+" + addScore + ")",
				Toast.LENGTH_SHORT);

		t.setGravity(Gravity.TOP, textViewGameScoreText.getLeft(),
				textViewGameScoreText.getTop());
		t.setMargin(0, 0);

		t.show();

		setGameScoreText("" + mGameScore);

	}

	private void setGameScoreText(String i_Text) {
		//
		textViewGameScoreText.setText(i_Text);

	}

	private void startSoundFromSoundPool(int i_Sound, int i_LoopEnabled) {
		//
		// checking if the user has enabled the sound and the sound is loaded.
		if (mSoundEnabled && i_Sound != -1) {

			mSoundPool.play(i_Sound, 1, 1, 0, i_LoopEnabled, 1);
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
		mAllQuestionsLives += mCurrentQuestion.getQuestionLevel();

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
		switch (mCurrentGameType) {
		case GAMETYPE_ALL_QUESTIONS:
			Toast.makeText(this, getString(R.string.game_over),
					Toast.LENGTH_LONG).show();
			break;

            case GAMETYPE_CATEGORIES:
		case GAMETYPE_LEVELS:
			startNewRoundGameLevels();
			break;

		default:
			break;
		}

	}

	private void incCurrentWrongAnswersCounter() {
		//
		switch (mCurrentGameType) {

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
		mGameOver = true;
		stopOrPauseCountdownTimer(true);

		// if m_UserId is of logged in user than enter it to the database
		//
		if (mUserId > 0) {
			//
			mJSONHandler.uploadScoreToDatabase(String.valueOf(mUserId),
					String.valueOf(mCurrentGameType),
					String.valueOf(mGameScore),
					String.valueOf(System.currentTimeMillis()), -1);

		} else {
			Toast.makeText(
					this,
					getString(R.string.game_score_wasn_t_sent_sicne_the_user_isn_t_registered_or_login),
					Toast.LENGTH_LONG).show();
		}

		AlertDialog.Builder gameOverDialog = new AlertDialog.Builder(
				this);
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
		switch (mCurrentGameType) {

		case GAMETYPE_ALL_QUESTIONS:
			textViewLivesLeftValue.setText(String.valueOf(mAllQuestionsLives));

			break;

		case GAMETYPE_CATEGORIES:
		case GAMETYPE_LEVELS:

			textViewLivesLeftValue.setText(String
					.valueOf(mMaxWrongAnswersAllowed
							- mCurrentWrongAnswersCounter));

			break;

		default:
			break;
		}
	}

	private void incCurrentWrongAnswersCounter_GameLevels() {
		//
		mCurrentWrongAnswersCounter++;

		if (mCurrentWrongAnswersCounter >= mMaxWrongAnswersAllowed) {
			showGameOver();

		}

	}

	private void incCurrentWrongAnswersCounter_AllQuestions() {
		//
		mAllQuestionsLives -= mCurrentQuestion.getQuestionLevel();

		if (mAllQuestionsLives < 0) {
			showGameOver();
		}

	}

	private void stopOrPauseCountdownTimer(boolean i_StopCounter) {
		//
		mClockSound.stop();
		try {
			mClockSound.prepare();
		} catch (IllegalStateException e) {
			//
			e.printStackTrace();
		} catch (IOException e) {
			//
			e.printStackTrace();
		}
		if (i_StopCounter) {
			mCountDownCounter.cancel();
		} else {
			mCountDownCounter.pause();
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

		if (mResumeFromHelp) {
			mResumeClock = false;
			mResumeFromHelp = false;
		}

		if (mResumeClock) {

			startOrResumeCountDownTimer(false);
		}

	}

	private void loadSoundPool() {
		//
		mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		mSoundAnswerCorrect = mSoundPool.load(this, R.raw.correct, 0);
		mSoundAnswerWrong = mSoundPool.load(this, R.raw.wrong, 0);

	}

	@Override
	protected void onPause() {
		//
		super.onPause();

		mSoundPool.release();

		stopOrPauseCountdownTimer(false);
		mResumeClock = true;

	}

	@Override
	public View makeView() {
		//
		TextView t = new TextView(this);
		t.setGravity(Gravity.CENTER);
		t.setTextSize(18);

		return t;

	}

	@Override
	public void onScoreAdded(int i_Result) {
		//
		if (i_Result == JSONHandler.SUCCESS_SCORE_ADDED) {

		} else if (i_Result == JSONHandler.ERROR_SCORE_WAS_NOT_ADDED) {
			m_TriviaDb
					.addScoreToDatabase(mUserId, mCurrentGameType, mGameScore);
		}

	}

	@Override
	public void deleteScoreFromDatabase(int rowInDatabase) {
		//

	}

}
