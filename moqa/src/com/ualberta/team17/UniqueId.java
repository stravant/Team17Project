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
	 * Utility method, digest a string into a hash, and encode
	 * the hash as a Hex string.
	 */
	public String digest(String input) {
		try {
			// Initially try building the string from the user and date
			MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
			byte[] bytes = messageDigest.digest(input.getBytes());
			String hash = "";
			for (byte b: bytes) {
				hash += Integer.toHexString(b & 0xFF);
			}
			return hash;
		} catch (NoSuchAlgorithmException e) {
			// Fall back to a random string
			System.err.print(String.format("%s Algorithm not available for creating new UniqueId", DIGEST_ALGORITHM));
			return GenerateRandomIdString(input);
		}
	}
	
	public static UniqueId fromSerial(String data) {
		UniqueId id = new UniqueId();
		id.mId = data;
		return id;
	}
	
	/**
	 * Preferred constructor -- generates a unique id using the provided user context and other information.
	 * @param context The user context to use in constructing the digest
	 */
	public UniqueId(UserContext context) {
		String digestString = context.toString() + (new Date()).toString();
		mId = digest(digestString);
	}
	
	/**
	 * Constructor that takes constructs a unique ID by digesting a string
	 * @param id
	 */
	public UniqueId(String id) {
		mId = digest(id);
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
