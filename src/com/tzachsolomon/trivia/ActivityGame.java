package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;

import java.util.Random;

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
import android.widget.*;
import android.widget.ViewSwitcher.ViewFactory;

public class ActivityGame extends BaseGameActivity implements OnClickListener,
        ViewFactory, ClassGame.Listener {

    public static final String TAG = ActivityGame.class.getSimpleName();



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
    ;
    private TriviaDbEngine mTriviaDb;
    private SharedPreferences m_SharedPreferences;

    private Random m_Random;
    private Bundle mExtras;

    private int[] mCategories;

    private boolean mResumeFromHelp;

    private StringParser mStringParser;
    private SoundPool mSoundPool;

    // each of the sounds is initialize with -1 i order to prevent a state where
    // we try
    // to play and the sound was not yet loaded.
    private int mSoundAnswerCorrect = -1;
    private int mSoundAnswerWrong = -1;

    private boolean mSoundEnabled;
    private MediaPlayer mClockSound;

    private TextView textViewTimesPlayedTitle;

    public long mMillisUntilFinished;

    private int mTimeToAnswerQuestion;

    private TextView textViewGameScoreText;

    private int mUserId;

    private TextSwitcher textSwitcherTime;

    private JSONHandler mJSONHandler;
    private TextView textViewHowManyTimesQuestionsBeenAsked;
    private boolean mRestartLivesEachLevel;

    private ClassGame mCurrentGame;
    private int mCurrentGameType;
    private boolean mReverseNumbersInQuestions;
    private boolean mShowCorrectAnswer;
    private Question mCurrentQuestion;
    private int mDelayBetweenQuestions;
    private boolean mResumeClock;
    private ImageButton imageButtonSound;


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


    private void initializeVariables() {
        //
        // initialize the sounds

        mTriviaDb = new TriviaDbEngine(this);
        mJSONHandler = new JSONHandler(this);
        mUserId = -1;

        mSoundEnabled = m_SharedPreferences.getBoolean(
                "checkBoxPreferencePlayGameSounds", true);


        imageButtonSound = (ImageButton)findViewById(R.id.imageButtonSound);

        imageButtonSound.setOnClickListener(this);


        // setting the correct image according to the state
        // since we change the state in the imageButtonSound_Clicked than we do the NOT before calling
        // imageButtonSound_Clicked;
        mSoundEnabled = !mSoundEnabled;
        imageButtonSound_Clicked();


        mStringParser = new StringParser(m_SharedPreferences);
        mReverseNumbersInQuestions = m_SharedPreferences.getBoolean(
                "checkBoxPreferenceRevereseInHebrew", false);
        mShowCorrectAnswer = m_SharedPreferences.getBoolean(
                "checkBoxPreferenceShowCorrectAnswer", true);
        mRestartLivesEachLevel = m_SharedPreferences.getBoolean("checkBoxPreferenceRestartLivesEachLevel", true);


        initializeTextViews();

        initializeButtons();

        initializeGameSettings();

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

    private void parseGameSetupAndStart() {

        switch (mCurrentGameType) {
            case GAMETYPE_ALL_QUESTIONS:
                break;
            case GAMETYPE_CATEGORIES:
                break;
            case GAMETYPE_LEVELS:
                mCurrentGame = new ClassGameLevels(mCurrentGameType,
                        3,
                        10,
                        10,
                        mRestartLivesEachLevel,
                        mTriviaDb,
                        mTimeToAnswerQuestion
                );

                break;

            default:
                break;
        }

        mCurrentGame.setGameListener(this);
        // TODO: change this to start from certain level
        mCurrentGame.setupNewGame(0);
        textViewLivesLeftValue.setText(mCurrentGame.getCurrentLivesAsString());
        textViewGameScoreText.setText(mCurrentGame.getGameScoreAsString());
        showStartLevel();


    }

    private void showStartLevel() {

        // verifying clock is stopped
        mCurrentGame.questionClockStop();

        AlertDialog.Builder levelFinished = new AlertDialog.Builder(
                this);
        levelFinished.setTitle("שלב" + " " + mCurrentGame.getNextLevelAsString());
        levelFinished.setCancelable(false);
        levelFinished.setPositiveButton(getString(R.string.start),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        mCurrentGame.startNewRound();
                    }
                });

        if ( !isFinishing()){
            levelFinished.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //
        parseGameSetupAndStart();
    }

    private void resetAnswerButtonBackground(int blueButton) {
        //
        buttonAnswer1.setBackgroundResource(blueButton);
        buttonAnswer2.setBackgroundResource(blueButton);
        buttonAnswer3.setBackgroundResource(blueButton);
        buttonAnswer4.setBackgroundResource(blueButton);

    }

    private void initializeQuestionTextViews(Question currentQuestion) {
        //
        mCurrentQuestion = currentQuestion;

        if (mReverseNumbersInQuestions) {
            textViewQuestion.setText(mStringParser
                    .reverseNumbersInStringHebrew(mCurrentGame.getCurrentQuestion()
                            .getQuestion()));

            textViewTimesPlayedTitle
                    .setText(mStringParser
                            .reverseNumbersInStringHebrew(getString(R.string.textViewTimesPlayedTitleText)
                                    + mCurrentGame.getCurrentQuestion().getQuestionTimesPlayed()));
        } else {
            textViewQuestion.setText(mCurrentGame.getCurrentQuestion().getQuestion());
            textViewTimesPlayedTitle
                    .setText(getString(R.string.textViewTimesPlayedTitleText)
                            + " " + mCurrentGame.getCurrentQuestion().getQuestionTimesPlayed());
        }
        // setting question difficulty
        textViewQuestionLevel.setText(mCurrentGame.getCurrentLevelAsString());

        textViewHowManyTimesQuestionsBeenAsked.setText(mCurrentGame.getHowManyTimesQuestionsBeenAsked());

        // randomize answer places (indices)
        mCurrentQuestion.randomizeAnswerPlaces(m_Random);

        buttonAnswer1.setText(mCurrentQuestion.getAnswer1());
        buttonAnswer2.setText(mCurrentQuestion.getAnswer2());
        buttonAnswer3.setText(mCurrentQuestion.getAnswer3());
        buttonAnswer4.setText(mCurrentQuestion.getAnswer4());

    }

    private void initializeGameSettings() {


        // m_Random = new Random(1);
        m_Random = new Random(System.currentTimeMillis());


        try {
            mTimeToAnswerQuestion = Integer.parseInt(m_SharedPreferences
                    .getString("editTextPreferenceCountDownTimer", "10"));
            mTimeToAnswerQuestion*=1000;

        } catch (ClassCastException e) {
            mTimeToAnswerQuestion = 10000;
            Log.e(TAG, e.getMessage().toString());
        }
        try {
            mDelayBetweenQuestions = Integer
                    .parseInt(m_SharedPreferences.getString(
                            "editTextPreferenceDelayBetweenQuestions", "500"));
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage().toString());
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


    @Override
    public void onClick(View v) {
        //

        switch (v.getId()) {

            case R.id.imageButtonSound:
                imageButtonSound_Clicked();
                break;
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

    private void imageButtonSound_Clicked() {
        mSoundEnabled = !mSoundEnabled;
        if (mSoundEnabled){
            imageButtonSound.setBackgroundResource(R.drawable.ic_audio_vol);
        }else{
            imageButtonSound.setBackgroundResource(R.drawable.ic_audio_vol_mute);
        }

        // save to property
        m_SharedPreferences.edit().putBoolean("checkBoxPreferencePlayGameSounds",mSoundEnabled).commit();

    }

    private void buttonPassQuestion_Clicked() {
        //
        checkAnswer(-2, null);

    }

    private void buttonReportMistakeInQuestion_Clicked() {

        // TODO: implement this

        Intent intent = new Intent(this, ActivityReportErrorInQuestion.class);
        // checking if we are on the first question

        // we are in the first question ,filling the previous question with
        // dud value
//        intent.putExtra(INTENT_EXTRA_PREVIOUS_QUESTION_ID,
//                "-1");
//        intent.putExtra(INTENT_EXTRA_PREVIOUS_QUESTION_STRING,
//                "bla bla");
//
//        intent.putExtra(INTENT_EXTRA_CURRENT_QUESTION_ID,
//                mCurrentQuestion.getQuestionId());
//
//        if (mReverseNumbersInQuestions) {
//            intent.putExtra(INTENT_EXTRA_CURRENT_QUESTION_STRING,
//                    mStringParser.reverseNumbersInStringHebrew(mCurrentQuestion
//                            .getQuestion()));
//        } else {
//            intent.putExtra(INTENT_EXTRA_CURRENT_QUESTION_STRING,
//                    mCurrentQuestion.getQuestion());
//        }
//
//        startActivityForResult(intent, -1);

    }

    private void setButtonRed(Button o_Button) {
        o_Button.setBackgroundResource(R.drawable.red_button);
    }

    private void setButtonGreen(Button o_Button) {
        o_Button.setBackgroundResource(R.drawable.green_button);
    }

    private void checkAnswer(int i, Button o_Button) {
        //
        mCurrentGame.questionClockStop();
        // this is implemented in order to prevent double click
        disableAnswerButtons();

        // checking if time is up
        if (i == -1) {
            startSoundFromSoundPool(mSoundAnswerWrong, 0);
            mCurrentGame.checkIsAnswerCorrect(i);

        } else if (i == -2) {
            // if pass question pressed
        } else {
            if ( mCurrentGame.checkIsAnswerCorrect(i)){

                setButtonGreen(o_Button);

                startSoundFromSoundPool(mSoundAnswerCorrect, 0);

                mTriviaDb.incUserCorrectCounter(mCurrentQuestion
                        .getQuestionId());

                toastLastScore();
                setGameScoreText(mCurrentGame.getGameScoreAsString());
            }else{

                startSoundFromSoundPool(mSoundAnswerWrong, 0);

                setButtonRed(o_Button);
                mTriviaDb
                        .incUserWrongCounter(mCurrentQuestion.getQuestionId());

                // checking if the user answer wrong and we need to show the
                // correct answer
                if (mShowCorrectAnswer) {
                    setButtonGreen(mCurrentQuestion.getCorrectAnswerIndex());
                }
            }

        }

        textViewLivesLeftValue.setText(mCurrentGame.getCurrentLivesAsString());

        new StartNewQuestionAsync().execute(1000);



    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }

    @Override
    public void onStartUserNotSignedOn() {

    }

    public class StartNewQuestionAsync extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            //
            mCurrentGame.nextQuestion();
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

    private void toastLastScore() {
        //
        Toast t = Toast.makeText(this, "" + "(+" + mCurrentGame.getLastAddedScore() + ")",
                Toast.LENGTH_SHORT);

        t.setGravity(Gravity.TOP, textViewGameScoreText.getLeft(),
                textViewGameScoreText.getTop());
        t.setMargin(0, 0);

        t.show();



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

    private void showGameOver() {

        // send score
        if ( getGamesClient().isConnected()){
            getGamesClient().submitScore(getString(R.string.game_levels_leadersboard),mCurrentGame.getGameScore());
        }

        mCurrentGame.questionClockStop();

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

                        parseGameSetupAndStart();

                    }
                });
        if (!isFinishing()){
            gameOverDialog.show();
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
    public void onGameFinished(int gameOverCode) {
        switch (gameOverCode){
            case ClassCommonUtils.GAME_OVER_NO_MORE_LIVES:
                showGameOver();

                break;
            case ClassCommonUtils.GAME_OVER_PLAYER_WON:
                showPlayerWon();
                showGameOver();

                break;
        }

    }

    private void showPlayerWon() {
        Toast.makeText(this,"You won! :)",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLevelFinished() {
        showStartLevel();
    }

    @Override
    public void onDisplayQuestion(Question currentQuestion) {
        if ( currentQuestion != null){
            resetAnswerButtonBackground(R.drawable.blue_button);
            initializeQuestionTextViews(currentQuestion);
            textViewNumberOfQuestionsLeft.setText(mCurrentGame.getNumberOfQuestionsLeftAsString());
            enableAnswerButtons();
            mCurrentGame.questionClockStart();
        }else{

        }

    }

    @Override
    public void onQuestionUpdateTime(String newTime) {
        textSwitcherTime.setText(newTime);
    }

    @Override
    public void onQuestionTimeFinished() {
        checkAnswer(-1,null);
        textSwitcherTime.setText("0");
    }

    @Override
    public void onAchievementUnlock(int achievementId) {

        String id = null;

        switch (achievementId){
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_1_NO_FAULT:
                id = getString(R.string.game_levels_level_01_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_2_NO_FAULT:
                id = getString(R.string.game_levels_level_02_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_3_NO_FAULT:
                id = getString(R.string.game_levels_level_03_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_4_NO_FAULT:
                id = getString(R.string.game_levels_level_04_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_5_NO_FAULT:
                id = getString(R.string.game_levels_level_05_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_6_NO_FAULT:
                id = getString(R.string.game_levels_level_06_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_7_NO_FAULT:
                id = getString(R.string.game_levels_level_07_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_8_NO_FAULT:
                id = getString(R.string.game_levels_level_08_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_9_NO_FAULT:
                id = getString(R.string.game_levels_level_09_no_faults);
                break;
            case ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_10_NO_FAULT:
                id = getString(R.string.game_levels_level_10_no_faults);
                break;
        }

        if (getGamesClient().isConnected()){
            if ( id !=null){
                getGamesClient().unlockAchievement(id);
            }
        }

    }

}
