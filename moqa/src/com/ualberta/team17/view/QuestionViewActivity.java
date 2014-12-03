package com.ualberta.team17.view;

import java.nio.channels.SelectableChannel;
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

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ualberta.team17.AnswerItem;
import com.ualberta.team17.AttachmentItem;
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
import com.ualberta.team17.datamanager.IncrementalResult;
import com.ualberta.team17.datamanager.comparators.DateComparator;
import com.ualberta.team17.datamanager.comparators.IdComparator;
import com.ualberta.team17.view.ListFragment.Taxonomy;

public class QuestionViewActivity extends Activity implements IQAView {
	public final static String QUESTION_ID_EXTRA = "question_id";
	private final static int RELATED_QUESTIONS_FRAGMENT_ID = 356;
	
	private QuestionItem mQuestion;
	private ArrayList<QABody> mQABodies;
	private View mQuestionView; //needed for accessing views outside of the adapter
	protected QAController mController;	
	protected QABodyAdapter mAdapter;
	private Mode mMode = Mode.DISPLAY;
	protected Fragment mRelatedQuestions;
	
	private EditText mCreateTitleView;
	private EditText mCreateBodyView;
	
	// Attachment items that are added during question creation.
	private List<AttachmentItem> mAddedAttachments;
	private AttachmentDisplayView mAddedAttachmentsView;
	
	private enum Mode {
		CREATE,
		DISPLAY
	}
	
	private enum Tab {
		COMMENT,
		ATTACHMENT,
		RQ	
	}
	
