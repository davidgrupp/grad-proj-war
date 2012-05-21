

// DHKeyObject.java                                              -*- Java -*-
//    The DH key object
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
// $Source: /home/franco/.../Project/DHKeyObject.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2008/04/03 01:33:07 $
//
// $Log: DHKeyObject.java,v $
// Revision 1.1.1.1  2008/04/03 01:33:07  franco
//
//
// Revision 0.2  1998/11/30 18:59:04  bkuhn
//   -- latest changes from Robert
//
// Revision 1.1  1998/11/30 13:53:36  robert
// Initial revision
//
// Revision 0.1  1998/11/30 03:25:28  bkuhn
//   # initial version
//

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

/*
 * This object is used for Public Key Exchange. 
 * The Crypto routines require it.  I haven't put the heavy
 * duty methods in here because I want it to stay small
 */ 

class DHKeyObject implements Serializable {

    BigInteger n,g;    /* These two make up the public Key */

    String Description;
    Date created;

    DHKeyObject(BigInteger N, BigInteger G,String what) {
	n = N;
	g = G;

	Description = what;
	created = new Date();
    }

    /* You may wish to customize the following */

    public String toString() {
	StringBuffer scratch = new StringBuffer();
	scratch.append("Public Key(n): " + n.toString(32) + "\n" );
	scratch.append("Public Key(g): " + g.toString(32) + "\n" );
	scratch.append("Description: "   + Description  + "\n" );
	scratch.append("Created: "       + created );
	return scratch.toString();
    }
}
