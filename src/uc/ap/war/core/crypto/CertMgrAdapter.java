package uc.ap.war.core.crypto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.core.ex.SecurityServiceException;
import uc.ap.war.core.ex.SecurityServiceNotReadyException;

public class CertMgrAdapter {
    private static final Logger log = Logger.getLogger(CertMgrAdapter.class);
    static CertMgrAdapter INS = new CertMgrAdapter();

    public static CertMgrAdapter ins() {
        return INS;
    }

    public static void main(String args[])
            throws SecurityServiceNotReadyException {
        PropertyConfigurator.configure("log4j.properties");

        CertMgrAdapter.ins().init("localhost", 1097);
    }

    private boolean inited = false;
    private boolean karnReady = false;
    private Class certMgrC = null;
    private Object certMgr = null;
    private BigInteger myPubExp = null;
    private BigInteger myPubMod = null;

    private CertMgrAdapter() {
        // prevent instantiation by others
    }

    public BigInteger encryptWithMonPubKey(final BigInteger bi)
            throws SecurityServiceException {
        if (!inited) {
            throw new SecurityServiceNotReadyException();
        }
        try {
            final Method encryptWithMonPubKey = certMgrC.getMethod(
                    "encryptWithMonPubKey", new Class[] { BigInteger.class });
            final BigInteger encryptedBi = (BigInteger) encryptWithMonPubKey
                    .invoke(certMgr, new Object[] { bi });
            log.debug("encrypted msg: " + encryptedBi);
            return encryptedBi;
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error(e);
            throw new SecurityServiceException();
        }
    }

    public BigInteger getMyHalf() throws SecurityServiceException {
        if (!inited) {
            throw new SecurityServiceNotReadyException();
        }
        try {
            final Method getEncryptedMyHalf = certMgrC
                    .getMethod("getEncryptedMyHalf");
            return (BigInteger) getEncryptedMyHalf.invoke(certMgr);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            log.error(e);
            throw new SecurityServiceException();
        }
    }

    public String getMyHalfStr() throws SecurityServiceException {
        return getMyHalf().toString(32);
    }

    public BigInteger getMyPublicKeyExp() throws SecurityServiceException {
        if (!inited) {
            throw new SecurityServiceNotReadyException();
        }
        if (myPubExp != null) {
            return myPubExp;
        }
        try {
            final Method getMyPublicKeyExp = certMgrC
                    .getMethod("getMyPublicKeyExp");
            myPubExp = (BigInteger) getMyPublicKeyExp.invoke(certMgr);
            log.debug("my public key exp: " + myPubExp);
            return myPubExp;
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error(e);
            throw new SecurityServiceException();
        }
    }

    public String getMyPublicKeyExpStr() throws SecurityServiceException {
        return getMyPublicKeyExp().toString(32);
    }

    public BigInteger getMyPublicKeyMod() throws SecurityServiceException {
        if (!inited) {
            throw new SecurityServiceNotReadyException();
        }
        if (myPubMod != null) {
            return myPubMod;
        }
        try {
            final Method getMyPublicKeyMod = certMgrC
                    .getMethod("getMyPublicKeyMod");
            myPubMod = (BigInteger) getMyPublicKeyMod.invoke(certMgr);
            log.debug("my public key mod: " + myPubMod);
            return myPubMod;
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error(e);
            throw new SecurityServiceException();
        }
    }

    public String getMyPublicKeyModStr() throws SecurityServiceException {
        return getMyPublicKeyMod().toString(32);
    }

    public BigInteger getShareKarnSecret(final String monHalfKey)
            throws SecurityServiceException {
        if (!inited) {
            throw new SecurityServiceNotReadyException();
        }
        try {
            final Method getShareKarnSecret = certMgrC.getMethod(
                    "getShareKarnSecret", new Class[] { String.class });
            final BigInteger karnSec = (BigInteger) getShareKarnSecret.invoke(
                    certMgr, new Object[] { monHalfKey });
            karnReady = true;
            return karnSec;
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            log.error(e);
            throw new SecurityServiceException();
        }
    }

    public void init(final String regHost, final int regPort)
            throws SecurityServiceNotReadyException {
        try {
            certMgrC = Class.forName("CertMgr");
            certMgr = certMgrC.newInstance();
            final Method connect = certMgrC.getMethod("connect", new Class[] {
                    String.class, Integer.TYPE });
            inited = (boolean) connect.invoke(certMgr, new Object[] { regHost,
                    new Integer(regPort) });
            if (!inited) {
                throw new SecurityServiceNotReadyException();
            }
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException
                | SecurityException | ClassNotFoundException
                | InstantiationException e) {
            log.error(e);
            throw new SecurityServiceNotReadyException();
        }
    }

    public boolean karnReady() {
        return karnReady;
    }
}