	/**
	 * Constructor
	 */
	public QuestionViewActivity() {
		mQABodies = new ArrayList<QABody>();
		mAddedAttachments = new ArrayList<AttachmentItem>();
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
			
		mAdapter = createNewAdapter();
		
		View addAttachmentView = createQuestionView.findViewById(R.id.addAttachmentView);
		mAddedAttachmentsView = (AttachmentDisplayView) addAttachmentView.findViewById(R.id.addAttachmentDisplayView);
		
		if (intent.getSerializableExtra(QUESTION_ID_EXTRA) != null) {
			setMode(Mode.DISPLAY);
			
			UniqueId id = UniqueId.fromString((String)intent.getSerializableExtra(QUESTION_ID_EXTRA));
			queryQuestion(id);
			
		} else {
			setMode(Mode.CREATE);
			
			mCreateTitleView = (EditText) createQuestionView.findViewById(R.id.createQuestionTitleView);
			mCreateBodyView = (EditText) createQuestionView.findViewById(R.id.createQuestionBodyView);
			ImageButton addAttachmentButton = (ImageButton) addAttachmentView.findViewById(R.id.addAttachmentButton);			addAttachmentButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					requestImage();
				}
				
			});
			
		}
		
	}
	
	public void setMode(Mode mode) {
		mMode = mode;
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
		question.addView(this);
		mQuestion = question;
		mQABodies.add(new QuestionBody(question));
		refresh();
	}
	
	private void resetContent() {
		mQuestion = null;
		mQABodies = new ArrayList<QABody>();
	}
	
	private void focusRelated() {			
		QuestionBody qb = (QuestionBody) this.mQABodies.get(0);
		qb.currentTab = Tab.RQ;
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
	
	private static final int IMAGE_REQUEST = 1888;
	
	private void requestImage() {
		Intent imageIntent = new Intent();
		imageIntent.setType("image/*");
		imageIntent.setAction(Intent.ACTION_GET_CONTENT);
		imageIntent.addCategory(Intent.CATEGORY_OPENABLE);
		
		startActivityForResult(imageIntent, IMAGE_REQUEST);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
			Uri imageUri = data.getData();
			String name = imageUri.getLastPathSegment();
			AttachmentItem newAttachment = mController.createDetachedAttachment(name, imageUri);
			
			mAddedAttachments.add(newAttachment);
			mAddedAttachmentsView.addAttachment(newAttachment);
		}
	}
	
	/**
	 * Creates the toolbar at the top of the app.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.moqa_menu, menu);
		mMenu = menu;
		
		menu.setGroupVisible(R.id.questionlist_group, false);
		setMenu(mMode);
		
		return true;
	}
	private Menu mMenu;
	
	private void setMenu(Mode mode) {
		if(mMenu == null) {
			throw new NullPointerException();
		}
		switch(mode) {
		case CREATE:
			mMenu.setGroupVisible(R.id.questioncreation_group, true);
			mMenu.setGroupVisible(R.id.questionview_group, false);
			break;
		case DISPLAY:
			mMenu.setGroupVisible(R.id.questioncreation_group, false);
			mMenu.setGroupVisible(R.id.questionview_group, true);
			break;
		}
	}
	
	/**
	 * Use this to respond to specific action clicks.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_new_question2) {
			Intent intent = new Intent(this, QuestionViewActivity.class);		
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_new_answer) {
			DialogFragment popup = new AddAnswerPopup();	
			popup.show(getFragmentManager(), "answer");
			return true;
		}
		if(id == R.id.action_submit_question) {
			setMode(Mode.DISPLAY);
			setQuestion(mController.createQuestion(mCreateTitleView.getText().toString(), mCreateBodyView.getText().toString()));
			for(AttachmentItem attachment : mAddedAttachments) {
				mController.connectAttachment(attachment, mQuestion.mUniqueId);
			}
			return true;
		}
		if(id == R.id.action_read_later) {
			mController.markViewLater(mQuestion);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showRelatedQuestionsTab() {
		FragmentManager fragmentManager = getFragmentManager();
		if (null != mRelatedQuestions) {
			fragmentManager.beginTransaction().show(mRelatedQuestions).commit();
		} else {
			mRelatedQuestions = new ListFragment();
			Bundle fragmentArgs = new Bundle();
			fragmentArgs.putSerializable(ListFragment.TAXONOMY_NUM, Taxonomy.RelatedQuestions);
			fragmentArgs.putSerializable(ListFragment.QUESTION_ID_EXTRA, mQuestion.getUniqueId());
			mRelatedQuestions.setArguments(fragmentArgs);
			fragmentManager.beginTransaction().add(R.id.relatedView, mRelatedQuestions).commit();						
		}
	}
	
	private void hideRelatedQuestionsTab() {
		if (null != mRelatedQuestions) {
			System.out.println("Successfully hid fragment");
			getFragmentManager().beginTransaction().hide(mRelatedQuestions).commit();								
		}
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
	
	private class QuestionBody extends QABody {
		public List<AttachmentItem> attachments;
		public Tab currentTab;

		public QuestionBody(AuthoredTextItem initParent) {
			super(initParent);
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
			QABody qaBody = mObjects.get(position);

			LayoutInflater inflater = null;
			if (null == convertView || qaBody.comments.size() > 0) {
				inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			View qaItemView = convertView;
			if (null == qaItemView) {
				qaItemView = inflater.inflate(R.layout.qaitem, parent, false);

			}
			
			Resources res = getResources();
			
			View textSideView = qaItemView.findViewById(R.id.content).findViewById(R.id.textSide);
			View iconSideView = qaItemView.findViewById(R.id.content).findViewById(R.id.iconSide);
			View tabSelectView = qaItemView.findViewById(R.id.tabSection).findViewById(R.id.tabSelect);
			View tabContentView = qaItemView.findViewById(R.id.tabSection).findViewById(R.id.tabContent);
			
			TextView titleTextView = (TextView) textSideView.findViewById(R.id.titleText);
			TextView bodyTextView = (TextView) textSideView.findViewById(R.id.bodyText);
			TextView answerCountView = (TextView) qaItemView.findViewById(R.id.answerCountView);
			TextView authorTextView = (TextView) textSideView.findViewById(R.id.authorBar).findViewById(R.id.authorText);
			TextView upvoteTextView = (TextView) iconSideView.findViewById(R.id.upvoteCount);
			
			ImageButton favoriteButton = (ImageButton) iconSideView.findViewById(R.id.favoriteButton);
			ImageButton upvoteButton = (ImageButton) iconSideView.findViewById(R.id.upvoteButton);							
			
			RelativeLayout commentTabButton = (RelativeLayout) tabSelectView.findViewById(R.id.commentTabHolder);
			RelativeLayout attachmentTabButton = (RelativeLayout) tabSelectView.findViewById(R.id.viewAttachmentHolder);			
			RelativeLayout rqTabButton = (RelativeLayout) tabSelectView.findViewById(R.id.relatedViewHolder);
			commentTabButton.setOnClickListener(new TabSelectListener(Tab.COMMENT));
			attachmentTabButton.setOnClickListener(new TabSelectListener(Tab.ATTACHMENT));	
			TabSelectListener rqListener = new TabSelectListener(Tab.RQ);
			rqTabButton.setOnClickListener(rqListener);
			
			LinearLayout commentsView = (LinearLayout) tabContentView.findViewById(R.id.commentView);		
			Button commentButton = (Button) tabContentView.findViewById(R.id.createCommentButton);			
			AttachmentDisplayView attachmentsView = (AttachmentDisplayView) tabContentView.findViewById(R.id.attachmentView);	
			
			TextView rqTitle = (TextView) tabContentView.findViewById(R.id.relatedTitle); 
			QuestionBody qb = (QuestionBody) QuestionViewActivity.this.mQABodies.get(0);


			if(qaBody.parent.mType == ItemType.Question) {
				QuestionItem question = (QuestionItem) qaBody.parent;
				QuestionBody questionBody = (QuestionBody) qaBody;
				
				authorTextView.setOnClickListener(new QuestionAuthorListener());
				
				tabSelectView.setVisibility(View.VISIBLE);
				titleTextView.setVisibility(View.VISIBLE);
				favoriteButton.setVisibility(View.VISIBLE);				
				answerCountView.setVisibility(View.VISIBLE);
				
				titleTextView.setText(question.getTitle());
				
				/*
				// does not work
				if (qb.currentTab == Tab.RQ) {
					rqTitle.setText("You may also be able to answer these questions");
					rqListener.onClick(rqTabButton);
				}
				*/
				
				if(question.getReplyCount() == 1) {
					answerCountView.setText(getString(R.string.answer_count_one));
				} else {
					answerCountView.setText(String.format(getString(R.string.answer_count), question.getReplyCount()));
				}
				
				if(question.isFavorited()) {
					Drawable favoriteHighlighted = res.getDrawable(R.drawable.ic_action_favorite_blue);
					favoriteButton.setImageDrawable(favoriteHighlighted);
				}
				
				favoriteButton.setOnClickListener(new FavoriteListener(question));

				attachmentsView.setVisibility(View.GONE);
				
				attachmentsView.clearAttachments();
				
				for (AttachmentItem attachment: questionBody.attachments) {
					attachmentsView.addAttachment(attachment);
				}
				
			} else if (qaBody.parent.mType == ItemType.Answer) {
				authorTextView.setOnClickListener(new AnswerAuthorListener());
				tabSelectView.setVisibility(View.GONE);
				titleTextView.setVisibility(View.GONE);
				favoriteButton.setVisibility(View.GONE);				
				answerCountView.setVisibility(View.GONE);
				attachmentsView.setVisibility(View.GONE);
				

				
			} else {
				// This should never happen. If it does, a bad object was added to the list.
				throw new IllegalStateException();
			}
						
			commentButton.setTag(qaBody.parent.getUniqueId());
			commentButton.setOnClickListener(new AddCommentListener(commentButton));
			
			if(qaBody.parent.haveUpvoted()) {
				Drawable highlightedUpvote = res.getDrawable(R.drawable.ic_action_collapse_blue);
				upvoteButton.setImageDrawable(highlightedUpvote);
			}
			upvoteButton.setOnClickListener(new UpvoteListener(qaBody.parent));
			upvoteTextView.setText("" + qaBody.parent.getUpvoteCount());
			
			bodyTextView.setText(qaBody.parent.getBody());
			authorTextView.setText(qaBody.parent.getAuthor());

			for (int i = 0; i < qaBody.comments.size(); i++){
				CommentItem comment = qaBody.comments.get(i);
				
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
			QuestionBody parentBody = (QuestionBody) findById(mQuestion.getUniqueId());

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
			for(AttachmentItem item : mAddedAttachments) {
				controller.connectAttachment(item, mQuestion.mUniqueId);
			}
		}
	}
	
	private class AddAnswerListener implements View.OnClickListener {		

		@Override
		public void onClick(View v) {
			DialogFragment popup = new AddAnswerPopup();	
			popup.show(getFragmentManager(), "answer");
			
		}		
	}
	
	private class TabSelectListener implements View.OnClickListener {
		private Tab tab;
		
		public TabSelectListener(Tab val) {
			super();
			tab = val;
		}
		
		@Override
		public void onClick(View v) {
			LinearLayout tabSelect = (LinearLayout) v.getParent();
			RelativeLayout tabSection = (RelativeLayout) tabSelect.getParent();
			RelativeLayout tabContent = (RelativeLayout) tabSection.findViewById(R.id.tabContent);
			
			RelativeLayout commentTab = (RelativeLayout) tabSelect.findViewById(R.id.commentTabHolder);
			RelativeLayout attachmentTab = (RelativeLayout) tabSelect.findViewById(R.id.viewAttachmentHolder);
			RelativeLayout rqTab = (RelativeLayout) tabSelect.findViewById(R.id.relatedViewHolder);
			
			//Set the same as unselectedtab style
			commentTab.setBackgroundColor(Color.parseColor("#fcf2d3"));
			attachmentTab.setBackgroundColor(Color.parseColor("#fcf2d3"));
			rqTab.setBackgroundColor(Color.parseColor("#fcf2d3"));
			
			v.setBackgroundColor(Color.parseColor("#f7ebca"));

			View commentView = tabContent.findViewById(R.id.commentView);
			View commentCButton = tabContent.findViewById(R.id.createCommentButton);
			View attachmentView = tabContent.findViewById(R.id.attachmentView);
			View relatedView = tabContent.findViewById(R.id.relatedView);
			View relatedTitle = tabContent.findViewById(R.id.relatedTitle);			
			
			switch (tab) {
			
				case COMMENT: 
					commentView.setVisibility(View.VISIBLE);
					commentCButton.setVisibility(View.VISIBLE);
					attachmentView.setVisibility(View.GONE);	
					hideRelatedQuestionsTab();
					relatedView.setVisibility(View.GONE);
					relatedTitle.setVisibility(View.GONE);
										
					break;
					
				case ATTACHMENT:
					commentView.setVisibility(View.GONE);
					commentCButton.setVisibility(View.GONE);
					attachmentView.setVisibility(View.VISIBLE);		
					hideRelatedQuestionsTab();
					relatedView.setVisibility(View.GONE);
					relatedTitle.setVisibility(View.GONE);					
					
					break;
				
				case RQ:
					commentView.setVisibility(View.GONE);
					commentCButton.setVisibility(View.GONE);
					attachmentView.setVisibility(View.GONE);				
					relatedView.setVisibility(View.VISIBLE);
					relatedTitle.setVisibility(View.VISIBLE);
					showRelatedQuestionsTab();
					
					break;		
					
				default: 
					commentView.setVisibility(View.GONE);
					commentCButton.setVisibility(View.GONE);
					attachmentView.setVisibility(View.GONE);	
					hideRelatedQuestionsTab();
					relatedView.setVisibility(View.GONE);
					relatedTitle.setVisibility(View.GONE);					
					
					break;
			}
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
	
	private class QuestionAuthorListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			/*
			TextView tView = (TextView) v;
			//launch intent to QLA
			Intent intent = new Intent(QuestionViewActivity.this, QuestionListActivity.class);		
			intent.putExtra(QuestionListActivity.AUTHOR_SORT, tView.getText());
			startActivity(intent);		
			*/	
		}
		
	}
	
	private class AnswerAuthorListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			/*
			TextView tView = (TextView) v;
			//launch intent to QLA
			Intent intent = new Intent(QuestionViewActivity.this, QuestionListActivity.class);		
			intent.putExtra(QuestionListActivity.AUTHORSORT, tView.getText());
			startActivity(intent);		
			*/
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
				focusRelated();
				
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
