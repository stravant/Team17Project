package com.ualberta.team17.controller;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IItemComparator;
import com.ualberta.team17.datamanager.IncrementalResult;

public class QAController {
	/* Ctor */
	public QAController(DataManager model) {
		
	}
	
	/* Access existing content */
	public IncrementalResult getObjects(DataFilter filter, IItemComparator sort) {
		throw new UnsupportedOperationException();
	}
	public IncrementalResult getQuestionChildren(QuestionItem question, IItemComparator sort) {
		throw new UnsupportedOperationException();
	}
	
	/* Create new content */
	public QuestionItem createQuestion(String title, String body) {
		throw new UnsupportedOperationException();
	}
	public AttachmentItem createAttachment(QuestionItem parent) {
		throw new UnsupportedOperationException();
	}
	public AnswerItem createAnswer(QuestionItem parent, String body) {
		throw new UnsupportedOperationException();
	}
	public CommentItem createComment(AuthoredTextItem parent, String body) {
		throw new UnsupportedOperationException();
	}
}
