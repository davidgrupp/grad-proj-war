package uc.ap.war.crypto;

/******* Class KarnPrintWriter **********
  Copyright (c) 2004 Coleman Kane

  Written for: John V. Franco
               University of Cincinnati
               Computer Science
               20-ECES-694 Advanced Topics in Computer Science
**/
import java.io.*;
import java.lang.*;
import java.math.*;
import java.security.*;

public class KarnPrintWriter extends PrintWriter {
	/******************************************************
	  ** class KarnPrintWriter: Implements an encryptor to
	  **   take plaintext from the user and write it to a
	  **   PrintWriter stream as output
	  ******************************************************/

	/** Stores the shared key for the encrypted session **/
	private BigInteger sharedSecret;

	/** MessageDigest common to multiple functions in class, 
	    implements SHA-1 **/
	private MessageDigest md;

	/** SecureRandom to pad the shorter blocks with random data, and
	    not just all zeros **/
	private SecureRandom r;

	/** Initialize a new PrintWriter with the specified OutputStream,
	    autoFlush on/off and a designated shared key **/
	public KarnPrintWriter(Writer out, boolean autoFlush, BigInteger sharedKey) 
		throws NoSuchAlgorithmException {
		super(out, autoFlush);
		sharedSecret = sharedKey;
		r = SecureRandom.getInstance("SHA1PRNG");
		r.setSeed(System.currentTimeMillis()); /* Seed the generator */
		md = MessageDigest.getInstance("SHA-1");
	}

	/** Initialize the PrintWriter with a specific OutputStream and
	    shared encryption key, but set autoFlush to false **/
	public KarnPrintWriter(Writer out, BigInteger sharedKey)
		throws NoSuchAlgorithmException {
		super(out);
		sharedSecret = sharedKey;
		r = SecureRandom.getInstance("SHA1PRNG");
		r.setSeed(System.currentTimeMillis()); /* Seed the generator */
		md = MessageDigest.getInstance("SHA-1");
	}

	/** General encryption function called by all print functions **/
	public String encrypt(String msg) {
		String val = "";
		md.reset();
		byte s[];
		byte m[] = new byte[40];
		try {
			s = msg.getBytes("ISO-8859-1"); /* Grab as ASCII values */
		} catch(UnsupportedEncodingException uex) {
			return null;
		}
		byte mLeft[] = new byte[20];
		byte mRight[] = new byte[20];
		byte key[] = sharedSecret.toByteArray();
		byte kLeft[] = new byte[key.length/2];
		byte kRight[] = new byte[key.length/2];

		/* Create the array, padding to the next block size */
		int cSize;
		for(cSize = 1; cSize < msg.length() + 1; cSize += 40);
		byte c[] = new byte[cSize];
		int cOffset = 0;

		for(; cOffset < s.length; cOffset += 40) {
			byte padByte[] = new byte[1];
			padByte[0] = 0;
			for(int i = 0; i < 40; i++) {
				if((i + cOffset) < s.length) {
					m[i] = s[i + cOffset];
				} else {
					m[i] = padByte[0];
					r.nextBytes(padByte);
				}
			}

			for(int i = 0; i < 20; i++) {
				mLeft[i] = m[i];
				mRight[i] = m[i + 20];
			}

			for(int i = 0; i < key.length/2; i++) {
				kLeft[i] = key[i];
				kRight[i] = key[i + key.length/2];
			}

			md.reset();
			md.update(mLeft);
			md.update(kLeft);
			byte tmp[] = md.digest();

			/* Write the right side */
			for(int i = 0; i < 20; i++)
				c[i + 21 + cOffset] = tmp[i] ^= mRight[i];

			md.reset();
			md.update(tmp);
			md.update(kRight);
			tmp = md.digest();

			/* Write the left side */
			for(int i = 0; i < 20; i++)
				c[i + 1 + cOffset] = (byte)(tmp[i] ^ mLeft[i]);
		}
		c[0] = 42; /* Put the guard byte at the beginning */

		BigInteger message = new BigInteger(c);

		return message.toString(32);
	}

	/** Print a complete encrpyted line (record) **/
	public void println(String msg) {
		super.println(encrypt(msg));
	}
}
