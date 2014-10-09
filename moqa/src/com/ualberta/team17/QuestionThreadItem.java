package com.ualberta.team17;

/*
 * Simple wrapper around both a QAItem, and it's child comments.
 */
public class QuestionThreadItem<ItemT extends QAItem> {
	ItemT mItem;
	List<CommentItem> mCommentList;
}
