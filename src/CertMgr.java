import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class CertMgr {
    private static final String MY_KEY_FILE_NAME = "MyKey.dat";
    private static final Logger log = Logger.getLogger(CertMgr.class);

    public static void main(String[] args) throws NotBoundException,
            UnknownHostException, NoSuchAlgorithmException, IOException,
            ClassNotFoundException {
        PropertyConfigurator.configure("log4j.properties");

        CertMgr mgr = new CertMgr();
        mgr.connect("localhost", 1099);
    }

    private RSA myKey;
    private PlayerCertificate monCert;
    private BigInteger myHalf = null;

    public CertMgr() throws FileNotFoundException, ClassNotFoundException,
            IOException {
        initMyRsaKey();
    }

    public boolean connect(final String regHost, final int regPort) {
        final StringBuilder uri = new StringBuilder("rmi://");
        uri.append(regHost).append(":").append(regPort);
        uri.append("/CertRegistry");
        try {
            log.debug("got rmi registry uri: " + uri.toString());
            final CertRemote cert = (CertRemote) Naming.lookup(uri.toString());
            monCert = cert.getCert("MONITOR");
            log.info("get cert " + monCert);
            return true;
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            log.error(e);
            return false;
        }
    }

    public BigInteger encryptWithMonPubKey(final BigInteger bi) {
        return monCert.getPublicKey().encrypt(bi);
    }

    public BigInteger getEncryptedMyHalf() {
        if (myHalf == null) {
            final SecureRandom sr = new SecureRandom();
            myHalf = new BigInteger(256, sr);
        }
        return monCert.getPublicKey().encrypt(myHalf);
    }

    public BigInteger getMyPublicKeyExp() {
        return myKey.publicKey().getExponent();
    }

    public BigInteger getMyPublicKeyMod() {
        return myKey.publicKey().getModulus();
    }

    public BigInteger getShareKarnSecret(String monHalfStr) {
        final BigInteger monHalf = myKey.decryptNum(new BigInteger(monHalfStr,
                32));
        byte myHalfB[] = myHalf.toByteArray();
        byte monHalfB[] = monHalf.toByteArray();

        int keySize = 512;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(keySize / 8);

        for (int i = 0; i < keySize / 16; i++) {
            bos.write(monHalfB[i]);
            bos.write(myHalfB[i]);
        }
        return new BigInteger(1, bos.toByteArray());
    }

    private void initMyRsaKey() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        try {
            FileInputStream fIn = new FileInputStream(MY_KEY_FILE_NAME);
            ObjectInputStream in = new ObjectInputStream(fIn);
            myKey = (RSA) in.readObject();
            in.close();
            fIn.close();
            log.info("my rsa key loaded.");
            return;
        } catch (FileNotFoundException e) {
            log.error(e);
            log.info("My player key not found, gonna initialize it.");
        }
        // init my key
        myKey = new RSA(); /* Default is 512-bit key size */
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                MY_KEY_FILE_NAME));
        oos.writeObject(myKey);
        oos.close();
        log.info("my rsa key built.");
    }
}
