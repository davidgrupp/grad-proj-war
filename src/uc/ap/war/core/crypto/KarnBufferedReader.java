package uc.ap.war.core.crypto;

/******* Class KarnPrintWriter **********
 Copyright (c) 2004 Coleman Kane

 Written for: John V. Franco
 University of Cincinnati
 Computer Science
 20-ECES-694 Advanced Topics in Computer Science
 **/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;

public class KarnBufferedReader extends BufferedReader {
    /*********************
     * The Karn Symmetric Encryption portion of the Monitor client
     ********************/
    private BigInteger sharedSecret;
    private MessageDigest md;

    public KarnBufferedReader(Reader in, BigInteger sKey) {
        super(in);
        sharedSecret = sKey;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (Exception x) {
            System.out.println("Can't make SHA digests");
            md = null;
        }
    }

    public String decrypt(String msg) {
        String result = "";
        if (msg == null)
            return "";
        BigInteger msgNum = new BigInteger(msg, 32);
        byte m[] = msgNum.toByteArray();
        md.reset();
        int mOffset = 0;

        if (m[0] == 0x2a)
            for (; m.length > 1 + mOffset;) {
                byte key[] = sharedSecret.toByteArray();
                byte mLeft[] = new byte[20];
                byte mRight[] = new byte[20];
                byte kLeft[] = new byte[key.length / 2];
                byte kRight[] = new byte[key.length / 2];
                byte tmp[] = new byte[20];

                for (int i = 0; i < 20; i++) {
                    mRight[i] = m[i + 21 + mOffset];
                    mLeft[i] = m[i + 1 + mOffset];
                }

                for (int i = 0; i < key.length / 2; i++) {
                    kRight[i] = key[i + key.length / 2];
                    kLeft[i] = key[i];
                }

                md.update(mRight);
                md.update(kRight);
                tmp = md.digest();
                for (int i = 0; i < tmp.length; i++)
                    tmp[i] ^= mLeft[i];
                try {
                    result += new String(tmp, "ISO-8859-1");
                } catch (UnsupportedEncodingException uex) {
                    System.out.println("Bad encoding, you are screwed");
                    return null;
                }

                md.reset();
                md.update(tmp);
                md.update(kLeft);
                tmp = md.digest();
                for (int i = 0; i < tmp.length; i++)
                    tmp[i] ^= mRight[i];
                try {
                    result += new String(tmp, "ISO-8859-1");
                } catch (UnsupportedEncodingException uex) {
                    System.out.println("Bad encoding, you are screwed");
                    return null;
                }
                md.reset();
                mOffset += 40;
            }
        return result;
    }

    public String readLine() throws IOException {
        String line = decrypt(super.readLine());
        if (line.indexOf(0) > -1)
            line = line.substring(0, line.indexOf(0));
        return line;
    }
}
