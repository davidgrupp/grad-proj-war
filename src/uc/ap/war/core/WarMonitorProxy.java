package uc.ap.war.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.log4j.Logger;

import uc.ap.war.core.crypto.CertMgrAdapter;
import uc.ap.war.core.crypto.KarnBufferedReader;
import uc.ap.war.core.crypto.KarnPrintWriter;
import uc.ap.war.core.ex.NoPlayerIdException;
import uc.ap.war.core.ex.SecurityServiceException;
import uc.ap.war.core.ex.WarSecurityException;
import uc.ap.war.core.ex.WrongPwChecksumException;
import uc.ap.war.core.model.WarPlayer;
import uc.ap.war.core.protocol.CmdHelper;
import uc.ap.war.core.protocol.ProtoKw;

public class WarMonitorProxy {
    private static final Logger log = Logger.getLogger(WarMonitorProxy.class);
    private WarMonitorProxyLogger pLog;
    private DirectiveHandler hdlr;
    private CertMgrAdapter cAdp;
    private IoChannel io;

    public WarMonitorProxy(final Reader monitorReader,
            final Writer monitorWriter, final DirectiveHandler cmdHandler)
            throws UnknownHostException, IOException {
        this(monitorReader, monitorWriter, cmdHandler, null);
    }

    public WarMonitorProxy(final Reader monitorReader,
            final Writer monitorWriter, final DirectiveHandler cmdHandler,
            final WarMonitorProxyLogger proxyLogger)
            throws UnknownHostException, IOException {
        this.cAdp = CertMgrAdapter.ins();
        this.io = new IoChannel(monitorReader, monitorWriter);
        this.hdlr = cmdHandler;
        if (proxyLogger == null) {
            // if no custom proxy logger is supplied, just log to the class
            // logger
            pLog = new WarMonitorProxyLogger() {
                @Override
                public void log(String msg) {
                    log.info(msg);
                }
            };
        } else {
            pLog = proxyLogger;
        }
    }

    public void cmdAlive() {
        io.issueCmd(CmdHelper.alive(WarPlayer.ins().getCookie()));
    }

    public void cmdCrackCookie(final String targetId, int compAmt) {
        io.issueCmd(CmdHelper.crackCookie(targetId, compAmt));
    }

    public void cmdCrackHostPort(final String targetId) {
        io.issueCmd(CmdHelper.crackHostPort(targetId));
    }

    public void cmdCrackStatus(final String targetId, int compAmt) {
        io.issueCmd(CmdHelper.crackStatus(targetId, compAmt));
    }

    public void cmdDeclareWar(final String targetId, final String host,
            final int port, final int weaponsAmt, final int vehiclesAmt) {
        io.issueCmd(CmdHelper.declearWar(targetId, host, port, weaponsAmt,
                vehiclesAmt));
    }

    public void cmdDefendWar(int resourceAmt) {
        io.issueCmd(CmdHelper.defendWar(resourceAmt, resourceAmt));
    }

    public void cmdGameIdents() {
        io.issueCmd(CmdHelper.gameIdents());
    }

    public void cmdHostPort() {
        io.issueCmd(CmdHelper.hostPort(WarPlayer.ins().getHost(), WarPlayer
                .ins().getPort()));
    }

    public void cmdIdent() throws NoPlayerIdException, SecurityServiceException {
        if (cAdp.karnReady()) {
            cmdIdentWithCrypto();
        } else {
            io.issueCmd(CmdHelper.ident(WarPlayer.ins().getId()));
        }
    }

    public void cmdIdentWithCrypto() throws NoPlayerIdException,
            SecurityServiceException {
        io.issueCmd(CmdHelper.ident(WarPlayer.ins().getId(),
                cAdp.getMyHalfStr()));
    }

    public void cmdMakeCert() throws SecurityServiceException {
        final String myPubExp = cAdp.getMyPublicKeyExpStr();
        final String myPubMod = cAdp.getMyPublicKeyModStr();
        io.issueCmd(CmdHelper.makeCert(myPubExp, myPubMod));
    }

    public void cmdPlayerStatus() {
        io.issueCmd(CmdHelper.playerStatus());
    }

