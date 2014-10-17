package com.ualberta.team17;

/*
 * How should a given 
 */
public enum StoragePolicy {
	Cached,    // Store in local file system if possible
	Transient, // Don't store in local file system
	Inherit    // Inherit same storage policy as the AuthoredItem that this is in reply to
}
