package uc.ap.war.crypto;

// Cipher.java                                              -*- Java -*-
//    The Ciphering object
//
// Copyright(C) 1998 Robert Sexton
// You can do anything you want with this, except pretend you
// wrote it.
//
// Written   :   Robert Sexton         University of Cincinnati
//   By          
//
// Written   :   John Franco
//   For         Special Topics: Java Programming
//               15-625-595-001, Fall 1998
// RCS       :
//
// $Source: /home/franco/.../Project/Cipher.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2008/04/03 01:33:07 $
//
// $Log: Cipher.java,v $
// Revision 1.1.1.1  2008/04/03 01:33:07  franco
//
//
// Revision 0.3  1998/12/02 06:51:33  bkuhn
//   -- added a bounds check
//
// Revision 0.2  1998/11/30 18:59:05  bkuhn
//   -- latest changes from Robert
//
// Revision 1.2  1998/11/30 18:48:11  robert
// Added Guard Bytes.
//
// Revision 1.1  1998/11/30 13:53:59  robert
// Initial revision
//
// Revision 0.1  1998/11/30 03:27:12  bkuhn
//    # initial version
//

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/*
 * class Cipher
 * Here's where the symmetrical encryption happens.
 * instantiate one of these, passing in a secret key (A BigInteger)
 *
 * This is an implementation of Phil Karn's algorithm, as described by
 * Schneier.  Text is padded to the nearest 40 bytes.
 * Bigger padding sizes would result in less infomation leakage.
 * This algoritm is vulnerable to chosen plaintext attacks under some
 * specific circumstances, but on the whole its pretty secure
 *
 */

class Cipher {

    final int RADIX=32;
    final int PADSIZE=40; /* Plaintext buffer */ 
    
    private byte key[];
    private byte key_left[];
    private byte key_right[];

    static SecureRandom sr = null;  /* This is expensive.  We only need one */
    MessageDigest md = null; 

    Cipher(BigInteger bi) {
	if (sr == null) sr = new SecureRandom();
	key = bi.toByteArray(); 
	// System.out.println("C: key " + new BigInteger(key).toString(RADIX));

	/* Digest encryption needs keys split into two halves */
	key_left =  new byte[key.length/2];
	key_right = new byte[key.length/2];

	/* I anxiously await a more elegant solution to the following */

	for (int i = 0;i<key.length/2;i++) {
	    key_left[i] = key[i];
	    key_right[i] = key[i+key.length/2];
	}

	// System.out.println("C: keyl " + 
	//	   new BigInteger(key_left).toString(RADIX));
	// System.out.println("C: keyr " + 
	//	   new BigInteger(key_right).toString(RADIX));

	try { md = MessageDigest.getInstance("SHA"); }
	catch (NoSuchAlgorithmException e) {
	    System.err.println("Yow! NoSuchAlgorithmException." +
			       " Abandon all hope");
	}
    }
    
    /*
     * Encrypt the string using the karn algorithm 
     */

    String Encrypt(String plaintext) {
	byte[] plain_left,plain_right;
	byte[] ciph_left,ciph_right;
	byte[] digest;

	/*
	 * These buffers are used for the encryption.
	 */
	byte input[] = StringToBytes(plaintext); /* Pad the string */
	
	// System.out.println("C: i  " +
	//		   new BigInteger(input).toString(RADIX));


	plain_left =  new byte[PADSIZE/2];
	plain_right = new byte[PADSIZE/2];

	ciph_left =  new byte[PADSIZE/2];
	ciph_right =  new byte[PADSIZE/2];

	digest = new byte[PADSIZE/2]; /* Temp storage for the hash */

	int cursor = 0; /* Our pointer into the workspace */
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	out.write(42); /* Guard Byte for the ciphertext */

	while(cursor < input.length) {
	    // Copy the next slab into the left and right 
	    for(int i=0; i<PADSIZE/2; i++) {
		plain_left[i] = input[cursor + i];
		plain_right[i] = input[cursor + PADSIZE/2 + i];	
	    }

	    // System.out.println("C: pl " + 
	    //	       new BigInteger(plain_left).toString(RADIX));
	    // System.out.println("C: pr " + 
	    //	       new BigInteger(plain_right).toString(RADIX));
	    

	    /* Hash the left plaintext with the left key */
	    md.reset(); /* Start the hash fresh */
	    md.update(plain_left);
	    md.update(key_left);
	    digest = md.digest(); /* Get out the digest bits */
	    /* XOR the digest with the right plaintext for the right c-text */
	    for (int i=0; i<PADSIZE/2;i++) { /* Right half */
		ciph_right[i] = (byte)(digest[i] ^ plain_right[i]);
	    }

	    //System.out.println("C: cr " + 
	    //	       new BigInteger(ciph_right).toString(RADIX));

	    /* Now things get a little strange */
	    md.reset();
	    md.update(ciph_right);
	    md.update(key_right);
	    digest = md.digest();
	    for (int i=0; i<PADSIZE/2;i++) {
		ciph_left[i] = (byte) (digest[i] ^ plain_left[i]);
	    }

	    // System.out.println("C: cl " + 
	    //	       new BigInteger(ciph_left).toString(RADIX));

	    out.write(ciph_left,0,PADSIZE/2);
	    out.write(ciph_right,0,PADSIZE/2);
	    cursor += PADSIZE;
	}

	BigInteger bi_out = new BigInteger(out.toByteArray());
	// System.out.println("C: res  " + bi_out.toString(RADIX));

	return(bi_out.toString(RADIX));
    }

