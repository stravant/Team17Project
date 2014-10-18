package com.ualberta.team17.usercontext.test;

import java.util.ArrayList;

import com.ualberta.team17.UniqueId;
import com.ualberta.team17.usercontext.UserContext;

import junit.framework.TestCase;

public class UserContextTest extends TestCase {
	private UserContext context;
	
	public void SetUp() {
		context = new UserContext();
	}
	
	public void Test_UC1_AddFavorites() {
		UniqueId id = new UniqueId();
		context.addFavorite( id );
		
		ArrayList<UniqueId> favorites = context.getFavorites();
		assertTrue("Favorites has id", favorites.contains(id));
	}
	
	public void Test_UC2_AddFavoritesNoDuplicates() {
		UniqueId id = new UniqueId();
		context.addFavorite( id );
		context.addFavorite( id );
		
		ArrayList<UniqueId> favorites = context.getFavorites();
		assertEquals( "Favorites has length 1", favorites.size(), 1 );
	}
	
	public void Test_UC3_AddReply() {
		UniqueId id = new UniqueId();
		context.addReply( id );
		
		ArrayList<UniqueId> replies = context.getReplies();
		assertTrue( "Replies has id", replies.contains(id));
	}
	
	public void Test_UC4_AddReplyNoDuplicates() {
		UniqueId id = new UniqueId();
		context.addReply( id );
		context.addReply( id );
		
		ArrayList<UniqueId> replies = context.getReplies();
		assertEquals( "Replies has length 1", replies.size(), 1 );
	}
}
