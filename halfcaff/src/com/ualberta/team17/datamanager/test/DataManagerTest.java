package com.ualberta.team17.datamanager.test;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.LocalDataManager;
import com.ualberta.team17.datamanager.NetworkDataManager;
import com.ualberta.team17.datamanager.UserContext;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.comparators.IdComparator;
import com.ualberta.team17.datamanager.comparators.IdentityComparator;

public class DataManagerTest extends DataManagerTester<LocalDataManager> {
	NetworkDataManager netDataManager;
	DataManager manager;
	QAController controller;

	public static final String TEST_DATA = "[[\"Question\",{\"id\":\"c4ca4238a0b923820dcc509a6f75849b\",\"type\":\"question\",\"parent\":\"0\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testBody\",\"title\":\"testTitle\"}],[\"Answer\",{\"id\":\"c81e728d9d4c2f636f067f89cc14862c\",\"type\":\"answer\",\"parent\":\"c4ca4238a0b923820dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply\"}],[\"Answer\",{\"id\":\"eccbc87e4b5ce2fe28308fd9f2a7baf3\",\"type\":\"answer\",\"parent\":\"c4ca4238a0b923820dcc509a6f75849b\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"testReply2\"}],[\"Comment\",{\"id\":\"a87ff679a2f3e71d9181a67b7542122c\",\"type\":\"comment\",\"parent\":\"c81e728d9d4c2f636f067f89cc14862c\",\"author\":\"test_user\",\"date\":\"1970-01-01T00:00:00Z\",\"body\":\"comment!\"}]]";

	public void setUp() {
		// User context
		userContext = new UserContext("test_user");
		
		// Make local data manager
		dataManager = new LocalDataManager(getActivity());
		
		// Make net data manager
		Resources resources = getInstrumentation().getTargetContext().getResources();
		netDataManager = new NetworkDataManager(
				resources.getString(R.string.esTestServer),
				resources.getString(R.string.esTestIndex) + "_DataManagerTest");
		
		// Make the manager
		manager = new DataManager(getActivity(), dataManager, netDataManager);
		
		// Set the user context to blank
		manager.resetUserContextData();
		
		// Make the controller
		controller = new QAController(manager);
		controller.login(userContext);
		
		// Set the data to the test data set
		dataManager.writeTestData(TEST_DATA);
	}
	
	/**
	 * Basic test of DataManager functionality, querying for an item, and waiting 
	 * for it to arrive.
	 */
	public void test_DataManager() {		
		// Do a query
		DataFilter f = new DataFilter();
		f.addFieldFilter(AuthoredTextItem.FIELD_BODY, "testReply", FilterComparison.EQUALS);
		IncrementalResult r = controller.getObjects(f, new IdComparator());
		
		// Get the results
		assertTrue(waitForResults(r, 1));
		assertEquals("testReply", r.getCurrentResults().get(0).getField(AuthoredTextItem.FIELD_BODY));
	}
	
	/**
	 * Test adding several new answers to a question, and then querying back those
	 * answers via getChildren.
	 */
	public void test_AddSeveralChildren() {
		// Add the children
		QuestionItem q = controller.createQuestion("New Question", "Question body and stuff.");
		controller.createAnswer(q, "Answer 1 body.");
		controller.createAnswer(q, "Answer 2 body.");
		controller.createAnswer(q, "Answer 3 body.");
		controller.createComment(q, "Comment body.");
		
		// Query them back
		IncrementalResult r = controller.getChildren(q, new IdComparator());
		assertTrue("Did not get back 4 results.", waitForResults(r, 4));
		assertEquals(3, r.getCurrentResultsOfType(ItemType.Answer).size());
		assertEquals(1, r.getCurrentResultsOfType(ItemType.Comment).size());
	}
	
	/**
	 * Favorite a question from the test data set and check for it
	 */
	public void test_FavoriteItem() {
		// Add a question and favorite it
		QuestionItem q = controller.createQuestion("Test", "Test");
		
		// Check that isFavorited is not set
		assertFalse(q.isFavorited());
		
		// Favorite the question
		controller.addFavorite(q);
		
		// Query back and see if we get the favorited item
		IncrementalResult r = controller.getFavorites(null);
		assertTrue(waitForResults(r, 1));
		assertEquals(1, r.getCurrentResults().size());
		assertEquals(q.getUniqueId(), r.getCurrentResults().get(0).getUniqueId());
		
		// Check that the derived isFavorited flag is set
		assertTrue(q.isFavorited());
	}
	
	/**
	 * Add a couple of items to recently viewed and query them back
	 */
	public void test_RecentlyViewed() {
		// Add the questions
		QuestionItem q1 = controller.createQuestion("Question 1", "body.");
		QuestionItem q2 = controller.createQuestion("Question 2", "body.");
		
		// Mark the questions as viewed
		controller.markRecentlyViewed(q1);
		controller.markRecentlyViewed(q2);
		
		// Query back recently viewed
		IncrementalResult r = controller.getRecentItems(null);
		assertTrue(waitForResults(r, 2));
		assertEquals(q2.getUniqueId(), r.getCurrentResults().get(0).getUniqueId());
		assertEquals(q1.getUniqueId(), r.getCurrentResults().get(1).getUniqueId());
	}
	
