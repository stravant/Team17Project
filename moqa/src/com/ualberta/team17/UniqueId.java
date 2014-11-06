package com.ualberta.team17;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import com.ualberta.team17.datamanager.UserContext;

/**
 * UniqueId class, wrapper around using a hashing algorithm to 
 * create unique identifiers for pieces of content. 
 */
public class UniqueId {
	/**
	 * The hashing digest algorithm to use to transform string content
	 * into a unique ID.
	 */
	private final static String DIGEST_ALGORITHM = "MD5";
	
	/**
	 * The length of hash to use for UniqueIds.
	 * Represented as a string, UniqueIds will be twice as long 
	 * as this value.
	 */
	private final static Integer UNIQUE_ID_LENGTH = 32;
	
	/**
	 * Internal storage for our hash.
	 */
	private String mId;

	/**
	 * Utility method, digest a string into a hash, and encode
	 * the hash as a Hex string.
	 * @param input The string to digest
	 */
	private String digest(String input) {
		try {
			// Digest the message into a set of hash bytes
			MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
			byte[] bytes = messageDigest.digest(input.getBytes());
			
			// Convert the bytes into a hex-string
			String hash = "";
			for (byte b: bytes) {
				String hex = Integer.toHexString(b & 0xFF);
				if (hex.length() == 1)
					hex = "0" + hex;
				hash += hex;
			}
			
			// Return the constructed hash string
			return hash;
		} catch (NoSuchAlgorithmException e) {
			// Fall back: Generate random string
			System.err.print(String.format("%s Algorithm not available for creating new UniqueId", DIGEST_ALGORITHM));
			return GenerateRandomIdString(input);
		}
	}
	
	/**
	 * Get a UniqueId object from it's string representation.
	 * @param repr The string representation of the UniqueId.
	 * @return A UniqueId such that returnValue.toString() is equal to repr.
	 */
	public static UniqueId fromString(String repr) {
		UniqueId id = new UniqueId();
		id.mId = repr;
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
	 * Constructor that takes constructs a unique ID by digesting
	 * arbitrary data given as a string.
	 * @param id The data to digest.
	 */
	public UniqueId(String id) {
		mId = digest(id);
	}

	/**
	 * Constructor that constructs a random unique ID using the 
	 * system's random number generator.
	 */
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

	/**
	 * Returns a representation of the UniqueId hash as a hex-string.
	 * @return A hex string representing this UniqueId hash
	 */
	@Override
	public String toString() {
		return mId;
	}
}