    public void cmdPwd() {
        io.issueCmd(CmdHelper.pwd(WarPlayer.ins().getPw()));
    }

    public void cmdQuit() {
        io.issueCmd(CmdHelper.quit());
    }

    public void cmdRandomPlayerHp() {
        io.issueCmd(CmdHelper.randomPlayerHp());
    }

    public void cmdSignOff() {
        io.issueCmd(ProtoKw.CMD_SIGN_OFF);
    }

    public void cmdSynth(final String resource) {
        io.issueCmd(CmdHelper.synth() + " " + resource);
    }

    public void cmdTradeAccepted() {
        io.issueCmd(CmdHelper.tradeAccepted());
    }

    public void cmdTradeDeclined() {
        io.issueCmd(CmdHelper.tradeDeclined());
    }

    public void cmdTradeReq(String myRes, String myResAmt, String targetId,
            String forRes, String forResAmt) throws NoPlayerIdException {
        io.issueCmd(CmdHelper.tradeReq(WarPlayer.ins().getId(), myRes,
                myResAmt, targetId, forRes, forResAmt));
    }

    public void cmdWarStatus(final String warTargetId) {
        io.issueCmd(CmdHelper.warStatus(warTargetId));
    }

    public void cmdWarTruce(String id, int rupy, int comp, int weap, int vehi,
            int steel, int copper, int oil, int glass, int plastic, int rubber)
            throws NoPlayerIdException {
        io.issueCmd(CmdHelper.warTruce(WarPlayer.ins().getId(), id, rupy, comp,
                weap, vehi, steel, copper, oil, glass, plastic, rubber));
    }

    public void dispatchMonitorDirectives() throws IOException,
            NoPlayerIdException, SecurityServiceException, WarSecurityException {
        for (MsgGroup mg = nextMsgGroup(); mg != null; mg = nextMsgGroup()) {

            switch (mg.getResultArg()) {
            case ProtoKw.CMD_PWD:
                this.hdlr.resultPwd(mg);
                break;
            case ProtoKw.CMD_PSTAT:
                this.hdlr.resultPlayerStatus(mg);
                break;
            case ProtoKw.CMD_QUIT:
                this.hdlr.resultQuit(mg);
                break;
            case ProtoKw.CMD_GAME_IDS:
                this.hdlr.resultGameIds(mg);
                break;
            case ProtoKw.CMD_RANDOM_PLAYER_HP:
                this.hdlr.resultRandomHostPort(mg);
                break;
            case ProtoKw.CMD_CRACK_HP:
                this.hdlr.resultCrackHostPort(mg);
                break;
            case ProtoKw.CMD_CRACK_STATUS:
                this.hdlr.resultCrackStatus(mg);
                break;
            case ProtoKw.CMD_CRACK_COOKIE:
                this.hdlr.resultCrackCookie(mg);
                break;
            case ProtoKw.CMD_MAKE_CERT:
                this.hdlr.resultMakeCert(mg);
            default:
                log.debug("uninterested result argument: " + mg.getResultArg());
            }

            switch (mg.getRequiredCmd()) {
            case ProtoKw.CMD_ID:
                this.hdlr.requireIdent(this);
                break;
            case ProtoKw.CMD_PWD:
                this.hdlr.requirePwd(this);
                break;
            case ProtoKw.CMD_HP:
                this.hdlr.requireHostPort(this);
                break;
            case ProtoKw.CMD_ALIVE:
                this.hdlr.requireAlive(this);
                break;
            case ProtoKw.CMD_QUIT:
                this.hdlr.requireQuit(this);
                break;
            case ProtoKw.CMD_TRADE_RESP:
                this.hdlr.requireTradeResp(this, mg);
                break;
            case ProtoKw.CMD_WAR_DEFEND:
                this.hdlr.requireWarDefend(this, mg);
                break;
            case ProtoKw.CMD_WAR_TRUCE_RESP:
                this.hdlr.requireTruceResp(this, mg);
                break;
            default:
                log.debug("No command required by monitor, free form transaction begins...");
            }
        }
    }

