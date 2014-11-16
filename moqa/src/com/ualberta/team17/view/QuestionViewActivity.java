package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IItemComparator.SortDirection;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.comparators.DateComparator;
import com.ualberta.team17.datamanager.comparators.IdComparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivity extends Activity implements IQAView {
	public final static String QUESTION_ID_EXTRA = "question_id";
	
	// Test - can be deleted later
	private final static boolean GENERATE_TEST_DATA = false;
	
	//protected QuestionContent mContent;
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQABodies;
	protected QAController mController; 	
	protected ArrayAdapter mAdapter;	
	
	
		
	/**
	 * Method that sets the question for mContent
	 * @author Joel
	 * @param question
	 */
	private void loadContent(QuestionItem question) {
		// make sure we aren't loading a mix of two questions at the same time		
		//mContent = new QuestionContent();
		setQuestion(question);
		TextView title = (TextView)findViewById(R.id.titleView);
		ListView listview = (ListView)findViewById(R.id.qaItemView);
		listview.setAdapter(getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView));
		title.setText(getQuestion().getTitle());
		IncrementalResult iRA = mController.getChildren(question, new DateComparator());
		iRA.addObserver(new AnswerResultListener(), ItemType.Answer);
		IncrementalResult iRC = mController.getChildren(question, new DateComparator());
		iRC.addObserver(new CommentResultListener(), ItemType.Comment);		
	}	
	
	
	/**
	 * Method that queries the controller for a question based on Id
	 * @author Joel
	 * @param id
	 */
	private void queryQuestion(UniqueId id) {		
		DataFilter dFilter = new DataFilter();
		dFilter.setTypeFilter(ItemType.Question);
		dFilter.addFieldFilter(QAModel.FIELD_ID, id.toString(), FilterComparison.EQUALS);
		IncrementalResult queryResult = mController.getObjects(dFilter, new IdComparator());
		//set up observer
		queryResult.addObserver(new QuestionResultListener(), ItemType.Question);
	}
	
	/**
	 * Constructor
	 */
	public QuestionViewActivity() {
		//mContent = new QuestionContent();		
	}
	
	public void setContent(QuestionContent content) {
		//mContent = content;
	}

	/**
	 * Inherited from IQAView
	 * 
	 * Updates the view when a model is changed.
	 * @param model The model that was changed.
	 */
	@Override
	public void update(QAModel model) {
		// we don't need to do any real work here, it will all happen in the adapter.
		refresh();
	}
	
	/**
	 * Refreshes the view.
	 * 
	 * Remakes the adapter so the view will redraw itself.
	 */
	private void refresh() {
		ListView qaList = (ListView) findViewById(R.id.qaItemView);
		mAdapter = new QABodyAdapter(this, R.id.qaItemView, mQABodies);
		qaList.invalidate();
		qaList.setAdapter(mAdapter);		
	}
		
	/**
	 * Initializes data depending on what is passed in the intent. Creates adapters and
	 * listeners for all data interactions that will happen.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionview);
		
		Intent intent = this.getIntent();
		mController = QAController.getInstance();		
		
		((Button)findViewById(R.id.createAnswer)).setOnClickListener(new AddAnswerListener());		
		mAdapter = getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView);
		
		// get question from controller somehow
		if (intent.getSerializableExtra(QUESTION_ID_EXTRA) != null) {
			UniqueId id = UniqueId.fromString((String)intent.getSerializableExtra(QUESTION_ID_EXTRA));
			queryQuestion(id);			
		}		
		
		if(intent.getSerializableExtra(QUESTION_ID_EXTRA) == null) {
			// TODO: implement Question Creation.
			
			// Generate our own data to test displaying before the other modules work.
			if(GENERATE_TEST_DATA) {
				//mContent.generateTestData();
			} else {
				AddQuestionPopup popup = new AddQuestionPopup(QuestionViewActivity.this);
				popup.show();
					
			}
			
			if (getQuestion() != null) {
				TextView title = (TextView) findViewById(R.id.titleView);
				title.setText(getQuestion().getTitle());
				
				ListView qaList = (ListView) findViewById(R.id.qaItemView);
				ListAdapter adapter = getArrayAdapter(this, R.id.qaItemView);
			
			qaList.setAdapter(adapter);
			//((BaseAdapter) adapter).notifyDataSetChanged();
			}
		}
		/*else {
			// TODO: Implement interactions with the controller to get Answers/Comments.
		}*/
		
	}
	
	

	public void favoriteQuestion(View v) {
		System.out.println("Favorite Question!");
		QAController.getInstance().addFavorite(getQuestion());
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
		mQuestion.addView(this);
		if(question != null) {
			// Make sure that the question is the first item in the list.
			List<QABody> oldList = mQABodies;
			mQABodies = new ArrayList<QABody>();
			mQABodies.add(new QABody(mQuestion));
			mQABodies.addAll(oldList);
		}
	}
	
	/**
	 * Adds all answers passed to it to the QABody list
	 * 
	 * @param answers A list of AnswerItems.
	 */
	public void addAnswers(AnswerItem... answers) {
		for(AnswerItem answer : answers) {
			if (!exists(answer)) {
				answer.addView(this);
				QABody answerBody = new QABody(answer);
				mQABodies.add(answerBody);
			}			
		}
	}
	
	public boolean exists(AuthoredTextItem item) {
		for (QABody body : mQABodies) {
			if (body.parent.equals(item)) {
				return true;
			}			
		}
		return false;
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
				if (!parentBody.comments.contains(comment)) {
					comment.addView(this);
					parentBody.comments.add(comment);
				}
			}
			else {
				// maybe some kind of error
			}
		}
	}
	
	/**
	 * Creates an adapter for the question's list content.
	 * @param context The context for the adapter.
	 * @param textViewResourceId The id of the resource to connect to.
	 * @return A new ListAdapter for this question's content.
	 */
	public ArrayAdapter getArrayAdapter(Context context, int textViewResourceId) {
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
			if(id.equals(body.parent.mUniqueId)){
				return body;
			}
		}
		return null;
	}
	
	/**
	 * This class holds a Question/Answer and its child Comments.
	 * 
	 * It's essentially a struct, so we just use public members.
	 * 
	 * @author Corey
	 *
	 */
	private class QABody {
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
		
		
		/**
		 * Returns the view after adding the list content.
		 */
		public View getView( int position, View convertView, ViewGroup parent ) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View qaItemView = inflater.inflate(R.layout.qaitem, parent, false);
			
			TextView bodyTextView = (TextView) qaItemView.findViewById(R.id.bodyText);
			TextView authorTextView = (TextView) qaItemView.findViewById(R.id.authorText);
			
			Button createCommentBtn = (Button) qaItemView.findViewById(R.id.createCommentButton);			
			createCommentBtn.setTag(mObjects.get(position).parent.getUniqueId());
			createCommentBtn.setOnClickListener(new AddCommentListener(createCommentBtn));
			
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
	
	private class QuestionResultListener implements IIncrementalObserver {

		@Override
		public void itemsArrived(List<QAModel> item, int index) {
			loadContent((QuestionItem)item.get(0));			
		}
		
	}
	
	private class AnswerResultListener implements IIncrementalObserver {

		@Override
		public void itemsArrived(List<QAModel> item, int index) {				
			ListView qaList = (ListView) findViewById(R.id.qaItemView);
			for(QAModel qaitem : item ) {
				if (qaitem.mType == ItemType.Answer) {				
					addAnswers((AnswerItem) qaitem);									
				}
			}			
			qaList.invalidate();
			qaList.setAdapter(getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView));			
		}
		
	}
	
	private class CommentResultListener implements IIncrementalObserver {

		@Override
		public void itemsArrived(List<QAModel> item, int index) {				
			ListView qaList = (ListView) findViewById(R.id.qaItemView);
			List<CommentItem> comments = new ArrayList<CommentItem>();
			for(QAModel qaitem : item ) {				
				if (qaitem.mType == ItemType.Comment) {
					comments.add((CommentItem)qaitem);					
				}
			}			
			for (CommentItem comment : comments) {
				addComments(comment);
			}
			qaList.invalidate();
			qaList.setAdapter(getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView));			
		}
		
	}
	
	private class AddAnswerListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			AddAnswerPopup popup = new AddAnswerPopup(QuestionViewActivity.this);
			popup.show();
			
		}		
	}
	
	private class AddCommentListener implements View.OnClickListener {
		private View view;
		
		public AddCommentListener(View v) {
			super();
			view = v;
		}

		@Override
		public void onClick(View v) {
			AddCommentPopup popup = new AddCommentPopup(QuestionViewActivity.this, (UniqueId) view.getTag());	
			popup.show();
		}		
	}
	
	private class UpvoteListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class FavoriteListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ViewAttachmentListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class AddAttachmentListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class PopupCancelListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//do nothing
		}
	}	
	
	private class AddAnswerPopup extends AlertDialog.Builder {
		private EditText answerBody;
		
		AddAnswerPopup (Context context) {
			super(context);
			answerBody = new EditText(context);	
			this.setTitle("Add an Answer");
			this.setView(answerBody);
			this.setPositiveButton("Submit", new AnswerPopupSubmitListener());
			this.setNegativeButton("Cancel", new PopupCancelListener());		
		}
		
		private class AnswerPopupSubmitListener implements DialogInterface.OnClickListener {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String body = answerBody.getText().toString();
				AnswerItem newAnswer = mController.createAnswer(getQuestion(), body);								
				addAnswers(newAnswer);
				loadContent(getQuestion());
			}
		}		
	}
	
	private class AddCommentPopup extends AlertDialog.Builder {
		private EditText commentBody;	
		private UniqueId parentId;
		
		AddCommentPopup(Context context, UniqueId pId) {
			super(context);
			commentBody = new EditText(context);
			parentId = pId;
			this.setTitle("Add an Comment");
			this.setView(commentBody);
			this.setPositiveButton("Submit", new CommentPopupSubmitListener());
			this.setNegativeButton("Cancel", new PopupCancelListener());
		}
		
		private class CommentPopupSubmitListener implements DialogInterface.OnClickListener {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String body = commentBody.getText().toString();
				CommentItem newComment = mController.createComment(parentId, body);								
				addComments(newComment);
				loadContent(getQuestion());			
			}
		}			
	}
	
	private class AddQuestionPopup extends AlertDialog.Builder {
		private LinearLayout layout;
		private EditText titleText;
		private EditText bodyText;		
		
		AddQuestionPopup(Context context) {
			super(context);
			layout = new LinearLayout(context);
			titleText = new EditText(context);
			bodyText = new EditText(context);
			layout.addView(titleText);
			layout.addView(bodyText);
			this.setTitle("New Question");
			this.setView(layout);
			this.setPositiveButton("add", new QuestionPopupSubmitListener());
			this.setNegativeButton("cancel", new PopupCancelListener());
		}
		
		private class QuestionPopupSubmitListener implements DialogInterface.OnClickListener {					
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String title = titleText.getText().toString();
				String body = bodyText.getText().toString();
				QuestionItem newQuestion = mController.createQuestion(title, body);
				loadContent(newQuestion);	
			}
		}	
	}
}
