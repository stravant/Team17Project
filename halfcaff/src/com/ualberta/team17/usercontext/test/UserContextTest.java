package com.ualberta.team17.usercontext.test;

import java.util.List;

import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.UserContext;

import junit.framework.TestCase;

public class UserContextTest extends TestCase {
	private static final String UserContextId = "TestUser";
	private UserContext context;
	
	public void setUp() {
		context = new UserContext(UserContextId);
	}
	
	/**
	 * Tests that adding favorites to a user context works properly.
	 */
	public void test_UC1_AddFavorites() {
		UniqueId id = new UniqueId();
		context.addFavorite( id );
		
		List<UniqueId> favorites = context.getFavorites();
		assertTrue("Favorites has id", favorites.contains(id));
	}
	
	/**
	 * Tests that the user context does not allow duplicate favorite items.
	 */
	public void test_UC2_AddFavoritesNoDuplicates() {
		UniqueId id = new UniqueId();
		context.addFavorite( id );
		context.addFavorite( id );
		
		List<UniqueId> favorites = context.getFavorites();
		assertEquals( "Favorites has length 1", favorites.size(), 1 );
	}
	
	/**
	 * Tests that a reply in a user context is properly added to the list of replies.
	 */
	public void test_UC3_AddReply() {
		UniqueId id = new UniqueId();
		context.addReply( id );
		
		List<UniqueId> replies = context.getReplies();
		assertTrue( "Replies has id", replies.contains(id));
	}
	
	/**
	 * Tests that duplicate replies cannot be added to the user context.
	 */
	public void test_UC4_AddReplyNoDuplicates() {
		UniqueId id = new UniqueId();
		context.addReply( id );
		context.addReply( id );
		
		List<UniqueId> replies = context.getReplies();
		assertEquals( "Replies has length 1", replies.size(), 1 );
	}
}
