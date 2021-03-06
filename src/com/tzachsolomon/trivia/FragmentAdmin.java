package com.tzachsolomon.trivia;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by tzach on 5/25/13.
 */
public class FragmentAdmin extends SherlockFragment implements View.OnClickListener {

    private Button buttonSendSuggestion;
    private Button buttonUpdateDatabase;
    private Button buttonSendQuestion;
    private Button buttonShowPreferences;
    private Button buttonDeleteDatabase;
    private FragmentAdminListener mFragmentAdminListener;
    private TriviaDbEngine mTriviaDb;


    public interface FragmentAdminListener {
        public void onUpdateDatabaseClicked();
        public void onSendSuggestionClicked();
        public void onSendQuestionClicked();
        public void onShowPreferencesClicked();

        public void onDeleteDatabase();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentAdminListener = (FragmentAdminListener)activity;

        }catch (ClassCastException e){
            throw new ClassCastException("activity " + activity.toString() + " must implement FragmentAdminListener");
        }

        mTriviaDb = new TriviaDbEngine(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin,null);

        initializeVariables(view);
        return view;
    }

    private void initializeVariables(View view) {

        buttonSendSuggestion = (Button)view.findViewById(R.id.buttonSendSuggestion);
        buttonUpdateDatabase = (Button)view.findViewById(R.id.buttonUpdateDatabase);
        buttonSendQuestion = (Button)view.findViewById(R.id.buttonSendQuestion);
        buttonShowPreferences = (Button)view.findViewById(R.id.buttonShowPreferences);
        buttonDeleteDatabase = (Button)view.findViewById(R.id.buttonDeleteDatabase);

        buttonSendQuestion.setOnClickListener(this);
        buttonSendSuggestion.setOnClickListener(this);
        buttonUpdateDatabase.setOnClickListener(this);
        buttonShowPreferences.setOnClickListener(this);
        buttonDeleteDatabase.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.buttonDeleteDatabase:
                buttonDeleteDatabase_Clicked();
                break;
            case R.id.buttonSendSuggestion:
                buttonSendSuggestion_Clicked();
                break;
            case R.id.buttonSendQuestion:
                buttonSendQuestion_Clicked();
                break;
            case R.id.buttonUpdateDatabase:
                buttonUpdateDatabase_Clicked();
                break;
            case R.id.buttonShowPreferences:
                buttonShowPreferences_Clicked();
                break;

        }
    }

    private void buttonDeleteDatabase_Clicked() {
        mTriviaDb.deleteDatabase();

        mFragmentAdminListener.onDeleteDatabase();

    }

    private void buttonShowPreferences_Clicked() {
        mFragmentAdminListener.onShowPreferencesClicked();
    }

    private void buttonUpdateDatabase_Clicked() {
        mFragmentAdminListener.onUpdateDatabaseClicked();

    }

    private void buttonSendQuestion_Clicked() {
        mFragmentAdminListener.onSendQuestionClicked();
    }

    private void buttonSendSuggestion_Clicked() {
        mFragmentAdminListener.onSendSuggestionClicked();
    }
}
