package com.tzachsolomon.trivia;

/**
 * Created by tzach on 6/16/13.
 */
public abstract class ClassGame  {

    protected Listener mListener;



    public interface Listener{
        public void onGameFinished(int gameOverCode);
        public void onLevelFinished();
        public void onDisplayQuestion(Question currentQuestion);
        public void onQuestionUpdateTime(String newTime);
        public void onQuestionTimeFinished();
        public void onAchievementUnlock(int achievementLevelsLevel1NoFault);
    }

    protected int mMaxNumberOfLives;
    protected int mNumberOfQuestionsInEachLevel;
    protected int mNumberOfLevels;

    boolean mIsGameOver;
    protected int mGameScore;
    protected int mGameType;
    protected boolean mRestartLivesInEachLevel;
    protected int mCurrentLevel;
    protected int mCurrentQuestionIndex;
    protected int mCurrentNumberOfLives;
    protected Question mCurrentQuestion;
    protected TriviaDbEngine mTriviaDb;
    protected Questions mQuestions;
    protected long mTimerTotalTime;
    protected long mTimerInterval;

    protected int mLastAddedScore;

    protected MyCountdownTimer mCountdownTimer;


    public ClassGame (int gameType,
                      int numberOfLives,
                      int numberOfQuestionsInEachLevel,
                      int numberOfLevels,
                      boolean restartLivesInEachLevel,
                       TriviaDbEngine triviaDb,
                       int totalTimeForEachQuestion
    ){
        mGameScore = 0;
        mLastAddedScore = 0;
        mMaxNumberOfLives = numberOfLives;
        mNumberOfLevels = numberOfLevels;
        mGameType = gameType;
        mNumberOfQuestionsInEachLevel = numberOfQuestionsInEachLevel;

        mRestartLivesInEachLevel = restartLivesInEachLevel;
        mIsGameOver = false;
        mTriviaDb = triviaDb;
        mCurrentNumberOfLives = -1;

        mTimerInterval = 1000;
        mTimerTotalTime = totalTimeForEachQuestion;

        mCountdownTimer = new MyCountdownTimer(mTimerTotalTime,mTimerInterval);

    }

    public void setGameListener(ActivityGame activityGame) {
        mListener = (ClassGame.Listener)activityGame;
    }

    public Question getCurrentQuestion(){
        return mQuestions.getQuestionAtIndex(mCurrentQuestionIndex);
    }

    public String getGameScoreAsString(){
        return String.valueOf(mGameScore);
    }

    public int getGameScore(){
        return mGameScore;
    }

    public int getCurrentGameType(){
        return mGameType;
    }

    public boolean isGameOver(){
        return  mIsGameOver;
    }

    public String getCurrentLevelAsString(){
        return String.valueOf(mCurrentLevel);
    }

    public String getNextLevelAsString(){
        return String.valueOf(mCurrentLevel+1);
    }

    public String getHowManyTimesQuestionsBeenAsked(){
        return String.valueOf(mCurrentQuestion.getQuestionTimesPlayed());
    }


    public String getCurrentLivesAsString(){
        return String.valueOf(mCurrentNumberOfLives);
    }

    public void questionClockStart(){
        mCountdownTimer.start();
    }

    public void questionClockResume(){
        mCountdownTimer.resume();
    }

    public void questionClockPause(){
        mCountdownTimer.pause();
    }

    public void questionClockStop (){
        mCountdownTimer.cancel();
    }

    public String getNumberOfQuestionsLeftAsString() {
        return String.valueOf(mNumberOfQuestionsInEachLevel - mCurrentQuestionIndex);
    }



    public abstract boolean checkIsAnswerCorrect(int answerIndex);
    public abstract Question nextQuestion();
    public abstract void startNewRound();
    public abstract void setupNewGame(int startLevel);
    public abstract String getLastAddedScore();
    public abstract void startNewGame();

}
