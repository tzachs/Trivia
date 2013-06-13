package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by tzach on 6/13/13.
 */
public class FragmentAchievementsAndScores extends SherlockFragment implements View.OnClickListener {

    private Button buttonShowAchievements;
    private Button buttonSignIn;
    private Button buttonSignOut;
    private Button buttonShowScoreLeaders;
    private Listener mListener;

    public void setSignInVisible(boolean b) {
        if (b){
            buttonSignOut.setVisibility(View.INVISIBLE);
            buttonSignIn.setVisibility(View.VISIBLE);
        }else{
            buttonSignOut.setVisibility(View.VISIBLE);
            buttonSignIn.setVisibility(View.INVISIBLE);
        }
    }

    public interface Listener {
        public void onButtonSignInClicked();
        public void onButtonSignOutClicked();
        public void onButtonShowScoreLeadersClicked();
        public void onButtonShowAchievements();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            mListener = (Listener)activity;
        } catch (ClassCastException e){
            throw new ClassCastException("Activity " + activity.toString() + " must implement class FragmentAchievementsAndScores.Listener");
        }

        ((ActivityTriviaNew)activity).setFragmentRefAchivevementAndScores(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_google_play,null);

        initVariables(ret);

        return ret;
    }

    private void initVariables(View ret) {

        buttonShowScoreLeaders = (Button)ret.findViewById(R.id.buttonShowLeaderBoards);
        buttonShowAchievements = (Button)ret.findViewById(R.id.buttonShowAchievements);
        buttonSignIn = (Button)ret.findViewById(R.id.buttonSignIn);
        buttonSignOut = (Button)ret.findViewById(R.id.buttonSignOut);

        buttonShowScoreLeaders.setOnClickListener(this);
        buttonShowAchievements.setOnClickListener(this);
        buttonSignIn.setOnClickListener(this);
        buttonSignOut.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.buttonShowAchievements:
                buttonShowAchievements_Clicked();
                break;
            case R.id.buttonShowLeaderBoards:
                buttonShowScoreLeaders_Clicked();
                break;
            case R.id.buttonSignIn:
                buttonSignIn_Clicked();
                break;
            case R.id.buttonSignOut:
                buttonSignOut_Clicked();
                break;
        }

    }

    private void buttonSignOut_Clicked() {
        mListener.onButtonSignOutClicked();
    }

    private void buttonSignIn_Clicked() {
        mListener.onButtonSignInClicked();
    }

    private void buttonShowScoreLeaders_Clicked() {
        mListener.onButtonShowScoreLeadersClicked();
    }

    private void buttonShowAchievements_Clicked() {
        mListener.onButtonShowAchievements();
    }
}
