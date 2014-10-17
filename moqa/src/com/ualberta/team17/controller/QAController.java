package com.ualberta.team17.controller;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.datamanager.DataManager;
import com.ualberta.team17.datamanager.IncrementalResult;

public class QAController {
	/* Ctor */
	public QAController(DataManager model) {
		
	}
	
	/* Access existing content */
	public IncrementalResult getObjects(DataFilter filter, IItemComparator sort) {
		
	}
	public IncrementalResult getQuestionChildren(QuestionItem question, IItemComparator sort) {
		
	}
	
	/* Create new content */
	public QuestionItem createQuestion(String title, String body) {
		
	}
	public AttachmentItem createAttachment(QuestionItem parent) {
		
	}
	public AnswerItem createAnswer(QuestionItem parent, String body) {
		
	}
	public CommentItem createComment(AuthoredTextItem parent, String body) {
		
	}
}
