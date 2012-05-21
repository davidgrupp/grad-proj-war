
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

public class RSA implements Serializable {
	/** Constants I'll need later */
	private static BigInteger FOUR = new BigInteger("4");
	public  static BigInteger E = new BigInteger("65537");
    private int keySize;

	private static SecureRandom r;
	private BigInteger q, p;
	String Description;
	Date created;

    public RSA() {
        if(r == null)
            r = new SecureRandom();
        keySize = 512;
        gen();
    }

    public RSA(int keyLen) {
        if(r == null)
            r = new SecureRandom();
        keySize = keyLen;
        gen();
    }

	/** Generate a new set of keys (used for pre-serialization). **/
	public void gen() {
		Description = "RSA secretKey";
		created = new Date();
		p = new BigInteger(keySize/2, 100, r);
		do {
			p = new BigInteger(keySize/2, 100, r);
		} while(p.mod(RSA.FOUR).intValue() != 3);

		do {
			q = new BigInteger(keySize/2, 100, r);
		} while(q.mod(RSA.FOUR).intValue() != 3);
	}

	/** Returns a signature of m (m^d mod n) */
	public BigInteger signNum(BigInteger m) {
		BigInteger d = RSA.E.modInverse(
				q.subtract(BigInteger.ONE).multiply(
					p.subtract(BigInteger.ONE)));
		BigInteger n = p.multiply(q);

		return m.modPow(d, n);
	}

    /** Provide this to make code look more readable **/
    public BigInteger decryptNum(BigInteger cipherNum) {
        return signNum(cipherNum);
    }

	/** Returns String signature of pt, converts pt to a biginteger */
	public String sign(String pt) {
		try {
			BigInteger m = new BigInteger(1, pt.getBytes("ISO-8859-1"));
			return new String(signNum(m).toByteArray(), "ISO-8859-1");
		} catch(UnsupportedEncodingException uex) {
			/** LATIN-1 not supported, you are screwed */
			return null;
		}
	}

    /** Provide this to make code look more readable **/
    public String decrypt(String cipherText) {
        return sign(cipherText);
    }

	/** Returns a Public key to match this object */
	public PubRSA publicKey() {
		return new PubRSA(RSA.E, p.multiply(q));
	}
}
