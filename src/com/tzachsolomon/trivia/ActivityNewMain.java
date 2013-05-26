package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

import java.util.ArrayList;

/**
 * Created by tzach on 5/25/13.
 */
public class ActivityNewMain extends SherlockFragmentActivity implements FragmentSingleGames.FragmentSingleGamesListener,
        FragmentAdmin.FragmentAdminListener{

    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private Intent mCurrentGame;
    private int mCurrentUserId;
    private int mLastLevel;
    private UpdateManager mUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);

        setContentView(mViewPager);

        initializeActionBar();

        mLastLevel = 1;
        mCurrentUserId = -2;
        mUpdateManager = new UpdateManager(this);


    }

    private void initializeActionBar() {
        //

        ActionBar actionBar = getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        boolean showTitle = mSharedPreferences.getBoolean(
//                "checkBoxPrefShowTitle", true);
        boolean showTitle = true;
        actionBar.setDisplayHomeAsUpEnabled(showTitle);
        actionBar.setDisplayShowHomeEnabled(showTitle);
        actionBar.setDisplayShowTitleEnabled(showTitle);

        actionBar.removeAllTabs();

        mTabsAdapter = new TabsAdapter(this, mViewPager);

        mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.single_games)),FragmentSingleGames.class,null);
        mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.admin)),FragmentAdmin.class,null);

    }

    @Override
    public void onNewGameSimpleClicked() {
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_GAME_TYPE,
                GAMETYPE_LEVELS);
        extras.putInt(EXTRA_GAME_START_LEVEL, mLastLevel);
        startNewGame(extras);
    }

    @Override
    public void onNewGameAllQuestionsClicked() {
        Bundle extras = new Bundle();

        extras.putInt(EXTRA_GAME_TYPE,
                GAMETYPE_ALL_QUESTIONS);
        startNewGame(extras);

    }

    @Override
    public void onNewGameCategoriesClicked() {
        Intent intent = new Intent(this, ActivityShowCategoryForGame.class);
        startActivityForResult(intent, REQUEST_CODE_START_GAME_CATEGORIES);

    }

    @Override
    public void onUpdateDatabaseClicked() {
        mUpdateManager.updateQuestions(false);
    }

    @Override
    public void onSendSuggestionClicked() {
        sendSuggestion();
    }


    private void sendSuggestion() {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] { "tzach.solomon@gmail.com" });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Suggestion for Trivia");

        startActivity(Intent.createChooser(emailIntent,
                getString(R.string.send_suggestion_in_)));

    }

    @Override
    public void onSendQuestionClicked() {
        Intent intent = new Intent(this,
                ActivitySuggestQuestion.class);
        startActivity(intent);
    }

    public static class TabsAdapter extends FragmentPagerAdapter implements
            ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mActionBar = activity.getSupportActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);

        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(),
                    info.args);
        }

        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        public void onPageScrollStateChanged(int state) {
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i = 0; i < mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        public void setTabPage(int i) {
            mViewPager.setCurrentItem(i);

        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

    }

    private void startNewGame(Bundle extras){
        mCurrentGame = new Intent(this, ActivityGame.class);
        mCurrentGame.putExtra(EXTRA_GAME_USER_ID, mCurrentUserId);
        mCurrentGame.putExtras(extras);
        startActivity(mCurrentGame);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //
        switch (requestCode) {
//            case REQUEST_CODE_BACK_FROM_ACTIVITY_WIZARD_SETUP:
//                mFirstTimeStartingDoNotTryToUpdate = false;
//                mCurrentUserId = resultCode;
//                changeToDefault();
//                break;
//            case REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER:
//                mCurrentUserId = resultCode;
//                // Log.v(TAG, "m_CurrentUserId --> " + m_CurrentUserId);
//                changeToDefault();
//                break;
//            case REQUEST_CODE_BACK_FROM_PREFERENCES:
//                mUpdateManager.updateServerIpFromPreferences();
//                break;
            case REQUEST_CODE_START_GAME_CATEGORIES:
                if (data != null) {

                    Bundle extras = data.getExtras();

                    extras.putInt(EXTRA_GAME_TYPE,
                            GAMETYPE_CATEGORIES);
                    startNewGame(extras);
                }

                break;

        }
    }


}
