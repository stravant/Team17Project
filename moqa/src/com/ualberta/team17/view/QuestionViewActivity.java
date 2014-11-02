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
	
	// Test stuff - can be deleted later
	private final static boolean GENERATE_TEST_DATA = true;
	private final static String LIPSUM = "Lorem ipsum dolor sit amet, consectetur " +
			"adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna " +
			"aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
			"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
			"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
			"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia " +
			"deserunt mollit anim id est laborum.";
	
	/*private QuestionItem mQuestion;
	private ArrayList<QABody> mQAItems;*/
	private QuestionContent mContent;
	
	/**
	 * This class holds all of the content of a Question and all
	 * relevant answers and comments.
	 * 
	 * It is responsible for ownership of all Question/Answer/Comment
	 * items.
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
	
	/**
	 * Constructor
	 */
	public QuestionViewActivity() {
		/*mQuestion = null;
		mQAItems = new ArrayList<QABody>();*/
		mContent = new QuestionContent();
	}
	
	/**
	 * Initializes data depending on what is passed in the intent. Creates adapters and
	 * listeners for all data interactions that will happen.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionview);
		
		Intent questionIntent = this.getIntent();
		
		mContent.setQuestion((QuestionItem) questionIntent.getSerializableExtra(QUESTION_EXTRA));
		if(mContent.getQuestion() == null) {
			// TODO: implement Question Creation.
			
			// Generate our own data to test displaying before the other modules work.
			if(GENERATE_TEST_DATA) {
				QuestionItem question = new QuestionItem(new UniqueId(), null, "Question Author",
						null, "Question: " + LIPSUM, 0, "Question Title");
				AnswerItem answer1 = new AnswerItem(new UniqueId(), question.mUniqueId, "ans1 Author",
						null, "Answer 1: " + LIPSUM, 0);
				AnswerItem answer2 = new AnswerItem(new UniqueId(), question.mUniqueId, "ans2 Author",
						null, "Answer 2: " + LIPSUM, 0);
				CommentItem comment1 = new CommentItem(new UniqueId(), question.mUniqueId, "c1a", null, "comment1... I wanted a longer comment so yeah... words and things and stuff", 0);
				CommentItem comment2 = new CommentItem(new UniqueId(), answer1.mUniqueId, "c2a", null, "comment2", 0);
				CommentItem comment3 = new CommentItem(new UniqueId(), answer1.mUniqueId, "c3a", null, "comment3", 0);
				
				mContent.setQuestion(question);
				mContent.addAnswers(answer1, answer2);
				mContent.addComments(comment1, comment2, comment3);
				
				TextView title = (TextView) findViewById(R.id.titleView);
				title.setText(mContent.getQuestion().getTitle());
				
				ListView qaList = (ListView) findViewById(R.id.qaItemView);
				QABodyAdapter adapter = new QABodyAdapter(this, R.id.qaItemView, mContent.getQABodies());
				
				qaList.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
		else {
			// TODO: Implement interactions with the controller to get Answers/Comments.
		}
		
	}		
}
