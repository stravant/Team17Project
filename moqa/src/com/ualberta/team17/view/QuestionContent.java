package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;

/**
 * This class holds all of the content of a Question and all
 * relevant answers and comments.
 * 
 * It is responsible for aggregation of all Question/Answer/Comment
 * items, as well as associating Answers and Comments to the correct
 * parent via their ParentId.
 * 
 * It will make it easier to test the view by dependency injection.
 * 
 * @author Corey
 */
public class QuestionContent {
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQABodies;
	
	public QuestionContent() {
		mQuestion = null;
		mQABodies = new ArrayList<QABody>();
	}
	
	public QuestionContent(QuestionItem question) {
		mQuestion = question;
		mQABodies = new ArrayList<QABody>();
		
		if(question != null) {
			mQABodies.add(new QABody(mQuestion));
		}
	}
	
	/**
	 * Getter for the QuestionItem.
	 * @return The question.
	 */
	public QuestionItem getQuestion() {
		return mQuestion;
	}
	
	/**
	 * Getter for the QABodies.
	 * @return The list of QABodies.
	 */
	public List<QABody> getQABodies() {
		return mQABodies;
	}
	
	/**
	 * Sets the question.
	 * 
	 * Also removes the old question from the list of QABodies and adds the new one.
	 * @param question The question to use.
	 */
	public void setQuestion(QuestionItem question) {
		if(mQuestion != null) {
			mQABodies.remove(mQuestion);
		}
		
		mQuestion = question;
		if(question != null) {
			mQABodies.add(new QABody(mQuestion));
		}
	}
	
	/**
	 * Adds all answers passed to it to the QABody list
	 * 
	 * @param answers A list of AnswerItems.
	 */
	public void addAnswers(AnswerItem... answers) {
		for(AnswerItem answer : answers) {
			QABody answerBody = new QABody(answer);
			mQABodies.add(answerBody);
		}
	}
	
	/**
	 * Adds all comments to their corresponding parent
	 * Question/Answer by the id.
	 * 
	 * @param comments A list of CommentItems.
	 */
	public void addComments(CommentItem... comments) {
		for(CommentItem comment : comments) {
			QABody parentBody = findById(comment.getParentItem());
			if(parentBody != null) {
				parentBody.comments.add(comment);
			}
			else {
				// maybe some kind of error
			}
		}
	}
	
	public ListAdapter getListAdapter(Context context, int textViewResourceId) {
		return new QABodyAdapter(context, textViewResourceId, mQABodies);
	}
	
	/**
	 * Finds a QABody by its unique id. Returns null if not found.
	 * 
	 * @param id The id to search for.
	 * @return The matching QABody.
	 */
	private QABody findById(UniqueId id) {
		for(QABody body : mQABodies) {
			if(id == body.parent.mUniqueId) {
				return body;
			}
		}
		return null;
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