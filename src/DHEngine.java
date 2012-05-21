

// DHEngine.java                                              -*- Java -*-
//    The engine for Diffie-Hellman
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
// $Source: /home/franco/.../Project/DHEngine.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2008/04/03 01:33:07 $
//
// $Log: DHEngine.java,v $
// Revision 1.1.1.1  2008/04/03 01:33:07  franco
//
//
// Revision 0.2  1998/11/30 18:59:03  bkuhn
//   -- latest changes from Robert
//
// Revision 1.2  1998/11/30 18:49:25  robert
// Added Wrapper for the KEy Object
//
// Revision 1.1  1998/11/30 13:51:55  robert
// Initial revision
//
// Revision 0.1  1998/11/30 04:57:28  bkuhn
//   # initial version
//

import java.math.BigInteger;
import java.security.SecureRandom;
 
/*
 * class DHEngine 
 * Heres where you will find all of the Diffie-Hellman bits.
 * You can generate DH Public Keys if you like, but you may get bored.
 * Did I mention that it takes a very long time?
 * Setting up the SecureRandom generator is time consuming, but you never
 * need more than one of them.
 * DH Key exchange produces about 512 bits worth of secret key.
 * You may opt to use only some of that.
 *
 * You may wish to remove the print statements from the Constructor
 *
 * How to use:
 *  a.  Instantiate one. - Feed it the offical DHKeyObject 
 *  b.  Send the other side the string you get back from getExchangeKey();
 *  c.  Get the exchange key from the other guy,
 *      feed it to setExchangeKey();
 *
 *  OPTIONAL: do b & c in reverse order, as needed.
 *
 *  d.  Retrieve the shared secret key with getSharedKey();  
 */

class DHEngine {

    static SecureRandom sr = null; /* This is expensive.  We only need one */

    final int keysize = 512;           /* Default size, in bits */
    final int ARBITRARY_CONSTANT = 80; /* Sort of */
    final int RADIX = 32;              /* All Keys are output base 32 */

    private DHKeyObject key;        /* Heres the basic Key gadget */
    
    private BigInteger x;     /* This is our personal secret Key */
    private BigInteger x_pub; /* This is our exchange Key */

    private BigInteger s_secret;

    /* Use this one */
    
    DHEngine(DHKeyObject dhk) {
        key = dhk;
        if (sr == null) sr = new SecureRandom();

        x = new BigInteger(keysize,sr); /* Generate our secret Key */
        x_pub = key.g.modPow(x,key.n);
        s_secret = BigInteger.valueOf(0); 
        // System.out.println("Done");
    }
   
    /*
     * This method is used for key initialization 
     * You probably won't need it 
     */

    DHEngine() {
        // System.out.print("Intializing DHEngine ");
        if (sr == null) sr = new SecureRandom();
        key = MakeKey(keysize,ARBITRARY_CONSTANT);
        
        x = new BigInteger(keysize,sr); /* Generate our secret Key */
        x_pub = key.g.modPow(x,key.n);
        s_secret = BigInteger.valueOf(0); 
        System.out.println("Done"); 
    }

    /* 
     * Return the Public Key
     */

    DHKeyObject getKeyObject() { return key; }

    /*
     * This is where you get your key to send the other side
     */

    public String getExchangeKey() {
        return(x_pub.toString(RADIX));
    }

    /*
     * Feed their key into this routine.
     */

    public boolean setExchangeKey(String their_key) {
        try {
            BigInteger them = new BigInteger(their_key,RADIX);
            s_secret = them.modPow(x,key.n);
            return(true);
        }
        catch (NumberFormatException e) {
            System.err.println("Malformed DH Key");
            return(false);
        }
    }

    /* 
     * When you are done, retrieve the shared secret key here 
     */
    
    BigInteger getSharedKey() {
        return(s_secret);
    }

    /* 
     * Initial Public Key Generation - This can be VERY time consuming
     */

    private DHKeyObject MakeKey(int size, int quality) { 
        final BigInteger ONE = BigInteger.valueOf(1);
        final BigInteger TWO = BigInteger.valueOf(2);
        final BigInteger NEGONE = BigInteger.valueOf(-1);

        BigInteger n = null;
        BigInteger g = null;
        BigInteger x = null;
        boolean good_enough;

        int counter = 0;
        
	System.out.println("Initializing DHEngine ");
        System.out.print("Looking for a suitable n: ");

        good_enough = false; /* Guilty until proven innocent */

        do {
            n = new BigInteger(size,quality,sr);
            x = n.subtract(ONE).divide(TWO);

            if(x.isProbablePrime(quality)) good_enough = true;
	    
            System.out.print(++counter + " " );	
        } while (!good_enough);

        System.out.println("\nFound " + n.toString(RADIX));
	
        /* The following is not a comprehensive czech */

        good_enough = false;

        /* Improved technique for generating G courtesy of Brad */
        System.out.print("Looking for a suitable g: ");
        counter = 0;

        BigInteger negOneModP = NEGONE.mod(n);

        do {
            g = new BigInteger(size-2,sr);
            if ( (g.compareTo(negOneModP) != 0) &&
                 (g.compareTo(g.modPow(x ,negOneModP)) == 0) );
            good_enough = true;
            System.out.print(++counter + " " );	
        } while (!good_enough);

        return(new DHKeyObject(n,g,"DHEngine $Revision: 1.1.1.1 $/"+keysize));
    }

    public String toString() {
	StringBuffer scratch = new StringBuffer();
	scratch.append("Secret Key(x): " + x.toString(RADIX) + "\n" );
	scratch.append("Public Key(X): " + x_pub.toString(RADIX) + "\n" );
	scratch.append("Shared Key   : " + s_secret.toString(RADIX) );
	return scratch.toString();
    }
}




