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
	
	public void test_UC1_AddFavorites() {
		UniqueId id = new UniqueId();
		context.addFavorite( id );
		
		List<UniqueId> favorites = context.getFavorites();
		assertTrue("Favorites has id", favorites.contains(id));
	}
	
	public void test_UC2_AddFavoritesNoDuplicates() {
		UniqueId id = new UniqueId();
		context.addFavorite( id );
		context.addFavorite( id );
		
		List<UniqueId> favorites = context.getFavorites();
		assertEquals( "Favorites has length 1", favorites.size(), 1 );
	}
	
	public void test_UC3_AddReply() {
		UniqueId id = new UniqueId();
		context.addReply( id );
		
		List<UniqueId> replies = context.getReplies();
		assertTrue( "Replies has id", replies.contains(id));
	}
	
	public void test_UC4_AddReplyNoDuplicates() {
		UniqueId id = new UniqueId();
		context.addReply( id );
		context.addReply( id );
		
		List<UniqueId> replies = context.getReplies();
		assertEquals( "Replies has length 1", replies.size(), 1 );
	}
}
