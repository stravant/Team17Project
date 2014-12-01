package com.ualberta.team17;

/**
 * Enumerator that is conveniently used to communicate types in the data model,
 * and when talking to the data managers.
 * 
 * @author michaelblouin
 */
public enum ItemType {
	Upvote,
	Attachment,
	Comment,
	Question,
	Answer;

	public static ItemType fromString(String text) {
		if (text.equalsIgnoreCase("Question")) {
			return Question;
		} else if (text.equalsIgnoreCase("Answer")) {
			return Answer;
		} else if (text.equalsIgnoreCase("Comment")) {
			return Comment;
		} else if (text.equalsIgnoreCase("Attachment")) {
			return Attachment;
		} else if (text.equalsIgnoreCase("Upvote")) {
			return Upvote;
		}

		return null;
	}
}
