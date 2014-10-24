package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.R;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivity extends Activity {
	public final static String QUESTION_EXTRA = "QUESTION";
	private final static boolean DEBUG = true;
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQAItems;
	
	// this will get deleted once the global controller is figured out.
	private QAController qaController;
	
	public QuestionViewActivity() {
		mQuestion = null;
		mQAItems = new ArrayList<QABody>();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionview);
		
		Intent questionIntent = this.getIntent();
		
		Log.e("onCreate", "TEST ERROR");
		
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
				
				TextView title = (TextView) findViewById(R.id.titleView);
				title.setText(mQuestion.getTitle());
				
				ListView qaList = (ListView) findViewById(R.id.qaItemView);
				QABodyAdapter adapter = new QABodyAdapter(this, R.id.qaItemView, mQAItems);
				
				qaList.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
		else {
			mQAItems.add(createBodyItem(mQuestion));
			IncrementalResult questionChildren = qaController.getChildren(mQuestion, null);
			AnswerObserver answerObs = new AnswerObserver();
			questionChildren.addObserver(answerObs, ItemType.Answer);
		}
		
	}
	
	private QABody createBodyItem(AuthoredTextItem item) {
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
				mQAItems.add(createBodyItem((AuthoredTextItem)item));
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
		public AuthoredTextItem parent;
		public List<CommentItem> comments;
		
		public QABody(AuthoredTextItem initParent) {
			parent = initParent;
			comments = new ArrayList<CommentItem>();
		}
	}
	
	private class QABodyAdapter extends ArrayAdapter<QABody> {
		Context mContext;
		List<QABody> mObjects;
		
		public QABodyAdapter(Context context, int textViewResourceId,
				List<QABody> objects) {
			super(context, textViewResourceId, objects);
			mContext = context;
			mObjects = objects;
		}
		

		public View getView( int position, View convertView, ViewGroup parent ) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View qaItemView = inflater.inflate(R.layout.qaitem, parent, false);
			
			View test = findViewById(R.id.bodyText);
			View test2 = qaItemView.findViewById(R.id.bodyText);
			TextView bodyTextView = (TextView) test2;
			TextView bodyTextView1 = (TextView) test;
			
			bodyTextView.setText(mObjects.get(position).parent.getBody());
			
			ListView commentListView = (ListView) qaItemView.findViewById(R.id.commentView);
			CommentAdapter commentAdapter = new CommentAdapter(mContext, R.id.commentView, mQAItems.get(position).comments);
			commentListView.setAdapter(commentAdapter);
			commentAdapter.notifyDataSetChanged();
			return qaItemView;
		}
	}
	
	private class CommentAdapter extends ArrayAdapter<CommentItem> {
		Context mContext;
		List<CommentItem> mComments;
		
		public CommentAdapter(Context context, int listViewResourceId, List<CommentItem> objects) {
			super(context, listViewResourceId, objects);
			mContext = context;
			mComments = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View commentView = inflater.inflate(R.layout.comment, parent, false);
			
			CommentItem comment = mComments.get(position);
			TextView commentText = (TextView) commentView.findViewById(R.id.commentText);
			
			commentText.setText(comment.getBody());
			commentText.getLayout();
			if(commentView.getMinimumHeight() > commentView.getHeight()) {
				commentText.setText("Too small!");
			}
			Log.i("asdf", new Integer(commentView.getMinimumHeight()).toString());
			
			return commentView;
		}
	}

	
}
