package com.ualberta.team17;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import com.ualberta.team17.datamanager.UserContext;

public class UniqueId {
	private final static String DIGEST_ALGORITHM = "MD5";
	private final static Integer UNIQUE_ID_LENGTH = 32;
	private String mId;

	/**
	 * Preferred constructor -- generates a unique id using the provided user context and other information.
	 * @param context The user context to use in constructing the digest
	 */
	public UniqueId(UserContext context) {
		String digestString = context.toString() + (new Date()).toString();

		try {
			// Initially try building the string from the user and date
			MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
			mId = messageDigest.digest(digestString.getBytes()).toString();
		} catch (NoSuchAlgorithmException e) {
			// Fall back to a random string
			System.err.print(String.format("%s Algorithm not available for creating new UniqueId", DIGEST_ALGORITHM));
			mId = GenerateRandomIdString(digestString);
		}
	}
	
	/**
	 * Constructor that takes constructs a unique ID by digesting a string
	 * @param id
	 */
	public UniqueId(String id) {
		try {
			// Initially try building the string from the passed id
			MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
			mId = messageDigest.digest(id.getBytes()).toString();
		} catch (NoSuchAlgorithmException e) {
			// Fall back to a random string
			System.err.print(String.format("%s Algorithm not available for creating new UniqueId", DIGEST_ALGORITHM));
			mId = GenerateRandomIdString(id);
		}
	}

	public UniqueId() {
		mId = GenerateRandomIdString(null);
	}

	@Override
	public boolean equals(Object other) {
		if (null == other || !(other instanceof UniqueId)) {
			return false;
		}

		return mId.equals(((UniqueId)other).mId);
	}

	@Override
	public int hashCode() {
		return mId.hashCode();
	}

	/**
	 * Generates a random string to be used as a UniqueId
	 * @param seedString A string to be turned into a seed for the random number generator
	 * @return The random string
	 */
	protected static String GenerateRandomIdString(String seedString) {
		String idString = "_";
		char[] characters = "abcdef0123456789".toCharArray();
		Random randomGenerator = new Random();

		if (null != seedString) {
			long seed = 0;
			for (char c: seedString.toCharArray()) {
				seed += (int)c;
			}

			randomGenerator.setSeed(seed);
		}

		while (idString.length() < UNIQUE_ID_LENGTH) {
			idString += characters[randomGenerator.nextInt(characters.length)];
		}

		return idString;
	}

	@Override
	public String toString() {
		return mId;
	}
}
