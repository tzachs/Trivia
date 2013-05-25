package com.tzachsolomon.trivia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by tzach on 5/25/13.
 */
public class FragmentAdmin extends SherlockFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin,null);

        initializeVariables(view);
        return view;
    }

    private void initializeVariables(View view) {

    }
}
