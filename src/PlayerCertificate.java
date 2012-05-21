/****************************************************************
  * PlayerCertificate - A certificate identity for the player,  *
  * stored at the monitor's database.                           *
  ***************************************************************/
/*
   Written by: Coleman Kane <cokane@cokane.org>
   
   Written For: Dr. John Franco, University of Cincinnati ECECS dept.
                20-ECES-653: Network Security

   Copyright(c): 2003, by Coleman Kane
   
   $Id: PlayerCertificate.java,v 1.1 2008/05/18 14:48:30 franco Exp $

   $Log: PlayerCertificate.java,v $
   Revision 1.1  2008/05/18 14:48:30  franco
   *** empty log message ***

   Revision 1.1.1.1  2008/04/03 01:33:08  franco


   Revision 1.1  2004/01/27 17:28:24  cokane
   Initial revision

*/
import java.math.BigInteger;
import java.io.Serializable;
import java.security.*;

public class PlayerCertificate implements Serializable {
	private BigInteger h;
	private String playerName;
    private PubRSA publicKey;

	/** Load the player certificate by taking the supplied number,
	  corresponding to the CA-Signed SHA-1 hash of the named player's
	  public key, also take the actualy Public Key for public posting and,
      as a String the player's Name. **/
	public PlayerCertificate(PubRSA clientPubKey, String name,
            BigInteger hash) {
            h = hash;
            playerName = name;
            publicKey = clientPubKey;
	}

	/** Returns the registered name of the player to which this certificate has
	    been assigned. */
	public String getPlayerName() {
		return playerName;
	}
	
	/** Returns the cetrificate itself, as a base32 number string. */
	public String getCertificate() {
		return h.toString(32);
	}

    /** Returns the public key object that was registered. */
    public PubRSA getPublicKey() {
        return publicKey;
    }
}