	/**
	 * Add a couple of items and mark them as to be viewed later
	 * The should be in the to be viewed later list, and have the to be viewed later flag
	 * equal to true.
	 * Then mark them as viewed and verify that they are no longer to be viewed later.
	 */
	public void test_ViewLater() {
		// Add the questions
		QuestionItem q1 = controller.createQuestion("Question 1", "body.");
		QuestionItem q2 = controller.createQuestion("Question 2", "body.");
		
		// Check flags
		assertFalse(q1.getViewLater());
		assertFalse(q2.getViewLater());
		
		// Mark the questions as to be viewed later
		controller.markViewLater(q1);
		controller.markViewLater(q2);
		
		// Query back
		IncrementalResult r = controller.getViewLaterItems(null);
		assertTrue(waitForResults(r, 2));
		assertEquals(q2.getUniqueId(), r.getCurrentResults().get(0).getUniqueId());
		assertEquals(q1.getUniqueId(), r.getCurrentResults().get(1).getUniqueId());
		
		// Check flags
		assertTrue(q1.getViewLater());
		assertTrue(q2.getViewLater());
		
		// View one of them
		controller.markRecentlyViewed(q1);
		
		// Check change
		IncrementalResult r2 = controller.getViewLaterItems(null);
		assertTrue(waitForResults(r2, 1));
		assertEquals(q2.getUniqueId(), r2.getCurrentResults().get(0).getUniqueId());
		
		// Check flags
		assertTrue(q2.getViewLater());
		assertFalse(q1.getViewLater());
	}
	
	/**
	 * Check that the haveUpvoted field works (Have I upvoted an item), and that
	 * calling Controller::upvote multiple times does not result in multiple upvotes
	 * counted in upvotecount total.
	 */
	public void test_Upvoting() {
		// Add a question and upvote it
		QuestionItem q = controller.createQuestion("Question Title", "body.");
		
		// Check not upvoted yet, and the upvote count is right
		assertFalse(q.haveUpvoted());
		assertEquals(0, q.getUpvoteCount());
		
		// Upvote it
		controller.upvote(q);
		
		// Check that we have upvoted, and the upvote count is right
		assertTrue(q.haveUpvoted());
		assertEquals(1, q.getUpvoteCount());
		
		// Upvote again and check that there is not an additional upvote
		// in the upvote count.
		controller.upvote(q);
		assertEquals(1, q.getUpvoteCount());
	}
	
	/**
	 * Test attachment saving
	 */
	public void test_Attachment() {
		// Add a question
		QuestionItem q = controller.createQuestion("Question Title", "Body... I have an attachment, look:");
		
		// Check that we have no attachments
		assertFalse(q.hasAttachments());
		
		// Create an image to attach
		Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		b.setPixel(0, 0, 0xFFFFFFFF);
		byte[] bytes = AttachmentItem.encodeBitmap(b);

		// Attach them
		controller.createAttachment(q.getUniqueId(), "Image Attachment", b);
		controller.createAttachment(q.getUniqueId(), "Bytes Attachment", bytes);
		
		// Check that we get them back
		IncrementalResult r = controller.getChildren(q, new IdentityComparator());
		waitForResults(r, 2);
		assertEquals(2, r.getCurrentResultsOfType(ItemType.Attachment).size());
		
		// Check that the derived info is set
		assertTrue(q.hasAttachments());
	}
	
	/**
	 * Test that derived fields like upvoteCount can also be gotten through
	 * the getField interface of QAModel.
	 */
	public void test_GetField_DerivedField() {
		// Add some stuff, Question
		QuestionItem q = controller.createQuestion("New Question", "Question body and stuff.");
		
		// With two comments
		controller.createComment(q, "Comment body.");	
		controller.createComment(q, "Comment body 2.");
		
		// And three answers
		controller.createAnswer(q, "Answer 1 body.");
		controller.createAnswer(q, "Answer 2 body.");
		AnswerItem ans = controller.createAnswer(q, "Answer 3 body.");
		
		// And one upvote
		controller.upvote(q);
		
		// And upvote one of the answers
		controller.upvote(ans);
		
		// Now check the derived props
		
		// Check replyCount
		Object replyCount = q.getField(QuestionItem.FIELD_REPLIES);
		assertTrue(replyCount instanceof Integer);
		assertEquals(3, ((Integer)replyCount).intValue());
		
		// Check comment count
		Object commentCount = q.getField(AuthoredTextItem.FIELD_COMMENTS);
		assertTrue(commentCount instanceof Integer);
		assertEquals(2, ((Integer)commentCount).intValue());
		
		// Check upvote count
		Object upvoteCount = q.getField(AuthoredTextItem.FIELD_UPVOTES);
		assertTrue(upvoteCount instanceof Integer);
		assertEquals(1, ((Integer)upvoteCount).intValue());
	}
}
