package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;

/**
 * Created by tzach on 5/25/13.
 */
public class ActivityTriviaNew extends BaseGameActivity implements FragmentSingleGames.FragmentSingleGamesListener,
        FragmentAdmin.FragmentAdminListener, UpdateManager.QuestionsListener, UpdateManager.CategoriesListener,
        FragmentAchievementsAndScores.Listener{

    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private Intent mCurrentGame;
    private int mCurrentUserId;
    private int mLastLevel;
    private UpdateManager mUpdateManager;
    private int mLaterUpdateQuestionsCounter;
    private ProgressDialog mProgressDialog;
    private AlertDialog.Builder mAlertDialogBuilderUpdate;
    private Dialog mDialogUpdate;
    private SharedPreferences mSharedPreferences;
    private StringParser mStringParser;
    private boolean mUpdateQuestionsLater;
    private boolean mLaterRegisterUser;
    private AlertDialog.Builder mAlertDialogBuilderRegisterUser;

    private int mLaterRegisterUserCounter;
    private boolean mUpdateCategoriesLater;
    private FragmentAchievementsAndScores fragmentRefAchivevementAndScores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableDebugLog(true,"mytag1");

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.pager);

        setContentView(mViewPager);

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());


        initializeActionBar();

        initVariables();
        initializeDialogs();



    }

    private void initVariables() {

        mLastLevel = 1;
        mCurrentUserId = -2;
        mUpdateManager = new UpdateManager(this);
        mProgressDialog = new ProgressDialog(this);
        mStringParser = new StringParser(mSharedPreferences);

        mUpdateManager = new UpdateManager(this);
        mUpdateManager.setCategoriesListener(this);
        mUpdateManager.setQuestionsListener(this);


        mLaterRegisterUser = false;
        mLaterRegisterUserCounter = 0;
        mUpdateQuestionsLater = false;
        mLaterUpdateQuestionsCounter = 5;
    }

    private void initializeActionBar() {
        //

        ActionBar actionBar = getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        boolean showTitle = mSharedPreferences.getBoolean(
//                "checkBoxPrefShowTitle", true);
        boolean showTitle = false;
        actionBar.setDisplayHomeAsUpEnabled(showTitle);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.removeAllTabs();

        mTabsAdapter = new TabsAdapter(this, mViewPager);

        mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.single_games)),FragmentSingleGames.class,null);
        mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.score_and_achivments)),FragmentAchievementsAndScores.class,null);
        mTabsAdapter.addTab(actionBar.newTab().setText(getString(R.string.admin)),FragmentAdmin.class,null);


    }

    @Override
    public void onNewGameSimpleClicked() {
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_GAME_TYPE,
                GAMETYPE_LEVELS);
        if (mSharedPreferences.getBoolean("checkBoxPreferenceStartGameFromLastLevelPlayed",true)){
            mLastLevel = Integer.valueOf(mSharedPreferences.getString("lastLevelPlayed","1"));
        }else{
            mLastLevel = 1;
        }

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

    @Override
    public void onShowPreferencesClicked() {
        Intent pref = new Intent(this, ActivityPrefs.class);

        startActivityForResult(pref, REQUEST_CODE_BACK_FROM_PREFERENCES);
    }

    @Override
    public void onQuestionsCorrectRatioSent() {
        Toast.makeText(this,
                getString(R.string.thank_you_for_making_social_trivia_better_),
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onCheckIfQuestionUpdateAvailablePost(Bundle result) {
        boolean silentMode = mUpdateManager.getSilentMode();
        int numberOfNewQuestions = 0;
        int numberOfUpdatedQuestions = 0;

        if (result != null) {
            numberOfNewQuestions = result.getInt("newQuestions");
            numberOfUpdatedQuestions = result.getInt("updatedQuestions");
        }

        if ((numberOfNewQuestions + numberOfUpdatedQuestions) > 0) {
            StringBuilder message = new StringBuilder();

            message.append(getString(R.string.there_are_));
            message.append(numberOfNewQuestions);
            message.append(getString(R.string._new_questions_and_));
            message.append(numberOfUpdatedQuestions - numberOfNewQuestions);
            message.append(getString(R.string._updated_questions));

            mAlertDialogBuilderUpdate.setMessage(mStringParser
                    .reverseNumbersInStringHebrew(message.toString()));

            if (mDialogUpdate == null) {
                mDialogUpdate = mAlertDialogBuilderUpdate.show();
            } else if (mDialogUpdate.isShowing() == false) {
                mDialogUpdate.show();
            }

        } else if (silentMode == false) {
            Toast.makeText(this,
                    getString(R.string.no_update_available), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void initializeDialogs() {
        //
        mAlertDialogBuilderRegisterUser = new AlertDialog.Builder(
                this);

        mAlertDialogBuilderRegisterUser
                .setTitle(getString(R.string.register_user));
        mAlertDialogBuilderRegisterUser
                .setMessage(getString(R.string.register_a_user_));
        mAlertDialogBuilderRegisterUser.setPositiveButton(
                getString(R.string.yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
                //buttonManagerUsers_Clicked();

            }

        });
        mAlertDialogBuilderRegisterUser.setNegativeButton(
                getString(R.string.later),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        Toast.makeText(
                                ActivityTriviaNew.this,
                                getString(R.string.registering_a_user_gives_you_the_ability_to_publish_scores_play_against_other_players_etc),
                                Toast.LENGTH_LONG).show();
                        mLaterRegisterUser = true;

                    }
                });

        mAlertDialogBuilderUpdate = new AlertDialog.Builder(this);
        mAlertDialogBuilderUpdate.setCancelable(false);

        mAlertDialogBuilderUpdate.setPositiveButton(getString(R.string.update),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (mUpdateManager.getUpdateType()) {
                            case JSONHandler.TYPE_UPDATE_CATEGORIES:
                                mUpdateManager.updateCategoriesNow();
                                break;
                            case JSONHandler.TYPE_UPDATE_QUESTIONS:
                                mUpdateManager.updateQuestionsNow();
                                break;
                        }

                    }
                });
        mAlertDialogBuilderUpdate.setNegativeButton(getString(R.string.later),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        switch (mUpdateManager.getUpdateType()) {
                            case JSONHandler.TYPE_UPDATE_CATEGORIES:
                                mUpdateCategoriesLater = true;
                                break;
                            case JSONHandler.TYPE_UPDATE_QUESTIONS:
                                mUpdateQuestionsLater = true;
                                break;
                        }
                    }
                });

    }


    @Override
    public void onQuestionsUpdated(int i_UpdateFrom) {
        switch (i_UpdateFrom) {
            case TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET:
                Toast.makeText(
                        this,
                        getString(R.string.finished_importing_questions_from_internet),
                        Toast.LENGTH_LONG).show();
                break;
            case TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE:
                // starting to update the categories
                releaseButtonsOnImportFromXMLFile();
                Toast.makeText(
                        this,
                        getString(R.string.finished_importing_questions_from_initial_file),
                        Toast.LENGTH_LONG).show();
                Toast.makeText(this,
                        getString(R.string.importing_categories_from_initial_file),
                        Toast.LENGTH_LONG).show();
                // Log.v(TAG, "Starting import categories from XML");
                mUpdateManager.importCategoriesFromXml();

                break;

            default:
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = false;
        switch (item.getItemId()){
            case android.R.id.home:
                showAbout();
                ret = true;
            break;

        }
        return ret;
    }

    private void showAbout() {

        Intent intent = new Intent(this, ActivityAbout.class);
        startActivity(intent);
    }

    private void lockButtonOnImportFromXMLFile() {
        //
        setLockReleaseButtons(false);

    }

    private void releaseButtonsOnImportFromXMLFile() {
        setLockReleaseButtons(true);

    }

    @Override
    public void updateQuestionProgress(int i_Progress, int i_Max) {
        mProgressDialog.setMax(i_Max);
        mProgressDialog.setProgress(i_Progress);

    }

    @Override
    public void onUpdateQuestionsPostponed() {
        mUpdateQuestionsLater = true;
        mLaterUpdateQuestionsCounter = 5;

    }

    private void setLockReleaseButtons(boolean b) {
        //
//        buttonGameScores.setEnabled(b);
//        buttonNewGameAllQuestions.setEnabled(b);
//        buttonNewGameCategories.setEnabled(b);
//        // dbuttonNewGameSimple.setEnabled(b);
//        buttonUpdateDatabase.setEnabled(b);
//        buttonManageDatabase.setEnabled(b);
//        buttonManageUsers.setEnabled(b);
    }

    @Override
    public void onCategoriesUpdated(int i_UpdateFrom) {
        switch (i_UpdateFrom) {
            case TriviaDbEngine.TYPE_UPDATE_FROM_INTERNET:
                Toast.makeText(
                        this,
                        getString(R.string.finished_updating_categories_from_internet),
                        Toast.LENGTH_LONG).show();
                break;
            case TriviaDbEngine.TYPE_UPDATE_FROM_XML_FILE:

                Toast.makeText(
                        this,
                        getString(R.string.finished_importing_categories_from_initial_file),
                        Toast.LENGTH_LONG).show();
                //mButtonsLockedDueToImportFromXML = false;
                mProgressDialog.dismiss();

                //showUserRegister();

                break;

            default:
                break;
        }

    }

    @Override
    public void onCheckIfCategoriesUpdateAvailablePost(Bundle result) {
        boolean silentMode = mUpdateManager.getSilentMode();
        int numberOfNewCategories = 0;
        int numberOfUpdatedCategories = 0;

        if (result != null) {
            numberOfNewCategories = result.getInt("newCategories");
            numberOfUpdatedCategories = result.getInt("updatedCategories");
        }

        if ((numberOfNewCategories + numberOfUpdatedCategories) > 0) {
            StringBuilder message = new StringBuilder();

            message.append(R.string.there_are_);
            message.append(numberOfNewCategories);
            message.append(" new categories and ");
            message.append(numberOfNewCategories);
            message.append(" updated categories");

            mAlertDialogBuilderUpdate.setMessage(mStringParser
                    .reverseNumbersInStringHebrew(message.toString()));

            if (mDialogUpdate == null) {
                mDialogUpdate = mAlertDialogBuilderUpdate.show();
            } else if (mDialogUpdate.isShowing() == false) {
                mDialogUpdate.show();
            }

        } else if (silentMode == false) {
            Toast.makeText(this,
                    getString(R.string.no_update_available), Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public void onUpdateCategoriesPostponed() {
        mUpdateCategoriesLater = true;
    }

    @Override
    public void onButtonSignInClicked() {
        beginUserInitiatedSignIn();
    }

    @Override
    public void onButtonSignOutClicked() {
        signOut();
        fragmentRefAchivevementAndScores.setSignInVisible(true);
    }

    @Override
    public void onButtonShowScoreLeadersClicked() {

    }

    @Override
    public void onButtonShowAchievements() {
        startActivityForResult(getGamesClient().getAchievementsIntent(), GameHelper.RC_UNUSED );

    }

    public void setFragmentRefAchivevementAndScores(FragmentAchievementsAndScores fragmentRefAchivevementAndScores) {
        this.fragmentRefAchivevementAndScores = fragmentRefAchivevementAndScores;
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
        super.onActivityResult(requestCode,resultCode,data);
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
            case REQUEST_CODE_BACK_FROM_PREFERENCES:
                mUpdateManager.updateServerIpFromPreferences();
                break;
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


    @Override
    public void onSignInFailed() {
        fragmentRefAchivevementAndScores.setSignInVisible(true);
    }

    @Override
    public void onSignInSucceeded() {
        fragmentRefAchivevementAndScores.setSignInVisible(false);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
