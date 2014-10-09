package com.ualberta.team17;

import java.util.Observable;

/*
 * DataModel that is returned by the DataManager when it is asked
 * for a full thread. QuestionThread objects provide a way to aggregate
 * Questions, Answers, and Comments all into the same data structure.
 * 
 * Note: This is an incremental data structure, it will be updated as
 * the data for the QuestionThread being queried arrives from the disk
 * and / or database.
 */
public class QuestionThread implements Observable {
	QuestionThreadItem<QuestionItem> mQuestion;       // The question
	List<QuestionThreadItem<AnswerItem>> mAnswerList; // The answers
	QASearchOrder mAnswerOrder; // What order to store the answers in in the AnswerList
}
