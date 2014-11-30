package com.ualberta.team17.controller;

import java.util.Calendar;

import android.graphics.Bitmap;
import android.net.Uri;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.StoragePolicy;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.DateComparator;
import com.ualberta.team17.datamanager.comparators.IdentityComparator;

public class QAController {
	private static QAController mControllerInstance;
	DataManager mDataManager;
	
	/**
	 * Constructor, from a DataManager to act on
	 * @param model
	 */
	public QAController(DataManager model) {
		mDataManager = model;
		mControllerInstance = this;
	}
	
	public static QAController getInstance() {
		return mControllerInstance;
	}
	
	/**
	 * Log in using a given user context
	 * @param ctx
	 */
	public void login(UserContext ctx) {
		mDataManager.setUserContext(ctx);
	}

	public UserContext getUserContext() {
		return mDataManager.getUserContext();
	}
	
	/**
	 * Access existing objects via an arbitrary Filter & Sort
	 * @param filter
	 * @param sort
	 * @return An IncrementalResult
	 */
	public IncrementalResult getObjects(DataFilter filter, IItemComparator sort) {
		return mDataManager.doQuery(filter, sort);
	}
	
	/**
	 * Access existing objects via an arbitrary Filter & Sort, and load them into an existing result
	 * @param filter
	 * @param sort
	 * @param result The result to place new results into
	 * @return An IncrementalResult
	 */
	public void getObjectsWithResult(DataFilter filter, IncrementalResult result) {
		mDataManager.doQuery(filter, result);
	}

	/**
	 * Get all of the children of a given item
	 * @param question
	 * @param sort
	 * @return An incremental result that will be populated with the results
	 */
	public IncrementalResult getChildren(QAModel item, IItemComparator sort) {
		if (null == item) {
			return null;
		}
		
		return getChildren(item.getUniqueId(), sort);
	}
	
	/**
	 * Get all of the children of the item with the given ID
	 * @param question
	 * @param sort
	 * @return An incremental result that will be populated with the results
	 */
	public IncrementalResult getChildren(UniqueId item, IItemComparator sort) {
		DataFilter filter = new DataFilter();
		filter.addFieldFilter(AuthoredItem.FIELD_PARENT, item.toString(), FilterComparison.EQUALS);
		return mDataManager.doQuery(filter, sort);
	}
	
	/**
	 * Gets the current users favorites
	 * @param comparator A comparator specifying the order to sort the items in. If the comparator is
	 *                    null, then the items will be returned with the most recently posted items first.
	 * @return An incremental result that will be populated with the users favorites.
	 */
	public IncrementalResult getFavorites(IItemComparator comparator) {
		if (comparator == null) {
			comparator = new DateComparator();
		}
		return mDataManager.doQuery(
				mDataManager.getUserContext().getFavorites(), comparator);
	}
	
	/**
	 * Adds an item to the user's favorites
	 * @param item The item to favorite
	 */
	public void addFavorite(QAModel item) {
		mDataManager.favoriteItem(item);
	}
	
	/**
	 * Get the recently items that have been marked as recently viewed
	 * most recently with QAController::markRecentlyViewed
	 * @param comparator A comparator specifying the order to sort the items in. If the comparator is
	 *                    null, then the items will be left in the order
	 *                    "most recently viewed first".
	 * @return An IncrementalResult that will be populated with the recently viewed items
	 */
	public IncrementalResult getRecentItems(IItemComparator comparator) {
		if (comparator == null) {
			comparator = new IdentityComparator(); // that is, no comparator
		}
		return mDataManager.doQuery(
				mDataManager.getUserContext().getRecentItems(), 
				comparator);
	}
	
	/**
	 * Get the items that have been marked as to be viewed later
	 * most recently with QAController::markViewLater
	 * @return An IncrementalResult that will be populated with the to be viewed later items
	 */
	public IncrementalResult getViewLaterItems() {
		return mDataManager.doQuery(
				mDataManager.getUserContext().getViewLater(), 
				new IdentityComparator());
	}
	
	/**
	 * Mark an item as recently viewed
	 * Mainly to be used for questions.
	 * @param item
	 */
	public void markRecentlyViewed(QAModel item) {
		mDataManager.markRecentlyViewed(item);
	}
	
	/**
	 * Mark an item as to be viewed later
	 * Mainly to be used for questions
	 * @param item The item to mark as to be viewed later
	 */
	public void markViewLater(QAModel item) {
		mDataManager.markViewLater(item);
	}
	 
