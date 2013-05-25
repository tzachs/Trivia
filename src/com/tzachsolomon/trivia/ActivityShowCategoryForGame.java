package com.tzachsolomon.trivia;

import static com.tzachsolomon.trivia.ClassCommonUtils.*;

import java.util.ArrayList;

import com.tzachsolomon.trivia.UpdateManager.CategoriesListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.DisplayMetrics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;

public class ActivityShowCategoryForGame extends ExpandableListActivity
		implements OnClickListener, CategoriesListener {

	private MyBaseExpandableListAdapter m_Adapter;
	private Items mItems;

	private ExpandableListView m_ExpandableList;
	private Button buttonStartCategoryGame;
	private Button buttonUpdateCategories;
	private TriviaDbEngine mDbEngine;
	private UpdateManager mUpdateManager;

	private AlertDialog.Builder mAlertDialogBuilderUpdateQuestions;
	private Dialog mDialogUpdateQuestions;
	private SharedPreferences mSharedPreferences;
	private StringParser mStringParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_show_category_for_game);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		initialiazeVariables();

		setIndicatorToRight();

		initItemsFromDatabase();
		initializeDialogs();

	}

	private void initializeDialogs() {
		//
		mAlertDialogBuilderUpdateQuestions = new AlertDialog.Builder(this);
		mAlertDialogBuilderUpdateQuestions.setCancelable(false);

		mAlertDialogBuilderUpdateQuestions.setPositiveButton(
				getString(R.string.update),
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
		mAlertDialogBuilderUpdateQuestions.setNegativeButton(
				getString(R.string.later),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//
						switch (mUpdateManager.getUpdateType()) {
						case JSONHandler.TYPE_UPDATE_CATEGORIES:

							break;
						case JSONHandler.TYPE_UPDATE_QUESTIONS:

							break;
						}
					}
				});

	}

	private void initialiazeVariables() {
		//

		mStringParser = new StringParser(mSharedPreferences);

		mDbEngine = new TriviaDbEngine(this);

		mUpdateManager = new UpdateManager(this);
		mUpdateManager.setCategoriesListener(this);

		buttonStartCategoryGame = (Button) findViewById(R.id.buttonStartCategoryGame);
		buttonUpdateCategories = (Button) findViewById(R.id.buttonUpdateCategories);

		buttonStartCategoryGame.setOnClickListener(this);
		buttonUpdateCategories.setOnClickListener(this);

		setButtonsVisibilaty();

	}

	@Override
	protected void onResume() {
		//
		super.onResume();

		mUpdateManager.updateCategories(true);
	}

	private void setButtonsVisibilaty() {
		if (mDbEngine.isCategoriesEmpty()) {
			buttonUpdateCategories.setVisibility(View.VISIBLE);
			buttonStartCategoryGame.setVisibility(View.GONE);
		} else {
			buttonUpdateCategories.setVisibility(View.GONE);
			buttonStartCategoryGame.setVisibility(View.VISIBLE);
		}
	}

	private void initItemsFromDatabase() {
		//

		ContentValues[] categories;
		ContentValues[] subCategories;
		int i, length, j, jlength, categoryId, currentGroupId;

		categories = mDbEngine.getPrimaryCategories();

		mItems = new Items();

		for (i = 0, length = categories.length; i < length; i++) {
			String categoryText = categories[i]
					.getAsString(TriviaDbEngine.KEY_COL_HE_NAME);
			categoryId = categories[i].getAsInteger(TriviaDbEngine.KEY_ROWID);
			subCategories = mDbEngine.getSubCategories(categoryId);

			// checking how many sub categories this category has
			jlength = subCategories.length;

			// add primary categories
			currentGroupId = mItems.addEmptyGroup(categoryText, false,
					categoryId);

			// adding sub categories
			for (j = 0; j < jlength; j++) {
				mItems.addChildToGroup(
						currentGroupId,
						categoryText
								+ " - "
								+ subCategories[j]
										.getAsString(TriviaDbEngine.KEY_COL_HE_NAME),
						false, subCategories[j]
								.getAsInteger(TriviaDbEngine.KEY_ROWID));

			}

		}

		m_Adapter = new MyBaseExpandableListAdapter(this, mItems);

		m_ExpandableList = getExpandableListView();

		m_ExpandableList.setAdapter(m_Adapter);

		setButtonsVisibilaty();

	}

	private void setIndicatorToRight() {

		DisplayMetrics metrics;
		int width;

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width = metrics.widthPixels;
		// this code for adjusting the group indicator into right side of the
		// view
		getExpandableListView().setIndicatorBounds(
				width - GetDipsFromPixel(50), width - GetDipsFromPixel(10));

		getExpandableListView().setChoiceMode(
				ExpandableListView.CHOICE_MODE_MULTIPLE);
	}

	public int GetDipsFromPixel(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	public class MyBaseExpandableListAdapter extends BaseExpandableListAdapter {

		private Items m_Items;
		private Context m_Context;

		public MyBaseExpandableListAdapter(Context i_Context, Items i_Items) {
			m_Items = i_Items;
			m_Context = i_Context;
		}

		@Override
		public Object getChild(int arg0, int arg1) {
			//
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			//
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			//
			CheckBox cb;
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) m_Context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.expand_group_row,
						null);

			}

			cb = (CheckBox) convertView.findViewById(R.id.checkBoxGroupRow);
			cb.setTag("" + groupPosition + "," + childPosition);
			cb.setText(m_Items.getChildText(groupPosition, childPosition));
			cb.setChecked(m_Items.getChildState(groupPosition, childPosition));
			cb.setOnCheckedChangeListener(onCheckedChangedChild);
			cb.setTextColor(Color.RED);

			return convertView;

		}

		@Override
		public int getChildrenCount(int groupPosition) {
			//
			return m_Items.getChildrenCount(groupPosition);
		}

		@Override
		public Object getGroup(int groupPosition) {
			//
			return null;
		}

		@Override
		public int getGroupCount() {
			//
			return m_Items.getGroupCount();
		}

		@Override
		public long getGroupId(int groupPosition) {
			//
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			//
			CheckBox cb;
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) m_Context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.expand_group_row,
						null);

			}

			cb = (CheckBox) convertView.findViewById(R.id.checkBoxGroupRow);
			cb.setTag("" + groupPosition);
			cb.setText(m_Items.getGroupText(groupPosition));
			cb.setChecked(m_Items.getGroupState(groupPosition));
			cb.setOnCheckedChangeListener(onCheckedChangedGroup);

			View ind = convertView.findViewById(R.id.explist_indicator);
			if (ind != null) {
				if (getChildrenCount(groupPosition) == 0) {
					ind.setVisibility(View.INVISIBLE);
				} else {
					ind.setVisibility(View.VISIBLE);
					if (isExpanded) {
						ind.setBackgroundDrawable(getResources().getDrawable(
								R.drawable.arrow_return_right_up));
					} else {
						ind.setBackgroundDrawable(getResources().getDrawable(
								R.drawable.arrow_right));
					}

				}
			}

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			//
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			//
			return false;
		}

		OnCheckedChangeListener onCheckedChangedGroup = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				//
				String tag = buttonView.getTag().toString();
				int groupId = Integer.valueOf(tag);
				m_Items.setGroupState(groupId, isChecked);
				m_Items.setGroupChildrenState(groupId, isChecked);

				m_Adapter.notifyDataSetChanged();

			}
		};

		OnCheckedChangeListener onCheckedChangedChild = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				//
				String tag = buttonView.getTag().toString();
				int groupId = Integer.valueOf(tag.split(",")[0]);
				int childId = Integer.valueOf(tag.split(",")[1]);
				m_Items.setChildState(groupId, childId, isChecked);
			}
		};

	}

	public class ItemClass {

		private String m_Text;
		private boolean m_State;
		private int m_Id;

		public ItemClass(boolean i_State, String i_Text, int i_Id) {
			m_Text = i_Text;
			m_State = i_State;
			m_Id = i_Id;

		}

		public void setState(boolean i_State) {
			//
			m_State = i_State;

		}

		public boolean getState() {
			//
			return m_State;
		}

		public CharSequence getText() {
			//
			return m_Text;
		}

		public int getId() {
			return m_Id;
		}

	}

	/**
	 * @author tzach
	 * 
	 */
	public class Items {

		private ArrayList<ItemClass> m_GroupItems;
		private ArrayList<ArrayList<ItemClass>> m_ChildItems;

		public Context m_Context;

		public Items() {
			m_GroupItems = new ArrayList<ActivityShowCategoryForGame.ItemClass>();
			m_ChildItems = new ArrayList<ArrayList<ItemClass>>();

		}

		public void setGroupChildrenState(int groupId, boolean isChecked) {
			//
			for (ItemClass item : m_ChildItems.get(groupId)) {
				item.setState(isChecked);
			}

		}

		public void addChildToGroup(int i_GroupId, String i_ChildText,
				boolean i_ChildState, int i_ChildId)

		{
			//
			if (i_GroupId < m_GroupItems.size()) {
				ItemClass child = new ItemClass(i_ChildState, i_ChildText,
						i_ChildId);

				m_ChildItems.get(i_GroupId).add(child);

			}

		}

		public void setChildState(int groupId, int childId, boolean isChecked) {
			//
			m_ChildItems.get(groupId).get(childId).setState(isChecked);

		}

		public boolean getChildState(int groupPosition, int childPosition) {
			//

			return m_ChildItems.get(groupPosition).get(childPosition)
					.getState();
		}

		public CharSequence getChildText(int groupPosition, int childPosition) {
			//
			return m_ChildItems.get(groupPosition).get(childPosition).getText();
		}

		public void setGroupState(int groupId, boolean isChecked) {
			//
			m_GroupItems.get(groupId).setState(isChecked);

		}

		public boolean getGroupState(int groupPosition) {
			//
			return m_GroupItems.get(groupPosition).getState();
		}

		public int getGroupCount() {
			//
			return m_GroupItems.size();
		}

		public int getChildrenCount(int groupPosition) {
			//
			return m_ChildItems.get(groupPosition).size();
		}

		/**
		 * <strong>NOTICE!!! You must addChildrenToGroup after calling this
		 * function, if not, you'll receive an exception!!! </strong>
		 * 
		 * @param i_GroupName
		 * @param i_State
		 * @param i_Id
		 * @return
		 */
		public int addGroup(String i_GroupName, boolean i_State, int i_Id) {
			//
			ItemClass group = new ItemClass(i_State, i_GroupName, i_Id);

			m_GroupItems.add(group);

			// returning the new group id
			return m_GroupItems.size() - 1;
		}

		public int addEmptyGroup(String i_GroupName, boolean i_State, int i_Id) {

			int groupId;

			groupId = addGroup(i_GroupName, i_State, i_Id);
			addChildrenToGroup(groupId, null);

			return groupId;
		}

		public void addChildrenToGroup(int i_GroupId,
				ArrayList<ItemClass> i_Children) {

			if (i_GroupId < m_GroupItems.size()) {
				if (i_Children == null) {
					i_Children = new ArrayList<ActivityShowCategoryForGame.ItemClass>();
				}

				m_ChildItems.add(i_Children);

			}
		}

		public CharSequence getGroupText(int groupPosition) {
			//
			if (groupPosition < m_GroupItems.size()) {
				return m_GroupItems.get(groupPosition).getText();
			} else {
				return null;
			}
		}

		public ArrayList<Integer> getChosenIds() {
			//
			ArrayList<Integer> ret = new ArrayList<Integer>();
			int i, length;

			for (ItemClass item : m_GroupItems) {
				if (item.getState()) {
					ret.add(item.getId());
				}
			}

			for (i = 0, length = m_ChildItems.size(); i < length; i++) {

				for (ItemClass item : m_ChildItems.get(i)) {
					if (item.getState()) {
						ret.add(item.getId());
					}
				}
			}

			return ret;
		}

	}

	@Override
	public void onClick(View v) {
		//
		switch (v.getId()) {
		case R.id.buttonStartCategoryGame:
			buttonStartCategoryGame_Clicked();
			break;
		case R.id.buttonUpdateCategories:
			buttonUpdateCategories_Clicked();
			break;
		}

	}

	private void buttonUpdateCategories_Clicked() {
		//

		mUpdateManager.updateCategories(false);

	}

	private void buttonStartCategoryGame_Clicked() {
		//
		ArrayList<Integer> chosenIds = mItems.getChosenIds();

		if (chosenIds.size() > 0) {
			int i, length;

			int[] ids;
			length = chosenIds.size();
			ids = new int[length];
			for (i = 0; i < length; i++) {
				ids[i] = chosenIds.get(i);
			}
			Intent resultData = new Intent();
			resultData.putExtra(EXTRA_GAME_CATEGORIES, ids);
			setResult(1, resultData);
			finish();
		} else {
			Toast.makeText(this,
					getString(R.string.you_must_choose_at_least_1_category),
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onCategoriesUpdated(int i_UpdateFrom) {
		//
		initItemsFromDatabase();

	}

	@Override
	public void onUpdateCategoriesPostponed() {
		//

	}

	@Override
	public void onCheckIfCategoriesUpdateAvailablePost(
			Bundle result) {
		//

		boolean silentMode = mUpdateManager.getSilentMode();
		int numberOfUpdatedCategories = 0;
		int numberOfNewCategories = 0;
		if ( result != null){
			numberOfNewCategories = result.getInt("newCategories");
			numberOfUpdatedCategories = result.getInt("updatedCategories");
		}
		
		if ((numberOfUpdatedCategories +  numberOfNewCategories) > 0) {
			if (silentMode) {

				mUpdateManager.updateCategoriesNow();
			} else {
				StringBuilder message = new StringBuilder();

				message.append(getString(R.string.there_are_));
				message.append(numberOfNewCategories);
				message.append(getString(R.string._new_categories_and_));
				message.append(numberOfUpdatedCategories);
				message.append(getString(R.string._updated_categories));
				
				mAlertDialogBuilderUpdateQuestions.setMessage(mStringParser
						.reverseNumbersInStringHebrew(message.toString()));

				if (mDialogUpdateQuestions == null) {
					mDialogUpdateQuestions = mAlertDialogBuilderUpdateQuestions
							.show();
				} else if (mDialogUpdateQuestions.isShowing() == false) {
					mDialogUpdateQuestions.show();
				}
			}

		} else if (silentMode == false) {
			Toast.makeText(this, getString(R.string.no_update_available),
					Toast.LENGTH_SHORT).show();
		}

	}
}
