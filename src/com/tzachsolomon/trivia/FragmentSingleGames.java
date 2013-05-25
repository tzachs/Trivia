package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by tzach on 5/25/13.
 */
public class FragmentSingleGames extends SherlockFragment implements View.OnClickListener {

    private Button buttonNewGameSimple;
    private Button buttonNewGameAllQuestions;
    private Button buttonNewGameCategories;
    private FragmentSingleGamesListener mFragmentSingleGamesListener;



    public interface FragmentSingleGamesListener {
        public void onNewGameSimpleClicked();
        public void onNewGameAllQuestionsClicked();
        public void onNewGameCategoriesClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mFragmentSingleGamesListener = (FragmentSingleGamesListener)activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement FragmentSingleGamesListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_single_games,null);
        
        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View view) {

        buttonNewGameSimple = (Button)view.findViewById(R.id.buttonNewGameSimple);
        buttonNewGameAllQuestions = (Button)view.findViewById(R.id.buttonNewGameAllQuestions);
        buttonNewGameCategories = (Button)view.findViewById(R.id.buttonNewGameCategories);

        buttonNewGameSimple.setOnClickListener(this);
        buttonNewGameAllQuestions.setOnClickListener(this);
        buttonNewGameCategories.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonNewGameSimple:
                buttonNewGameSimple_Clicked();
                break;
            case R.id.buttonNewGameAllQuestions:
                buttonNewGameAllQuestions_Clicked();
                break;
            case R.id.buttonNewGameCategories:
                buttonNewGameCategories_Clicked();
                break;

        }
    }

    private void buttonNewGameCategories_Clicked() {
        mFragmentSingleGamesListener.onNewGameCategoriesClicked();
    }

    private void buttonNewGameAllQuestions_Clicked() {
        mFragmentSingleGamesListener.onNewGameAllQuestionsClicked();
    }

    private void buttonNewGameSimple_Clicked() {
        mFragmentSingleGamesListener.onNewGameSimpleClicked();
    }


}
