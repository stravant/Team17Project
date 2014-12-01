package com.ualberta.team17;

/*
 * Defines how an object should be stored by the data managers.
 */
public enum StoragePolicy {
	Cached,    // Store in local file system if possible
	Transient, // Don't store in local file system
}
