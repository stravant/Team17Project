package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
import com.ualberta.team17.AuthoredTextItem;
import com.ualberta.team17.CommentItem;
import com.ualberta.team17.ItemType;
import com.ualberta.team17.QAModel;
import com.ualberta.team17.QuestionItem;
import com.ualberta.team17.R;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.R.id;
import com.ualberta.team17.controller.QAController;
import com.ualberta.team17.datamanager.DataFilter;
import com.ualberta.team17.datamanager.DataFilter.FilterComparison;
import com.ualberta.team17.datamanager.IIncrementalObserver;
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.comparators.DateComparator;
import com.ualberta.team17.datamanager.comparators.IdComparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivity extends Activity implements IQAView {
	public final static String QUESTION_ID_EXTRA = "question_id";
	
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQABodies;
	protected QAController mController;	
	protected QABodyAdapter mAdapter;
	
	private enum Mode {
		CREATE,
		DISPLAY
	}
	
	/**
	 * Constructor
	 */
	public QuestionViewActivity() {
		mQABodies = new ArrayList<QABody>();
	}
		
	/**
	 * Method that sets the question for mContent
	 * @author Joel
	 * @param question
	 */
	private void loadContent(QuestionItem question) {
		setQuestion(question);
		ListView listview = (ListView) findViewById(R.id.qaItemView);
		listview.setAdapter(createNewAdapter());
		IncrementalResult questionChildrenResult = mController.getChildren(question, new DateComparator());
		questionChildrenResult.addObserver(new QuestionChildrenResultListener());
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

		View displayQuestionView = findViewById(R.id.displayQuestionView);
		View createQuestionView = findViewById(R.id.createQuestionView);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		Intent intent = this.getIntent();
		mController = QAController.getInstance();		
		
		((Button) displayQuestionView.findViewById(R.id.createAnswer)).setOnClickListener(new AddAnswerListener());		
		mAdapter = createNewAdapter();
		
		if (intent.getSerializableExtra(QUESTION_ID_EXTRA) != null) {
			setMode(Mode.DISPLAY);
			
			UniqueId id = UniqueId.fromString((String)intent.getSerializableExtra(QUESTION_ID_EXTRA));
			queryQuestion(id);			
		} else {
			setMode(Mode.CREATE);
			
			Button submitButton = (Button) createQuestionView.findViewById(R.id.createQuestionSubmitButton);
			EditText titleText = (EditText) createQuestionView.findViewById(R.id.createQuestionTitleView);
			EditText bodyText = (EditText) createQuestionView.findViewById(R.id.createQuestionBodyView);
			
			submitButton.setOnClickListener(new SubmitQuestionListener(titleText, bodyText));
		}
		
	}
	
	public void setMode(Mode mode) {
		View displayQuestionView = findViewById(R.id.displayQuestionView);
		View createQuestionView = findViewById(R.id.createQuestionView);
		
		if(mode == Mode.CREATE) {
			displayQuestionView.setVisibility(View.GONE);
			createQuestionView.setVisibility(View.VISIBLE);
		} else if(mode == Mode.DISPLAY) {
			displayQuestionView.setVisibility(View.VISIBLE);
			createQuestionView.setVisibility(View.GONE);
		}
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
		resetContent();
		mQuestion = question;
		mQABodies.add(new QABody(question));
		refresh();
	}
	
	private void resetContent() {
		mQuestion = null;
		mQABodies = new ArrayList<QABody>();
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
	 * Creates a new adapter for the list content.
	 * @return A new QABody adapter.
	 */
	public QABodyAdapter createNewAdapter() {
		return new QABodyAdapter(this, R.id.qaItemView, mQABodies);
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
		public List<AttachmentItem> attachments;
		
		public QABody(AuthoredTextItem initParent) {
			parent = initParent;
			comments = new ArrayList<CommentItem>();
			attachments = new ArrayList<AttachmentItem>();
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
			QABody qaItem = mObjects.get(position);

			LayoutInflater inflater = null;
			if (null == convertView || qaItem.comments.size() > 0) {
				inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			View qaItemView = convertView;
			if (null == qaItemView) {
				qaItemView = inflater.inflate(R.layout.qaitem, parent, false);
				}		

			
			View textSideView = qaItemView.findViewById(R.id.content).findViewById(R.id.textSide);
			View iconSideView = qaItemView.findViewById(R.id.content).findViewById(R.id.iconSide);
			View tabSelectView = qaItemView.findViewById(R.id.tabSection).findViewById(R.id.tabSelect);
			View tabContentView = qaItemView.findViewById(R.id.tabSection).findViewById(R.id.tabContent);
			
			TextView titleTextView = (TextView) textSideView.findViewById(R.id.titleText);
			TextView bodyTextView = (TextView) textSideView.findViewById(R.id.bodyText);
			TextView answerCountView = (TextView) qaItemView.findViewById(R.id.answerCountView);
			TextView authorTextView = (TextView) textSideView.findViewById(R.id.authorBar).findViewById(R.id.authorText);
			
			ImageButton favoriteButton = (ImageButton) iconSideView.findViewById(R.id.favoriteButton);
			ImageButton attachmentButton = (ImageButton) tabSelectView.findViewById(R.id.viewAttachmentHolder).findViewById(R.id.viewAttachmentButton);			
			//replace with button or imagebutton
			TextView commentButton = (TextView) tabContentView.findViewById(R.id.createCommentButton);
			ImageButton upvoteButton = (ImageButton) iconSideView.findViewById(R.id.upvoteButton);			

			ImageButton commentTabButton = (ImageButton) tabSelectView.findViewById(R.id.commentTabHolder).findViewById(R.id.commentTabButton);
			
			LinearLayout commentsView = (LinearLayout) tabContentView.findViewById(R.id.commentView);			
			

			AttachmentView attachmentsView = (AttachmentView) tabContentView.findViewById(R.id.attachmentView);

			

			

			if(qaItem.parent.mType == ItemType.Question) {
				QuestionItem question = (QuestionItem) qaItem.parent;
				
				tabSelectView.setVisibility(View.VISIBLE);
				titleTextView.setVisibility(View.VISIBLE);
				favoriteButton.setVisibility(View.VISIBLE);
				attachmentButton.setVisibility(View.VISIBLE);
				answerCountView.setVisibility(View.VISIBLE);
				
				titleTextView.setText(question.getTitle());
				if(question.getReplyCount() == 1) {
					answerCountView.setText(getString(R.string.answer_count_one));
				} else {
					answerCountView.setText(String.format(getString(R.string.answer_count), question.getReplyCount()));
				}
				favoriteButton.setOnClickListener(new FavoriteListener(question));

				attachmentsView.setVisibility(qaItem.attachments.size() > 0 ? View.VISIBLE : View.GONE);

			} else if (qaItem.parent.mType == ItemType.Answer) {
				tabSelectView.setVisibility(View.GONE);
				titleTextView.setVisibility(View.GONE);
				favoriteButton.setVisibility(View.GONE);
				attachmentButton.setVisibility(View.GONE);
				answerCountView.setVisibility(View.GONE);
				attachmentsView.setVisibility(View.GONE);
				
			} else {
				// This should never happen. If it does, a bad object was added to the list.
				throw new IllegalStateException();
			}
						
			commentButton.setTag(qaItem.parent.getUniqueId());
			commentButton.setOnClickListener(new AddCommentListener(commentButton));
			
			upvoteButton.setOnClickListener(new UpvoteListener(qaItem.parent));
			
			bodyTextView.setText(qaItem.parent.getBody());
			authorTextView.setText(qaItem.parent.getAuthor());
			
			for (AttachmentItem attachment: qaItem.attachments) {
				attachmentsView.addAttachment(attachment);
			}

			for (int i = 0; i < qaItem.comments.size(); i++){
				CommentItem comment = qaItem.comments.get(i);
				
				View commentView = inflater.inflate(R.layout.comment, parent, false);
				TextView commentBody = (TextView) commentView.findViewById(R.id.commentText);
				commentBody.setText(comment.getBody());
				
				TextView commentAuthor = (TextView) commentView.findViewById(R.id.commentAuthor);
				commentAuthor.setText("-" + comment.getAuthor());
				
				commentsView.addView(commentView);
			}					
			return qaItemView;
		}
	}
	
	/**
	 * Listener for an incremental result's question.
	 * @author Corey
	 *
	 */
	private class QuestionResultListener implements IIncrementalObserver {

		@Override
		public void itemsArrived(List<QAModel> item, int index) {
			loadContent((QuestionItem)item.get(0));			
		}
		
	}
	
	/**
	 * Listener for the main question's answers, comments, and attachments.
	 * @author Corey
	 *
	 */
	private class QuestionChildrenResultListener implements IIncrementalObserver {

		@Override
		public void itemsArrived(List<QAModel> item, int index) {				
			ListView qaList = (ListView) findViewById(R.id.qaItemView);
			QABody parentBody = findById(mQuestion.getUniqueId());

			for(QAModel qaitem : item ) {
				if (qaitem.mType == ItemType.Answer) {
					AnswerItem answer = (AnswerItem) qaitem;
					addAnswers(answer);
					IncrementalResult answerChildrenResult = mController.getChildren(answer, new DateComparator());
					answerChildrenResult.addObserver(new CommentResultListener(), ItemType.Comment);
				} else if (qaitem.mType == ItemType.Comment) {
					addComments((CommentItem)qaitem);
				} else if (qaitem.mType == ItemType.Attachment) {
					qaitem.addView(QuestionViewActivity.this);
					parentBody.attachments.add((AttachmentItem)qaitem);
				}
			}

			qaList.invalidate();
			qaList.setAdapter(createNewAdapter());			
		}
		
	}
	
	/**
	 * Listener for an incremental result's comments.
	 * @author Corey
	 *
	 */
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
			qaList.setAdapter(createNewAdapter());			
		}
		
	}
	
	private class SubmitQuestionListener implements View.OnClickListener {
		EditText mTitleView;
		EditText mBodyView;

		public SubmitQuestionListener(EditText titleView, EditText bodyView) {
			mTitleView = titleView;
			mBodyView = bodyView;
		}
		@Override
		public void onClick(View v) {
			QAController controller = QAController.getInstance();
			setMode(Mode.DISPLAY);
			setQuestion(controller.createQuestion(mTitleView.getText().toString(), mBodyView.getText().toString()));
		}
		
	}
	
	private class AddAnswerListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			DialogFragment popup = new AddAnswerPopup();	
			popup.show(getFragmentManager(), "answer");
			
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
			DialogFragment popup = new AddCommentPopup((UniqueId) view.getTag());	
			popup.show(getFragmentManager(), "comment");
		}		
	}
	
	private class UpvoteListener implements View.OnClickListener {
		private QAModel mItem;
		
		public UpvoteListener(QAModel item) {
			mItem = item;
		}
		
		@Override
		public void onClick(View v) {
			QAController controller = QAController.getInstance(); 
			controller.upvote(mItem);
		}
		
	}
	
	private class FavoriteListener implements View.OnClickListener {
		private QuestionItem mItem;
		
		public FavoriteListener(QuestionItem item) {
			mItem = item;
		}
		
		@Override
		public void onClick(View v) {
			QAController controller = QAController.getInstance();
			controller.addFavorite(mItem);
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
		}
		
	}
	
	private class AddAnswerPopup extends DialogFragment {
		private EditText answerBody;			
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());			
			LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
			View answerView = inflater.inflate(R.layout.create_answer, null);
			answerBody = (EditText) answerView.findViewById(R.id.answerText);	
			Button submitButton = (Button) answerView.findViewById(R.id.submitAnswer);
			Button cancelButton = (Button) answerView.findViewById(R.id.cancelAnswer);
			submitButton.setOnClickListener(new AnswerPopupSubmitListener());
			cancelButton.setOnClickListener(new PopupCancelListener());					
			builder.setView(answerView);
			return builder.create();
		}		
			
		private class AnswerPopupSubmitListener implements View.OnClickListener {					
			@Override
			public void onClick(View v) {							
				String body = answerBody.getText().toString();
				AddAnswerPopup.this.dismiss();
				AnswerItem newAnswer = mController.createAnswer(getQuestion(), body);								
				addAnswers(newAnswer);
				loadContent(getQuestion());		
			}
		}		
		
		private class PopupCancelListener implements View.OnClickListener {
			@Override
			public void onClick(View v) {
				AddAnswerPopup.this.dismiss();
			}
		}
		
			
	}
	
	private class AddCommentPopup extends DialogFragment {
		private EditText commentBody;	
		private UniqueId parentId;
		
		public AddCommentPopup(UniqueId pId) {
			super();
			parentId = pId;
		}
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());			
			LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
			View commentView = inflater.inflate(R.layout.create_comment, null);
			commentBody = (EditText) commentView.findViewById(R.id.commentText);	
			Button submitButton = (Button) commentView.findViewById(R.id.submitComment);
			Button cancelButton = (Button) commentView.findViewById(R.id.cancelComment);
			submitButton.setOnClickListener(new CommentPopupSubmitListener());
			cancelButton.setOnClickListener(new PopupCancelListener());					
			builder.setView(commentView);
			return builder.create();
		}		
			
		private class CommentPopupSubmitListener implements View.OnClickListener {					
			@Override
			public void onClick(View v) {
				String body = commentBody.getText().toString();
				AddCommentPopup.this.dismiss();
				CommentItem newComment = mController.createComment(parentId, body);								
				addComments(newComment);
				loadContent(getQuestion());			
			}
		}		
		
		private class PopupCancelListener implements View.OnClickListener {
			@Override
			public void onClick(View v) {
				AddCommentPopup.this.dismiss();
			}
		}		
			
	}	
	
}
