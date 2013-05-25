package com.tzachsolomon.trivia;

/**
 * Created by tzach on 5/25/13.
 */
public final class ClassCommonUtils {



    public static final int GAMETYPE_ALL_QUESTIONS = 1;
    public static final int GAMETYPE_LEVELS = 2;
    public static final int GAMETYPE_CATEGORIES = 3;

    public static final String INTENT_EXTRA_PREVIOUS_QUESTION_ID = "previousQuestionId";
    public static final String INTENT_EXTRA_PREVIOUS_QUESTION_STRING = "previousQuestionString";
    public static final String INTENT_EXTRA_CURRENT_QUESTION_ID = "currentQuestionId";
    public static final String INTENT_EXTRA_CURRENT_QUESTION_STRING = "currentQuestionString";

    public static final String INTENT_EXTRA_GAME_TYPE = "keyGameType";


    public static final String EXTRA_GAME_TYPE = "GameType";
    public static final String EXTRA_GAME_CATEGORIES = "GameCategories";
    public static final String EXTRA_GAME_START_LEVEL = "GameStartLevel";
    public static final String EXTRA_GAME_USER_ID = "GameUserId";

    public static final int REQUEST_CODE_START_GAME_CATEGORIES = 1;
    public static final int REQUEST_CODE_BACK_FROM_PREFERENCES = 2;
    public static final int REQUEST_CODE_BACK_FROM_ACTIVITY_USER_MANAGER = 3;
    public static final int REQUEST_CODE_BACK_FROM_ACTIVITY_WIZARD_SETUP = 4;
}
