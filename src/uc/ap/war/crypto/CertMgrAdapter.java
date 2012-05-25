package uc.ap.war.crypto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CertMgrAdapter {
    private static final Logger log = Logger.getLogger(CertMgrAdapter.class);

    public static void main(String[] args) throws ClassNotFoundException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException,
            NoSuchMethodException, SecurityException {

        PropertyConfigurator.configure("log4j.properties");
        final CertMgrAdapter adp = new CertMgrAdapter();
        final String myHalf = adp.getMyHalf();
        log.debug("My half: " + myHalf);
        final BigInteger karnSec = adp
                .createShareKarnSecret("5dmqccpo33ehiisrtlb6mb2tuq1h8vvjemarfqbcfhrnr39ed8m");
        log.debug("karn secret is " + karnSec.toString(32));
    }

    private Class certMgrC;

    private Object certMgr;

    public CertMgrAdapter() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        // initiate the certificate related logic
        certMgrC = Class.forName("CertMgr");
        certMgr = certMgrC.newInstance();
    }

    public BigInteger createShareKarnSecret(final String monHalfKey)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        Method createShareKarnSecret = certMgrC.getMethod(
                "createShareKarnSecret", new Class[] { String.class });
        final BigInteger karnSec = (BigInteger) createShareKarnSecret.invoke(
                certMgr, new String[] { monHalfKey });
        return karnSec;
    }

    public BigInteger encryptWithMonPubKey(final BigInteger bi)
            throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        final Method encryptWithMonPubKey = certMgrC.getMethod(
                "encryptWithMonPubKey", new Class[] { BigInteger.class });
        final BigInteger encryptedBi = (BigInteger) encryptWithMonPubKey
                .invoke(certMgr, new BigInteger[] { bi });
        log.debug("encrypted msg: " + encryptedBi);
        return encryptedBi;
    }

    public String getMyHalf() throws ClassNotFoundException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException,
            NoSuchMethodException, SecurityException {
        final Method getMyHalf = certMgrC.getMethod("getMyHalf");
        final String myHalf = (String) getMyHalf.invoke(certMgr);
        log.debug("My half: " + myHalf);
        return myHalf;
    }

    public String getParticipantHalf() throws ClassNotFoundException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InstantiationException,
            NoSuchMethodException, SecurityException {
        final Method getParticipantHalf = certMgrC
                .getMethod("getParticipantHalf");
        final String participantHalf = (String) getParticipantHalf
                .invoke(certMgr);
        log.debug("Participant half: " + participantHalf);
        return participantHalf;
    }

    public BigInteger getMyPublicKeyExp() throws NoSuchMethodException,
            SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Method getMyPublicKeyExp = certMgrC
                .getMethod("getMyPublicKeyExp");
        final BigInteger exp = (BigInteger) getMyPublicKeyExp.invoke(certMgr);
        log.debug("my public key exp: " + exp);
        return exp;
    }

    public String getMyPublicKeyExpStr() throws NoSuchMethodException,
            SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        return getMyPublicKeyExp().toString(32);
    }

    public BigInteger getMyPublicKeyMod() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        final Method getMyPublicKeyMod = certMgrC
                .getMethod("getMyPublicKeyMod");
        final BigInteger mod = (BigInteger) getMyPublicKeyMod.invoke(certMgr);
        log.debug("my public key mod: " + mod);
        return mod;
    }

    public String getMyPublicKeyModStr() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        return getMyPublicKeyMod().toString(32);
    }
}