	/**
	 * Upvote a given question or answer
	 * @param target The item to upvote
	 */
	public void upvote(QAModel target) {
		UserContext creator = mDataManager.getUserContext();
		
		// The UniqueId for the upvote is a mixture of the user upvoting and the item being upvoted.
		// In that way a user will not be able to upvote the same item more than once, since both
		// upvotes will have the same uniqueId, and the DataManager layer will ignore the later
		// one.
		UniqueId id = new UniqueId(creator.getUserName() + "_Upvote_" + target.getUniqueId().toString());
		
		// Create and save the upvote item
		UpvoteItem up = new UpvoteItem(id, target.getUniqueId(), creator.getUserName(), Calendar.getInstance().getTime());
		up.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(up);
	}
	
	/**
	 * Create a question object, from a creator context, title, and body
	 * @param title
	 * @param body
	 * @return The item created
	 */
	public QuestionItem createQuestion(String title, String body) {
		UserContext creator = mDataManager.getUserContext();
		QuestionItem item = new QuestionItem(new UniqueId(creator), null, creator.getUserName(), Calendar.getInstance().getTime(), body, 0, title);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;
	}
	
	/**
	 * Create an answer with the given parent. A parent must be supplied.
	 * @param parent The object to use as the parent
	 * @param body The body text to apply to the answer
	 * @return The answer if it was created, or null if invalid.
	 */
	public AnswerItem createAnswer(QuestionItem parent, String body) {
		if (null == parent) {
			return null;
		}
		
		return createAnswer(parent.getUniqueId(), body);
	}
	
	/**
	 * Create an answer with the given parent. A parent must be supplied.
	 * @param parent The id of the object to use as parent
	 * @param body The body text to apply to the answer
	 * @return The answer if it was created, or null if invalid.
	 */
	public AnswerItem createAnswer(UniqueId parent, String body) {
		if (null == parent) {
			return null;
		}
		
		UserContext creator = mDataManager.getUserContext();
		AnswerItem item = new AnswerItem(new UniqueId(creator), parent, creator.getUserName(), Calendar.getInstance().getTime(), body, 0);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;	
	}
	
	/**
	 * Create a comment with the given parent. A parent must be supplied.
	 * @param parent The object to use as the parent
	 * @param body The body text to apply to the comment
	 * @return The comment if it was created, or null if invalid.
	 */
	public CommentItem createComment(AuthoredTextItem parent, String body) {
		if (null == parent) {
			return null;
		}
		
		return createComment(parent.getUniqueId(), body);
	}
	
	/**
	 * Create a comment with the given parent. A parent must be supplied.
	 * @param parent The id of the object to use as parent
	 * @param body The body text to apply to the comment
	 * @return The comment if it was created, or null if invalid.
	 */
	public CommentItem createComment(UniqueId parent, String body) {
		if (null == parent) {
			return null;
		}
		
		UserContext creator = mDataManager.getUserContext();
		CommentItem item = new CommentItem(new UniqueId(creator), parent, creator.getUserName(), Calendar.getInstance().getTime(), body, 0);
		item.setStoragePolicy(StoragePolicy.Cached);
		mDataManager.saveItem(item);
		return item;
	}
	
	/**
	 * Create an AttachmentItem from compressed image data
	 * @param parent The Id of the QuestionItem to have the attachment
	 * @param name The name of the attachment
	 * @param data The data contained in the attachment, compressed image 
	 *              data in some supported image format should be passed.
	 * @return The created and saved attachment item
	 */
	public AttachmentItem createAttachment(UniqueId parent, String name, byte[] data) {
		UserContext creator = mDataManager.getUserContext();
		AttachmentItem item = new AttachmentItem(new UniqueId(creator), 
				parent, 
				creator.getUserName(), 
				Calendar.getInstance().getTime(), 
				name, 
				data);
		mDataManager.saveItem(item);
		return item;
	}
	
	/**
	 * Create an AttachmentItew from a bitmap image.
	 * @param parent The Id of the QuestionItem to have the attachment
	 * @param name The name of the attachment
	 * @param image The image to attach
	 * @return The created and saved attachment item
	 */
	public AttachmentItem createAttachment(UniqueId parent, String name, Bitmap image) {
		UserContext creator = mDataManager.getUserContext();
		AttachmentItem item = new AttachmentItem(new UniqueId(creator), 
				parent, 
				creator.getUserName(), 
				Calendar.getInstance().getTime(), 
				name, 
				image);
		mDataManager.saveItem(item);
		return item;
	}
	
	/**
	 * Create an AttachmentItem from a URI of an image on the device. The image
	 * will be synchronously loaded in.
	 * @param parent The Id of the QuestionItem to have the attachment
	 * @param name The name of the attachment
	 * @param uri The URI of the image to attach
	 * @return The created and saved attachment item
	 */
	public AttachmentItem createAttachment(UniqueId parent, String name, Uri imageUri) {
		return createAttachment(parent, name, mDataManager.readImageFromUri(imageUri));
	}
}
