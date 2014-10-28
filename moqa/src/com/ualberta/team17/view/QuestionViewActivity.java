package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.R;
import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.UniqueId;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivity extends Activity {
	public final static String QUESTION_EXTRA = "QUESTION";
	
	// Debug stuff - can be deleted later
	private final static boolean DEBUG = true;
	private final static String LIPSUM = "Lorem ipsum dolor sit amet, consectetur " +
			"adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna " +
			"aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
			"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
			"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
			"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia " +
			"deserunt mollit anim id est laborum.";
	
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQAItems;
	
	public QuestionViewActivity() {
		mQuestion = null;
		mQAItems = new ArrayList<QABody>();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionview);
		
		Intent questionIntent = this.getIntent();
		
		mQuestion = (QuestionItem) questionIntent.getSerializableExtra(QUESTION_EXTRA);
		if(mQuestion == null) {
			// TODO: implement Question Creation.
			if(DEBUG) {
				mQuestion = new QuestionItem(new UniqueId(), null, "Question Author",
						null, "Question: " + LIPSUM, 0, "Question Title");
				AnswerItem answer1 = new AnswerItem(new UniqueId(), null, "ans1 Author",
						null, "Answer 1: " + LIPSUM, 0);
				AnswerItem answer2 = new AnswerItem(new UniqueId(), null, "ans2 Author",
						null, "Answer 2: " + LIPSUM, 0);
				CommentItem comment1 = new CommentItem(new UniqueId(), null, "c1a", null, "comment1... I wanted a longer comment so yeah... words and things and stuff", 0);
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
			// TODO: Implement interactions with the controller to get Answers/Comments.
		}
		
	}
	
	/**
	 * This class holds a Question/Answer and its child Comments.
	 * 
	 * @author Corey
	 *
	 */
	private class QABody {
		@SuppressWarnings("unused")
		public AuthoredTextItem parent;
		public List<CommentItem> comments;
		
		public QABody(AuthoredTextItem initParent) {
			parent = initParent;
			comments = new ArrayList<CommentItem>();
		}
	}
	
	/**
	 * This method adds a QABody items to our current array of QABody items
	 * 
	 * @author Joel
	 *
	 */
	private void addQABodyItem(QABody item) {
		mQAItems.add(item);
	}
	
	private void addQABodyItem(AuthoredTextItem item) {
		QABody qaBodyItem = new QABody(item);
		addQABodyItem(qaBodyItem);
	}
	
	/**
	 * This method adds a comment to our current array of comments for a specified QABody item
	 * 
	 * @author Joel
	 *
	 */	
	private void addCommentItem(QABody item, CommentItem comment) {
		int position = mQAItems.indexOf(item);
		mQAItems.get(position).comments.add(comment);
	}
	
	/**
	 * Adapter for QABody. Connects the body of the Question/Answer
	 * with the bodyText field and Comments with the comments field.
	 * @author Corey + Joel
	 *
	 */
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
			
			TextView bodyTextView = (TextView) qaItemView.findViewById(R.id.bodyText);
			TextView authorTextView = (TextView) qaItemView.findViewById(R.id.authorText);
			
			LinearLayout commentsView = (LinearLayout) qaItemView.findViewById(R.id.commentView);			
			
			bodyTextView.setText(mObjects.get(position).parent.getBody());
			authorTextView.setText(mObjects.get(position).parent.getAuthor());
			
			for (int i=0; i<mObjects.get(position).comments.size(); i++){
				TextView comment = new TextView(mContext);
				comment.setText(mObjects.get(position).comments.get(i).getBody());
				
				TextView commentAuthor = new TextView(mContext);
				commentAuthor.setText("-" + mObjects.get(position).comments.get(i).getAuthor());	
				commentAuthor.setGravity(Gravity.RIGHT);
				
				commentsView.addView(comment);
				commentsView.addView(commentAuthor);
			}			
			// TODO: Implement favorite/upvote buttons.
			return qaItemView;
		}
	}
}
