package com.ualberta.team17;

public enum ItemType {
	Upvote,
	Attachment,
	Comment,
	Question,
	Answer;

	public static ItemType fromString(String text) {
		for (ItemType type : ItemType.values()) {
			if (text.equalsIgnoreCase(type.toString())) {
				return type;
			}
		}

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
