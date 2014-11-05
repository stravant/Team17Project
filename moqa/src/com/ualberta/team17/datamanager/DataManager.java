package com.ualberta.team17.datamanager;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ualberta.team17.*;

public class DataManager {
	private UserContext mContext;
	private List<IDataSourceManager> mDataManagers;

	public IncrementalResult doQuery(DataFilter filter, IItemComparator sortComparator) {
		throw new UnsupportedOperationException();
	}

	public void saveItem(QAModel item) {
		throw new UnsupportedOperationException();
	}

	public void setUserContext(UserContext context) {
		mContext = context;
	}

	public UserContext getUserContext() {
		return mContext;
	}

	public static Gson getGsonObject() {
		// TODO: This is missing attachment items
		return new GsonBuilder()
			.registerTypeAdapter(AnswerItem.class, new AnswerItem.GsonTypeAdapter())
			.registerTypeAdapter(CommentItem.class, new CommentItem.GsonTypeAdapter())
			.registerTypeAdapter(QuestionItem.class, new QuestionItem.GsonTypeAdapter())
			.registerTypeAdapter(UpvoteItem.class, new UpvoteItem.GsonTypeAdapter())
			.serializeNulls()
			.create();
	}
}
