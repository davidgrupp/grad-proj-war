import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.crypto.KarnBufferedReader;
import uc.ap.war.crypto.KarnPrintWriter;

public class CertMgr {
    private static final Logger log = Logger.getLogger(CertMgr.class);
    public static void main(String[] args) throws NotBoundException,
            UnknownHostException, NoSuchAlgorithmException, IOException {
        PropertyConfigurator.configure("log4j.properties");

        CertMgr mgr = new CertMgr();
        log.debug(mgr.getMyHalf());
    }
    private RSA myKey;
    private BigInteger participantHalf;
    private PlayerCertificate monCert;
//    private PrintWriter out;
//    private BufferedReader in;
    private BigInteger sharedSecret;

    public CertMgr() throws NotBoundException, FileNotFoundException,
            IOException {
        buildRsaPrivateKey();
        CertRemote r = (CertRemote) Naming
                .lookup("rmi://localhost:1097/CertRegistry");
        monCert = r.getCert("MONITOR");
        log.info("get cert " + monCert);
    }

    public void createMyRsaPrivateKey() throws FileNotFoundException,
            IOException {
        RSA myPrivateKey = new RSA(); /* Default is 512-bit key size */
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                "PlayerKey"));
        oos.writeObject(myPrivateKey);
        oos.close();
    }

//    public BigInteger createShareKarnSecret(String monHalfStr) {
//        final BigInteger monHalfNum = myKey.decryptNum(new BigInteger(
//                monHalfStr, 32));
//        byte myHalf[] = myKey.publicKey().getModulus().toByteArray();
//        byte monHalf[] = monHalfNum.toByteArray();
//
//        int keySize = 512;
//        ByteArrayOutputStream bos = new ByteArrayOutputStream(keySize / 8);
//
//        for (int i = 0; i < keySize / 16; i++) {
//            bos.write(monHalf[i]);
//            bos.write(myHalf[i]);
//        }
//        sharedSecret = new BigInteger(1, bos.toByteArray());
//        return sharedSecret;
//    }
    
    public BigInteger createShareKarnSecret(String monHalfStr) {
        final BigInteger monHalfNum = myKey.decryptNum(new BigInteger(
                monHalfStr, 32));
        byte myHalf[] = participantHalf.toByteArray();
        byte monHalf[] = monHalfNum.toByteArray();

        int keySize = 512;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(keySize / 8);

        for (int i = 0; i < keySize / 16; i++) {
            bos.write(monHalf[i]);
            bos.write(myHalf[i]);
        }
        sharedSecret = new BigInteger(1, bos.toByteArray());
        return sharedSecret;
    }    

    public BigInteger encryptWithMonPubKey(final BigInteger bi) {
        return monCert.getPublicKey().encrypt(bi);
    }

    public String getMyHalf() throws RemoteException, MalformedURLException,
            NotBoundException {
        final BigInteger myModulus = myKey.publicKey().getModulus();
        final BigInteger encryptedM = monCert.getPublicKey().encrypt(myModulus);
        return encryptedM.toString(32);
    }
    
    public String getParticipantHalf() {
        final SecureRandom sr = new SecureRandom();
        participantHalf = new BigInteger(256, sr);
        final BigInteger encryptedM = monCert.getPublicKey().encrypt(participantHalf);
        return encryptedM.toString(32);
    }

    public BigInteger getMyPublicKeyExp() {
        return myKey.publicKey().getExponent();
    }

    public BigInteger getMyPublicKeyMod() {
        return myKey.publicKey().getModulus();
    }

//    public void makeCert() {
//        final String cmd = "MAKE_CERTIFICATE "
//                + myKey.publicKey().getExponent().toString(32) + " "
//                + myKey.publicKey().getModulus().toString(32);
//        if (karnOut != null) {
//            karnOut.println(cmd);
//        } else {
//            out.println(cmd);
//        }
//    }

    private void buildRsaPrivateKey() throws FileNotFoundException, IOException {
        myKey = new RSA(); /* Default is 512-bit key size */
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                "PlayerKey"));
        oos.writeObject(myKey);
        oos.close();
        log.info("player rsa key built.");
    }

}
