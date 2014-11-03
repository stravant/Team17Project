package com.ualberta.team17.controller;

import java.util.Calendar;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.StoragePolicy;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;

public class QAController {
	DataManager mDataManager;
	
	/**
	 * Constructor, from a DataManager to act on
	 * @param model
	 */
	public QAController(DataManager model) {
		mDataManager = model;
	}
	
	/**
	 * Access existing objects via a Filter & Sort
	 * @param filter
	 * @param sort
	 * @return An IncrementalResult
	 */
	public IncrementalResult getObjects(DataFilter filter, IItemComparator sort) {
		return mDataManager.doQuery(filter, sort);
	}
	
	/**
	 * Get all of the answers for a given question
	 * @param question
	 * @param sort
	 * @return
	 */
	public IncrementalResult getQuestionChildren(QuestionItem question, IItemComparator sort) {
		DataFilter filter = new DataFilter();
		filter.addFieldFilter("id", question.getUniqueId().toString(), FilterComparison.EQUALS);
		return mDataManager.doQuery(filter, sort);
	}
	
	/**
	 * Create a question, from a creator context, title, and body
	 * @param title
	 * @param body
	 * @return
	 */
	public QuestionItem createQuestion(UserContext creator, String title, String body) {
		QuestionItem item = new QuestionItem(new UniqueId(creator), null, creator.getUserName(), Calendar.getInstance().getTime(), body, 0, title);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;
	}
	
	/**
	 * Create an answer to a question
	 * @param parent
	 * @param body
	 * @return
	 */
	public AnswerItem createAnswer(UserContext creator, QuestionItem parent, String body) {
		AnswerItem item = new AnswerItem(new UniqueId(creator), parent.getUniqueId(), creator.getUserName(), Calendar.getInstance().getTime(), body, 0);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;	
	}
	
	/**
	 * Create an attachment
	 * @param parent
	 * @return
	 */
	public AttachmentItem createAttachment(QuestionItem parent) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Create a comment
	 * @param parent
	 * @param body
	 * @return
	 */
	public CommentItem createComment(AuthoredTextItem parent, String body) {
		throw new UnsupportedOperationException();
	}
}
