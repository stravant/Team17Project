package com.ualberta.team17.test;

import java.util.Date;

import com.google.gson.Gson;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.UpvoteItem;
import com.ualberta.team17.datamanager.DataManager;

import junit.framework.TestCase;

public class ItemSerializationTest extends TestCase {
	private void assertQAModelEquals(QAModel expected, QAModel other) {
		assertEquals(expected.getUniqueId(), other.getUniqueId());
		assertEquals(expected.getItemType(), other.getItemType());
		assertEquals(expected, other);
	}

	private void assertAuthoredItemEquals(AuthoredItem expected, AuthoredItem other) {
		assertQAModelEquals(expected, other);
		assertEquals(expected.getAuthor(), other.getAuthor());
		assertEquals(expected.getParentItem(), other.getParentItem());

		if (null == expected.getDate() || null == other.getDate()) {
			assertEquals(expected.getDate(), other.getDate());
		} else{
			assertEquals(expected.getDate().toString(), other.getDate().toString());
		}
	}

	private void assertAuthoredTextItemEquals(AuthoredTextItem expected, AuthoredTextItem other) {
		assertAuthoredItemEquals(expected, other);
		assertEquals(expected.getBody(), other.getBody());
		assertEquals(expected.getUpvoteCount(), other.getUpvoteCount());
	}

	private void assertQuestionItemEquals(QuestionItem expected, QuestionItem other) {
		assertAuthoredTextItemEquals(expected, other);
		assertEquals(expected.getTitle(), other.getTitle());
	}

	private void assertAttachmentItemEquals(AttachmentItem expected, AttachmentItem other) {
		assertAuthoredItemEquals(expected, other);
		assertEquals(expected.getData(), other.getData());
	}

	/**
	 * Tests that all fields in a question item are serialized/deserialized properly.
	 */
	public void testQuestionItemSerialization() {
		Gson gson = DataManager.getGsonObject();

		QuestionItem initialItem = new QuestionItem(
				UniqueId.fromString("ea449d0d1156e7368c6c360384261e10"), 
				UniqueId.fromString("abc4a20b880363a3b0c2aadc45db52f2"),
				"questionSerializationTestAuthor",
				new Date(81659369),
				"This is the serialization test question body",
				0,
				"This is the serialization test question title");

		assertQuestionItemEquals(
				initialItem, 
				gson.fromJson(gson.toJson(initialItem), QuestionItem.class));

		QuestionItem noParent = new QuestionItem(
				UniqueId.fromString("b2022836566381d6325ca8b5c45b7e5c"), 
				null,
				"questionSerializationTestAuthor2",
				new Date(),
				"This is the serialization test question body2",
				0,
				"This is the serialization test question title2");
		
		assertQuestionItemEquals(
				noParent, 
				gson.fromJson(gson.toJson(noParent), QuestionItem.class));
	}

	/**
	 * Tests that all fields in an answer item are serialized/deserialized properly.
	 */
	public void testAnswerItemSerialization() {
		Gson gson = DataManager.getGsonObject();

		AnswerItem initialItem = new AnswerItem(
				UniqueId.fromString("2b753c7d330e71c5e0205121c50b5cf2"), 
				UniqueId.fromString("cea8c6c8eba066021bdd45f93f75b413"),
				"answerSerializationTestAuthor",
				new Date(196949369),
				"This is the serialization test answer body",
				0);

		assertAuthoredTextItemEquals(
				initialItem, 
				gson.fromJson(gson.toJson(initialItem), AnswerItem.class));

		AnswerItem noParent = new AnswerItem(
				UniqueId.fromString("b2022836566381d6325ca8b5c45b7e5c"), 
				null,
				"answerSerializationTestAuthor2",
				new Date(),
				"This is the serialization test answer body2",
				0);

		assertAuthoredTextItemEquals(
				noParent, 
				gson.fromJson(gson.toJson(noParent), AnswerItem.class));
	}

	/**
	 * Tests that all fields in a comment item are serialized/deserialized properly.
	 */
	public void testCommentItemSerialization() {
		Gson gson = DataManager.getGsonObject();

		CommentItem initialItem = new CommentItem(
				UniqueId.fromString("32b4029a9c5dada047da838364cdf9b5"), 
				UniqueId.fromString("fbb78b71e5ea767d33c1d6bcff3d952d"),
				"commentSerializationTestAuthor",
				new Date(196949369),
				"This is the serialization test comment body",
				0);

		assertAuthoredTextItemEquals(
				initialItem, 
				gson.fromJson(gson.toJson(initialItem), CommentItem.class));

		CommentItem noParent = new CommentItem(
				UniqueId.fromString("e7477a24475a10c9dd39757c8387a234"), 
				null,
				"commentSerializationTestAuthor2",
				new Date(),
				"This is the serialization test comment body2",
				0);

		assertAuthoredTextItemEquals(
				noParent, 
				gson.fromJson(gson.toJson(noParent), CommentItem.class));
	}

	/**
	 * Tests that all fields in an upvote item are serialized/deserialized properly.
	 */
	public void testUpvoteItemSerialization() {
		Gson gson = DataManager.getGsonObject();

		UpvoteItem initialItem = new UpvoteItem(
				UniqueId.fromString("afec9e56592966a0dff5ba2e49008a3e"), 
				UniqueId.fromString("8f2c5ea88535ddd26a54a2e4be21da7b"), 
				"upvoteSerializationTestAuthor", 
				new Date(12394364));

		assertAuthoredItemEquals(
				initialItem, 
				gson.fromJson(gson.toJson(initialItem), UpvoteItem.class));

		UpvoteItem noParent = new UpvoteItem(
				UniqueId.fromString("113e83ce33b74c31b7a2980103a45771"), 
				UniqueId.fromString("fac2cc89f6de4cb81c81688b622772e8"), 
				"upvoteSerializationTestAuthor2", 
				new Date());

		assertAuthoredItemEquals(
				noParent, 
				gson.fromJson(gson.toJson(noParent), UpvoteItem.class));
	}

	/**
	 * Tests that all fields in an attachment item are serialized/deserialized properly.
	 */
	public void testAttachmentItemSerialization() {
		Gson gson = DataManager.getGsonObject();

		AttachmentItem initialItem = new AttachmentItem(
				UniqueId.fromString("afec9e56592966a0dff5ba2e49008a3e"), 
				UniqueId.fromString("8f2c5ea88535ddd26a54a2e4be21da7b"), 
				"attachmentSerializationTestAuthor", 
				new Date(91730474),
				"attachment1Name",
				new byte[0]);

		assertAttachmentItemEquals(
				initialItem, 
				gson.fromJson(gson.toJson(initialItem), AttachmentItem.class));

		AttachmentItem noParent = new AttachmentItem(
				UniqueId.fromString("7b1383ceeda2e8161a7b05f414650dd1"), 
				null, 
				"attachmentSerializationTestAuthor2", 
				new Date(),
				"attachment2Name",
				new byte[0]);

		assertAttachmentItemEquals(
				noParent, 
				gson.fromJson(gson.toJson(noParent), AttachmentItem.class));
	}
}
