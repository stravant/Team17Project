package com.ualberta.team17.view;

import java.util.ArrayList;
import java.util.List;

import com.ualberta.team17.AnswerItem;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class QuestionViewActivity extends Activity {
	public final static String QUESTION_ID_EXTRA = "question_id";
	
	// Test - can be deleted later
	private final static boolean GENERATE_TEST_DATA = false;
	
	private QuestionContent mContent;
	private QAController mController; 	
	private ArrayAdapter mAdapter;
	
	/**
	 * Listener that opens a pop-up to creating an answer
	 * @author Joel
	 *
	 */
	private class CreateAnswerListener implements View.OnClickListener {
		private Context mContext;
		
		public CreateAnswerListener(Context context){
			mContext = context;
		}
		public void onClick(View v){
			final EditText answerBody = new EditText(mContext);
			
			new AlertDialog.Builder(mContext)
					.setTitle("Add an Answer")
					.setView(answerBody)
					.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String body = answerBody.getText().toString();
								AnswerItem newAnswer = mController.createAnswer(mContent.getQuestion(), body);								
								mContent.addAnswers(newAnswer);
								loadContent(mContent.getQuestion());
							}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton)
						{
						// Do Nothing!
						}
					})
					.show();

		}
	}
	
	/**
	 * Method that sets the question for mContent
	 * @author Joel
	 * @param question
	 */
	private void loadContent(QuestionItem question) {
		// make sure we aren't loading a mix of two questions at the same time		
		mContent = new QuestionContent();
		mContent.setQuestion(question);
		TextView title = (TextView)findViewById(R.id.titleView);
		ListView listview = (ListView)findViewById(R.id.qaItemView);
		listview.setAdapter(mContent.getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView));
		title.setText(mContent.getQuestion().getTitle());
		IncrementalResult iRAC = mController.getChildren(question, new DateComparator());
		iRAC.addObserver(new IIncrementalObserver() {
			@Override
			public void itemsArrived(List<QAModel> item, int index) {				
				ListView qaList = (ListView) findViewById(R.id.qaItemView);
				List<CommentItem> comments = new ArrayList<CommentItem>();
				for(QAModel qaitem : item ) {
					switch(qaitem.mType) {
					case Answer:
						mContent.addAnswers((AnswerItem) qaitem);
						break;
					case Comment:
						comments.add((CommentItem)qaitem);
						break;
					}
				}
				//this functionality should be moved to content
				for (CommentItem comment : comments) {
					mContent.addComments(comment);
				}
				qaList.invalidate();
				qaList.setAdapter(mContent.getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView));
				//mAdapter.notifyDataSetChanged();
			}
		});
		//mAdapter.notifyDataSetChanged();
		
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
		queryResult.addObserver(new IIncrementalObserver() {
			@Override
			public void itemsArrived(List<QAModel> item, int index) {						
				loadContent((QuestionItem)item.get(0));
			}			
		});
	}
	
	/**
	 * Constructor
	 */
	public QuestionViewActivity() {
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
		
		Intent intent = this.getIntent();
		mController = QAController.getInstance();		
		
		((Button)findViewById(R.id.createAnswer)).setOnClickListener(new CreateAnswerListener(this));		
		mAdapter = mContent.getArrayAdapter(QuestionViewActivity.this, R.id.qaItemView);
		
		// get question from controller somehow
		if (intent.getSerializableExtra(QUESTION_ID_EXTRA) != null) {
			UniqueId id = UniqueId.fromString((String)intent.getSerializableExtra(QUESTION_ID_EXTRA));
			queryQuestion(id);			
		}		
		
		if(intent.getSerializableExtra(QUESTION_ID_EXTRA) == null) {
			// TODO: implement Question Creation.
			
			// Generate our own data to test displaying before the other modules work.
			if(GENERATE_TEST_DATA) {
				mContent.generateTestData();
			} else {
				final LinearLayout layout = new LinearLayout(this);
				final EditText titleText = new EditText(this);
				final EditText bodyText = new EditText(this);
				
				layout.addView(titleText);
				layout.addView(bodyText);
				
				new AlertDialog.Builder(this)
					.setTitle("New Question")
					.setView(layout)
					.setPositiveButton("add", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String title = titleText.getText().toString();
							String body = bodyText.getText().toString();
							QuestionItem newQuestion = mController.createQuestion(title, body);
							loadContent(newQuestion);							
						}
						
					})
					.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// probably go back to the previous view, otherwise we get a blank view 
						}
						
					})
					.show();
					
			}
			
			if (mContent.getQuestion() != null) {
				TextView title = (TextView) findViewById(R.id.titleView);
				title.setText(mContent.getQuestion().getTitle());
				
				ListView qaList = (ListView) findViewById(R.id.qaItemView);
				ListAdapter adapter = mContent.getArrayAdapter(this, R.id.qaItemView);
			
			qaList.setAdapter(adapter);
			//((BaseAdapter) adapter).notifyDataSetChanged();
			}
		}
		/*else {
			// TODO: Implement interactions with the controller to get Answers/Comments.
		}*/
		
	}
}
