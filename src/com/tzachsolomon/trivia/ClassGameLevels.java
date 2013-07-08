package com.tzachsolomon.trivia;

/**
 * Created by tzach on 6/16/13.
 */
public class ClassGameLevels extends ClassGame implements MyCountdownTimer.Listener {

    public ClassGameLevels(int gameType,int numberOfLives,
                           int numberOfQuestionsInEachLevel,
                           int numberOfLevels,
                           boolean restartLivesInEachLevel,
                           TriviaDbEngine triviaDb,
                           int totalTimeForEachQuestion){
        super(gameType,
                numberOfLives,
                numberOfQuestionsInEachLevel,
                numberOfLevels,
                restartLivesInEachLevel,
                triviaDb,
                totalTimeForEachQuestion
        );

        mCountdownTimer.setListener(this);
    }

    @Override
    public boolean checkIsAnswerCorrect(int answerIndex) {
        boolean ret = mCurrentQuestion.isCorrect(answerIndex);

        if ( ret){
            double timeBonus = mCountdownTimer.timePassed() / (double)mTimerTotalTime;
            timeBonus *= 10;
            mLastAddedScore = mCurrentLevel * 10;
            mLastAddedScore += timeBonus;

            mGameScore += mLastAddedScore;
        }else{
            mCurrentNumberOfLives--;
        }

        return ret;
    }

    @Override
    public Question nextQuestion() {
        mCurrentQuestion = null;

        if ( mCurrentNumberOfLives == 0 ){
            mListener.onGameFinished(ClassCommonUtils.GAME_OVER_NO_MORE_LIVES);
        }else{
            mCurrentQuestionIndex++;

            if ( mCurrentQuestionIndex >= mNumberOfQuestionsInEachLevel){

                if ( mCurrentLevel >= mNumberOfLevels ){
                    mListener.onGameFinished(ClassCommonUtils.GAME_OVER_PLAYER_WON);
                }else{
                    checkAchievement();
                    mListener.onLevelFinished();
                }
            } else{
                mCurrentQuestion = mQuestions.getQuestionAtIndex(mCurrentQuestionIndex);
                mListener.onDisplayQuestion(mCurrentQuestion);
            }
        }


        return mCurrentQuestion;
    }

    @Override
    public void startNewRound() {


        mCurrentQuestionIndex = -1;
        mCurrentLevel++;

        if (mRestartLivesInEachLevel){
            mCurrentNumberOfLives = mMaxNumberOfLives;
        }

        mQuestions = mTriviaDb.getQuestionsByLevel(mCurrentLevel,true);
        mQuestions.shuffle(true);

        nextQuestion();
    }

    private void checkAchievement() {
        if (mCurrentNumberOfLives == mMaxNumberOfLives){
            switch ( mCurrentLevel){
                case 1:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_1_NO_FAULT);
                    break;
                case 2:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_2_NO_FAULT);
                    break;
                case 3:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_3_NO_FAULT);
                    break;
                case 4:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_4_NO_FAULT);
                    break;
                case 5:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_5_NO_FAULT);
                    break;
                case 6:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_6_NO_FAULT);
                    break;
                case 7:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_7_NO_FAULT);
                    break;
                case 8:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_8_NO_FAULT);
                    break;
                case 9:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_9_NO_FAULT);
                    break;
                case 10:
                    mListener.onAchievementUnlock(ClassCommonUtils.ACHIEVEMENT_LEVELS_LEVEL_10_NO_FAULT);
                    break;
                default:
                    break;

            }
        }
    }

    @Override
    public void setupNewGame(int startLevel) {
        mCurrentLevel = startLevel;
        mCurrentNumberOfLives = mMaxNumberOfLives;


    }

    @Override
    public String getLastAddedScore() {
        return String.valueOf(mLastAddedScore);
    }

    @Override
    public void startNewGame() {
        startNewRound();


    }


    @Override
    public void onUpdateTime(String newTime) {

        mListener.onQuestionUpdateTime(newTime);


    }

    @Override
    public void onTimerFinish() {
        mListener.onQuestionTimeFinished();
    }
}
