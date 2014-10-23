package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class QuestionViewActivity extends Activity {
	public final static String QUESTION_EXTRA = "QUESTION";
	private final static boolean DEBUG = true;
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQAItems;
	
	// this will get deleted once the global controller is figured out.
	private QAController qaController;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent questionIntent = this.getIntent();
		
		mQuestion = (QuestionItem) questionIntent.getSerializableExtra(QUESTION_EXTRA);
		if(mQuestion == null) {
			// We can just use this as the create question case
			if(DEBUG) {
				mQuestion = new QuestionItem(new UniqueId(), null, "Test Author", null, "Test Body", 0, "Test Title");
				AnswerItem answer1 = new AnswerItem(new UniqueId(), null, "ans1author", null, "ans1", 0);
				AnswerItem answer2 = new AnswerItem(new UniqueId(), null, "ans2author", null, "ans2", 0);
				CommentItem comment1 = new CommentItem(new UniqueId(), null, "c1a", null, "comment1", 0);
				CommentItem comment2 = new CommentItem(new UniqueId(), null, "c2a", null, "comment2", 0);
				CommentItem comment3 = new CommentItem(new UniqueId(), null, "c3a", null, "comment3", 0);
				
				QABody questionBody = new QABody(mQuestion);
				questionBody.comments.add(comment1);
				
				QABody answer1Body = new QABody(answer1);
				answer1Body.comments.add(comment2);
				answer1Body.comments.add(comment3);
				
				QABody answer2Body = new QABody(answer2);
				
				mQAItems.add(questionBody);
				mQAItems.add(answer1Body);
				mQAItems.add(answer2Body);
			}
		}
		else {
			mQAItems.add(createBodyItem(mQuestion));
			IncrementalResult questionChildren = qaController.getChildren(mQuestion, null);
			AnswerObserver answerObs = new AnswerObserver();
			questionChildren.addObserver(answerObs, ItemType.Answer);
		}
		
	}
	
	private QABody createBodyItem(QAModel item) {
		QABody body = new QABody(item);
		IncrementalResult commentResult = qaController.getChildren(item, null);
		CommentObserver commentObs = new CommentObserver(body);
		commentResult.addObserver(commentObs, ItemType.Comment);
		return body;
	}
	
	private class AnswerObserver implements IIncrementalObserver {
		@Override
		public void itemsArrived(List<QAModel> items, int index) {
			for(QAModel item : items) {
				mQAItems.add(createBodyItem(item));
			}
		}
	}
	
	private class CommentObserver implements IIncrementalObserver {
		private QABody mBody;
		public CommentObserver(QABody body) {
			mBody = body;
		}
		
		@Override
		public void itemsArrived(List<QAModel> items, int index) {
			for(QAModel item : items) {
				mBody.comments.add((CommentItem) item);
			}
		}
	}
	
	private class QABody {
		@SuppressWarnings("unused")
		public QAModel parent;
		public List<CommentItem> comments;
		
		public QABody(QAModel initParent) {
			parent = initParent;
			comments = new ArrayList<CommentItem>();
		}
	}

	
}