    /*
     * Decrypt the ciphertext by running Karn in reverse
     */

    String Decrypt(String ciphertext) {
	BigInteger bi;
	byte input[];
	byte[] plain_left,plain_right;
	byte[] ciph_left,ciph_right;
	byte[] digest;

	/* Convert to a BigInteger, extract the bytes */
	bi = new BigInteger(ciphertext,RADIX);
	input = bi.toByteArray();

	/* Strip guard byte */
	ByteArrayOutputStream scratch = new ByteArrayOutputStream();
	scratch.write(input,1,input.length - 1);
	input = scratch.toByteArray();

	// System.out.println("D: i    " +
	//	   new BigInteger(input).toString(RADIX));

	/*
	 * Decryption here - this is a reversal of Encrypt, above 
	 */
	plain_left =  new byte[PADSIZE/2];
	plain_right = new byte[PADSIZE/2];

	ciph_left =  new byte[PADSIZE/2];
	ciph_right =  new byte[PADSIZE/2];

	digest = new byte[PADSIZE/2]; /* Temp storage for the hash */

	int cursor = 0; /* Our pointer into the workspace */
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	while(cursor < input.length) {
	    // Copy the next slab into the left and right 
	    for(int i=0; i<PADSIZE/2; i++) {
		ciph_left[i] = input[cursor + i];
		ciph_right[i] = input[cursor + PADSIZE/2 + i];
	    }
	    
	    // System.out.println("D: cl " + 
	    //	       new BigInteger(ciph_left).toString(RADIX));
	    // System.out.println("D: cr " + 
	    //	       new BigInteger(ciph_right).toString(RADIX));
	    

	    md.reset(); /* Start the hash fresh */
	    md.update(ciph_right);
	    md.update(key_right);
	    digest = md.digest(); /* Get out the digest bits */
	    for (int i=0; i<PADSIZE/2;i++) { /* Right half */
		plain_left[i] = (byte)(digest[i] ^ ciph_left[i]);
	    }

	    // System.out.println("C: pl " + 
	    //	       new BigInteger(plain_left).toString(RADIX));


	    md.reset();
	    md.update(plain_left);
	    md.update(key_left);
	    digest = md.digest();
	    for (int i=0; i<PADSIZE/2;i++) {
		plain_right[i] = (byte)(digest[i] ^ ciph_right[i]);
	    }

	    out.write(plain_left,0,PADSIZE/2);
	    out.write(plain_right,0,PADSIZE/2);
	    cursor += PADSIZE;
	}
	return(StripPadding(out.toByteArray()));
    }

    /*
     * Conversion and padding 
     * New, improved, with something like PKCS.  We add a null to the 
     * end, then add PADSIZE bytes.
     */

    private byte[] StringToBytes(String input) {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	byte scratch[];

	scratch = input.getBytes();
	int len = input.length();

	buffer.write(scratch,0,len);
	buffer.write(0); /* Add the null */
	
	/* Add the padding */

	int padlen = PADSIZE - ((len + 1) % PADSIZE);
	scratch = new byte[padlen];
	sr.nextBytes(scratch);

	buffer.write(scratch,0,padlen);

	// System.out.println("Data: " + len + " Padding: " + padlen +
	//		   " Total: " + buffer.size());

	return(buffer.toByteArray());
    }

    /* 
     * Strip the header off the byte array and return the string 
     */
    private String StripPadding(byte input[]){
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	int i = 0;

	while(input[i] != 0 && i < input.length) {
	    buffer.write(input[i]);
	    i++;
	}

	// System.out.println("Returned bytes: " + i);
	return(new String(buffer.toByteArray()));
    }
}

