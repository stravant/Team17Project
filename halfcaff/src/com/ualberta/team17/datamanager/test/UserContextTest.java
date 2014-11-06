package com.ualberta.team17.datamanager.test;

import java.util.List;

import com.google.gson.JsonElement;
import com.ualberta.team17.UniqueId;
import com.ualberta.team17.datamanager.UserContext;

import junit.framework.TestCase;

public class UserContextTest extends TestCase {
	public void setUp() {
	}
	
	/**
	 * Generate a unique id from an integer
	 * @param i
	 */
	public UniqueId uid(int i) {
		return new UniqueId(Integer.toString(i));
	}
	
	/**
	 * Test that we can add some items to the list of favorites, and then
	 * try saving and reading back the favorites to get the same thing.
	 */
	public void test_CycleFavorites() {
		// Make a context
		UserContext ctx1 = new UserContext("test_user");
		
		// Write out some favorites
		ctx1.addFavorite(uid(1));
		ctx1.addFavorite(uid(2));
		
		// Save out
		JsonElement elem = ctx1.saveToJson();
		
		// Read in to a new context
		UserContext ctx2 = new UserContext("test_user");
		ctx2.loadFromJson(elem);
		
		// Check that the favorites are the same
		List<UniqueId> faves = ctx2.getFavorites();
		
		assertEquals(2, faves.size());
		assertEquals(uid(1), faves.get(0));
		assertEquals(uid(2), faves.get(1));
	}
	
	/**
	 * Test that the recently viewed items are saved and loaded correctly,
	 * by adding some recently favorited items, saving them out, and reading
	 * them back.
	 */
	public void test_CycleRecent() {
		// Make a context
		UserContext ctx1 = new UserContext("test_user");
		
		// Write out some items
		ctx1.addRecentItem(uid(1)); // --> [1]
		ctx1.addRecentItem(uid(2)); // --> [2, 1]
		ctx1.addRecentItem(uid(1)); // --> [1, 2]
		ctx1.addRecentItem(uid(3)); // --> [3, 1, 2]
		
		// Save out
		JsonElement elem = ctx1.saveToJson();
		
		// Read back in
		UserContext ctx2 = new UserContext("test_user");
		ctx2.loadFromJson(elem);
		
		// Test the items
		List<UniqueId> recent = ctx2.getRecentItems();
		
		assertEquals(3, recent.size());
		assertEquals(uid(3), recent.get(0));
		assertEquals(uid(1), recent.get(1));
		assertEquals(uid(2), recent.get(2));
	}
}
