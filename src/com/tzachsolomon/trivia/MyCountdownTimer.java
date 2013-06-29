package com.tzachsolomon.trivia;

/**
 * Created by tzach on 6/18/13.
 */
public class MyCountdownTimer extends CountDownTimerWithPause {

    private long mMillisUntilFinished;

    private Listener mListener;

    public void setListener(ClassGame classGame) {
        mListener = (Listener)classGame;
    }

    public interface Listener{
        public void onUpdateTime(String newTime);
        public void onTimerFinish();
    }

    public MyCountdownTimer(long millisInFuture, long countDownInterval) {
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
        mListener.onUpdateTime(string);


    }

    @Override
    public void onFinish() {
        //

        updateTime("0");
        mMillisUntilFinished = 0;
        mListener.onTimerFinish();
    }

}