    private MsgGroup nextMsgGroup() throws IOException, WarSecurityException {
        final MsgGroup mg = new MsgGroup();
        String directive = io.readDirective();
        while (true) {
            if (directive == null || directive.equals("")) {
                log.debug("No incoming message, parsing aborted.");
                return null;
            }
            if (mg.addMsg(directive)) {
                log.debug("end of message ground encounted, done building.");
                pLog.log("[" + new Date() + "]\n");
                break;
            }
            if (mg.getResultArg().equals(ProtoKw.CMD_ID)
                    && !mg.getResultStr().equals("")) {
                switchToKarnChannel(mg.getResultStr());
            } else if (mg.getResultArg().equals(ProtoKw.CMD_CERT)) {
                validateMonCert(mg.getResultStr());
            } else if (!mg.getPwCheckSum().equals("")) {
                // pw checksum provided, use it to authenticate monitor
                authenticateMonitor(mg.getPwCheckSum());
            }
            directive = io.readDirective();
        }
        return mg;
    }

    private void authenticateMonitor(final String pwCheckSum)
            throws WarSecurityException {
        try {
            final String myPw = WarPlayer.ins().getPw();
            final MessageDigest mdsha = MessageDigest.getInstance("SHA-1");
            mdsha.update(myPw.toUpperCase().getBytes());
            final String myPwDigest = new BigInteger(1, mdsha.digest())
                    .toString(16);
            if (!myPwDigest.equals(pwCheckSum)) {
                log.warn("unabled to authenticate monitor by password checksum.");
                throw new WrongPwChecksumException();
            } else {
                log.info("monitor authenticated by password checksum.");
            }
        } catch (NoSuchAlgorithmException e) {
            log.error(e);
        }
    }

    private void switchToKarnChannel(final String resultStr) {
        try {
            log.debug("switching to karn channel with monitor half key: "
                    + resultStr);
            this.io.switchToKarn(cAdp.getShareKarnSecret(resultStr));
        } catch (SecurityServiceException e) {
            log.error("karn channel setup attempt failed: " + e);
        }
    }

    private void validateMonCert(final String resultStr) {
        try {
            final String monNum = resultStr.split("\\s+")[1];
            final MessageDigest mdsha = MessageDigest.getInstance("SHA-1");
            mdsha.update(cAdp.getMyPublicKeyExp().toByteArray());
            mdsha.update(cAdp.getMyPublicKeyMod().toByteArray());

            final BigInteger m = new BigInteger(1, mdsha.digest());
            final BigInteger p = new BigInteger(monNum, 32);
            final BigInteger certNumber = cAdp.encryptWithMonPubKey(p);
            if (m.compareTo(certNumber) == 0) {
                // TODO: plug-in certificate code
                log.debug("got it!");
            } else {
                log.debug("don't got it");
            }
        } catch (SecurityServiceException | NoSuchAlgorithmException e) {
            log.error(e);
        }
    }

    private class IoChannel {
        BufferedReader in;
        PrintWriter out;
        String channelName = "";

        IoChannel(final Reader r, final Writer w) {
            in = new BufferedReader(r);
            out = new PrintWriter(w, true);
            channelName = "Plain";
        }

        void issueCmd(final String cmd) {
            out.println(cmd);
            final String channel = "[" + channelName + ":CMD] ";
            log.debug(channel + cmd);
            pLog.log("\n" + channel + cmd + "\n\n");
        }

        String readDirective() throws IOException {
            final String dir = in.readLine();
            final String channel = "[" + channelName + ":DIR] ";
            log.debug(channel + dir);
            pLog.log(channel + dir + "\n");
            return dir;
        }

        void switchToKarn(final BigInteger sharedSecret) {
            try {
                final BufferedReader karnIn = new KarnBufferedReader(in,
                        sharedSecret);
                final PrintWriter karnOut = new KarnPrintWriter(out, true,
                        sharedSecret);
                // don't start changing existing io channels until we've got
                // both the karn in and out channels ready
                in = karnIn;
                out = karnOut;
                channelName = "Karn";
                log.debug("Swithed to karnanObject channel successfully.");
            } catch (NoSuchAlgorithmException e) {
                log.error("failed switching to karn channel");
            }
        }
    }

}