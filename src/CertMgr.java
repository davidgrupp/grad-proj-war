import java.io.BufferedReader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.crypto.KarnBufferedReader;
import uc.ap.war.crypto.KarnPrintWriter;

public class CertMgr {
    private static final Logger log = Logger.getLogger(CertMgr.class);
    private RSA myKey;
    private PlayerCertificate monCert;
    private PrintWriter out;
    private BufferedReader in;
    private BigInteger sharedSecret;
    private KarnBufferedReader karnIn;
    private KarnPrintWriter karnOut;

    public CertMgr() throws NotBoundException, FileNotFoundException,
            IOException {
        // myKey = new RSA(256);
        buildRsaPrivateKey();
        CertRemote r = (CertRemote) Naming
                .lookup("rmi://localhost/CertRegistry");
        monCert = r.getCert("MONITOR");
        log.info("get cert " + monCert);
    }

    public BigInteger encryptWithMonPubKey(final BigInteger bi) {
        return monCert.getPublicKey().encrypt(bi);
    }

    private void buildRsaPrivateKey() throws FileNotFoundException, IOException {
        myKey = new RSA(); /* Default is 512-bit key size */
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                "PlayerKey"));
        oos.writeObject(myKey);
        oos.close();
        log.info("player rsa key built.");
    }

    public BigInteger getMyPublicKeyExp() {
        return myKey.publicKey().getExponent();
    }

    public BigInteger getMyPublicKeyMod() {
        return myKey.publicKey().getModulus();
    }

    public String getMyHalf() throws RemoteException, MalformedURLException,
            NotBoundException {
        // final RSA myKey = new RSA(256);
        final BigInteger myModulus = myKey.publicKey().getModulus();
        final BigInteger encryptedM = monCert.getPublicKey().encrypt(myModulus);
        return encryptedM.toString(32);
    }

    // public String getMyHalf(final BigInteger myModulus) throws
    // RemoteException,
    // MalformedURLException, NotBoundException {
    // final CertRemote r = (CertRemote) Naming
    // .lookup("rmi://localhost/CertRegistry");
    // monCert = r.getCert("MONITOR");
    // final BigInteger encryptedM = monCert.getPublicKey().encrypt(myModulus);
    // return encryptedM.toString(32);
    // }

    public void startConnection(final String pid) throws UnknownHostException,
            IOException, NotBoundException, NoSuchAlgorithmException {
        final Socket sock = new Socket("localhost", 8180);
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        out = new PrintWriter(sock.getOutputStream(), true);

        final BigInteger modulus = myKey.publicKey().getModulus();
        boolean result = false;
        String monHalfStr = "";
        boolean required = false;
        String reqCmd = "";
        boolean done = false;
        for (String msg = readDir(); msg != null; msg = readDir()) {
            System.out.println(msg);
            if (msg.startsWith("RESULT")) {
                result = true;
                String[] arg = msg.split("\\s+", 3);
                if (arg.length == 3 && arg[1].equals("IDENT")) {
                    monHalfStr = arg[2];
                    System.out.println("got mon half number: " + monHalfStr);
                    final BigInteger sharedSec = createShareKarnSecret(monHalfStr);
                    createKarnChannel(sharedSec);
                }
            }
            if (msg.startsWith("REQUIRE")) {
                required = true;
                reqCmd = msg.split("\\s+", 2)[1];
            }
            if (msg.startsWith("WAITING")) {
                if (required) {
                    switch (reqCmd) {
                    case "IDENT":
                        String cmd = "IDENT " + pid + " " + getMyHalf();
                        System.out.println("==> " + cmd);
                        out.println(cmd);
                        break;
                    }
                }
            }
        }
    }

    private String readDir() throws IOException {
        if (karnIn != null) {
            log.debug("KARN channel");
            return karnIn.readLine();
        } else {
            log.debug("Plain channel");
            return in.readLine();
        }
    }

    private void testKarnChannel() throws IOException, NoSuchAlgorithmException {
        // karnOut.println("IDENT " + id);
        boolean requirePwd = false;
        boolean certResult = false;
        String certNum = "";
        for (String msg = karnIn.readLine(); msg != null; msg = karnIn
                .readLine()) {
            if (msg.equals("")) {
                System.out
                        .println("Got empty msg, probably connection lost, gonna quit...");
                break;
            }
            System.out.println("testing karn channel: " + msg);
            if (msg.startsWith("REQUIRE")) {
                String[] arg = msg.split("\\s+", 2);
                if (arg.length == 2 && arg[1].equals("PASSWORD")) {
                    requirePwd = true;
                }
            } else if (msg.startsWith("WAITING") && requirePwd) {
                final String cmd = "PASSWORD mylittlepw";
                System.out.println("==> " + cmd);
                karnOut.println(cmd);
                requirePwd = false;
            } else if (msg.startsWith("RESULT")) {
                final String[] args = msg.split("\\s+", 3);
                if (args.length == 3 && args[1].equals("CERTIFICATE")) {
                    certResult = true;
                    certNum = args[2];

                    // verify
                    MessageDigest mdsha = MessageDigest.getInstance("SHA-1");
                    mdsha.update(myKey.publicKey().getExponent().toByteArray());
                    mdsha.update(myKey.publicKey().getModulus().toByteArray());

                    BigInteger m = new BigInteger(1, mdsha.digest());
                    BigInteger p = new BigInteger(certNum, 32);
                    BigInteger certNumber = monCert.getPublicKey().encrypt(p);
                    if (m.compareTo(certNumber) == 0) {
                        System.out.println("got it!");
                    } else {
                        System.out.println("don't got it");
                    }

                }
            }
        }
    }

    // private void createShareSecret(String monHalfStr) {
    // final BigInteger monHalfNum = myKey.decryptNum(new BigInteger(
    // monHalfStr, 32));
    // byte myHalf[] = myKey.publicKey().getModulus().toByteArray();
    // byte monHalf[] = monHalfNum.toByteArray();
    //
    // int keySize = 512;
    // ByteArrayOutputStream bos = new ByteArrayOutputStream(keySize / 8);
    //
    // for (int i = 0; i < keySize / 16; i++) {
    // bos.write(monHalf[i]);
    // bos.write(myHalf[i]);
    // }
    // sharedSecret = new BigInteger(1, bos.toByteArray());
    // }

    public BigInteger createShareKarnSecret(String monHalfStr) {
        final BigInteger monHalfNum = myKey.decryptNum(new BigInteger(
                monHalfStr, 32));
        byte myHalf[] = myKey.publicKey().getModulus().toByteArray();
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

    private void createKarnChannel(final BigInteger sharedSec)
            throws NoSuchAlgorithmException {
        karnIn = new KarnBufferedReader(in, sharedSec);
        karnOut = new KarnPrintWriter(out, true, sharedSec);
    }

    // public void ident(final String id) throws RemoteException,
    // MalformedURLException, NotBoundException {
    // final BigInteger modulus = myKey.publicKey().getModulus();
    // final String myHalf = getMyHalf(modulus);
    // out.println("IDENT " + id + myHalf);
    // }

    public void createMyRsaPrivateKey() throws FileNotFoundException,
            IOException {
        RSA myPrivateKey = new RSA(); /* Default is 512-bit key size */
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                "PlayerKey"));
        oos.writeObject(myPrivateKey);
        oos.close();
    }

    public void makeCert() {
        final String cmd = "MAKE_CERTIFICATE "
                + myKey.publicKey().getExponent().toString(32) + " "
                + myKey.publicKey().getModulus().toString(32);
        if (karnOut != null) {
            karnOut.println(cmd);
        } else {
            out.println(cmd);
        }
    }

    public static void main(String[] args) throws NotBoundException,
            UnknownHostException, NoSuchAlgorithmException, IOException {
        PropertyConfigurator.configure("log4j.properties");

        CertMgr mgr = new CertMgr();
        log.debug(mgr.getMyHalf());
        mgr.startConnection("ct1");
        // mgr.testKarnChannel();
    }

}
