package com.ualberta.team17.controller;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QAItem;
import com.ualberta.team17.QuestionItem;

public interface IFullThreadView {
	/* Clear the view */
	void reset();
	
	/* The question item has arrived */
	void addQuestion(QuestionItem question);
	
	/* An answer item has arrived */
	void addAnswer(AnswerItem answer);
	
	/* Add a comment to an answer */
	void addComment(QAItem parent, CommentItem comment);
}
