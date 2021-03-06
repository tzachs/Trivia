package com.tzachsolomon.trivia;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.util.Log;

public class XmlDataHandlerCategories extends DefaultHandler {

	public static final String TAG = XmlDataHandlerCategories.class
			.getSimpleName();
	private ArrayList<ContentValues> mCategories;
	private ContentValues mCategory;
	private boolean inCategoriesData;
	private boolean inCategoriesDataRow;
	private boolean inColId;
	private boolean inColParentId;
	private boolean inColEnName;
	private boolean inColHeName;
	private boolean inColLastUpdate;
	private XmlDataHandlerCategoriesListener mXmlDataHandlerCategoriesListener;

	public XmlDataHandlerCategories() {
		mCategories = new ArrayList<ContentValues>();
	}

	@Override
	public void startDocument() throws SAXException {
		//
		super.startDocument();
		if (mXmlDataHandlerCategoriesListener != null) {
			mXmlDataHandlerCategoriesListener.onStartDocument();
		}

	}

	@Override
	public void endDocument() throws SAXException {
		//
		super.endDocument();
		if (mXmlDataHandlerCategoriesListener != null) {
			mXmlDataHandlerCategoriesListener.onEndDocument();
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		//
		try {
			if (localName.contentEquals("table_data")) {
				inCategoriesData = false;

				if (attributes.getValue(0).contentEquals("categories")) {
					inCategoriesData = true;

				}
			} else if (localName.contentEquals("row")) {

				inCategoriesDataRow = false;

				setInColFalse();

				if (inCategoriesData) {
					inCategoriesDataRow = true;
					mCategory = new ContentValues();
				}

			} else if (localName.contentEquals("field")) {
				if (inCategoriesDataRow) {

					if (attributes.getValue(0).contentEquals("_id")) {
						inColId = true;

					} else if (attributes.getValue(0).contentEquals(
							"colParentId")) {

						inColParentId = true;

					}

					else if (attributes.getValue(0).contentEquals("colEnName")) {

						inColEnName = true;

					} else if (attributes.getValue(0)
							.contentEquals("colHeName")) {

						inColHeName = true;

					} else if (attributes.getValue(0).contentEquals(
							"colLastUpdate")) {

						inColLastUpdate = true;

					}

				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage().toString());
		}
	}

	private void setInColFalse() {
		//
		inColId = false;
		inColEnName = false;
		inColHeName = false;
		inColLastUpdate = false;
		inColParentId = false;

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		//
		
		if (localName.contentEquals("table_data")) {

			inCategoriesData = false;

		} else if (localName.contentEquals("row")) {

			if (inCategoriesDataRow){
				mCategories.add(mCategory);

			}
			
			setInColFalse();
			
			


		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		String chars = new String(ch, start, length);
		if (inColEnName) {
			mCategory.put(TriviaDbEngine.KEY_COL_EN_NAME, chars);
			inColEnName = false;
		} else if (inColHeName) {
			mCategory.put(TriviaDbEngine.KEY_COL_HE_NAME, chars);
			inColHeName = false;
		} else if (inColId) {
			mCategory.put(TriviaDbEngine.KEY_ROWID, chars);
			inColId = false;
		} else if (inColLastUpdate) {
			mCategory.put(TriviaDbEngine.KEY_LAST_UPDATE, chars);
			inColLastUpdate = false;
		} else if (inColParentId) {
			mCategory.put(TriviaDbEngine.KEY_COL_PARENT_ID, chars);
			inColParentId = false;
		}

	}

	public static interface XmlDataHandlerCategoriesListener {
		public void onEndDocument();

		public void onStartDocument();
	}

	public void setXmlDataHandlerCategoriesListener(
			XmlDataHandlerCategoriesListener i_Listener) {
		this.mXmlDataHandlerCategoriesListener = i_Listener;
	}

	public ContentValues[] getCategories() {
		//
		ContentValues[] ret = new ContentValues[mCategories.size()];

		mCategories.toArray(ret);

		return ret;

	}

}
