package com.ualberta.team17;

import java.util.Date;

/*
 * An AuthoredItem is a item that a user posted to the service at some date.
 * Optionally, the item was posted in reply to another item on the service.
 */
public abstract class AuthoredItem implements IUnique {
	String mAuthor;
	Date mDate;
	UniqueId mInReplyTo;
	StoragePolicy mStoragePolicy;
}
