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
import uc.ap.war.core.model.WarPlayer;
import uc.ap.war.core.protocol.CmdHelper;
import uc.ap.war.core.protocol.ProtoKw;

public class WarMonitorProxy {
    private static final Logger log = Logger.getLogger(WarMonitorProxy.class);
    private WarMonitorProxyLogger pLog;
    private BufferedReader in;
    private PrintWriter out;
    private DirectiveHandler hdlr;
    private MsgGroup lastMsgGroup;
    private CertMgrAdapter cAdp;
    private KarnBufferedReader karnIn;
    private KarnPrintWriter karnOut;

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
        this.in = new BufferedReader(monitorReader);
        this.out = new PrintWriter(monitorWriter, true);
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
        issueCmd(CmdHelper.alive(WarPlayer.ins().getCookie()));
    }

    public void cmdCrackCookie(final String targetId, int compAmt) {
        issueCmd(CmdHelper.crackCookie(targetId, compAmt));
    }

    public void cmdCrackHostPort(final String targetId) {
        issueCmd(CmdHelper.crackHostPort(targetId));
    }

    public void cmdCrackStatus(final String targetId, int compAmt) {
        issueCmd(CmdHelper.crackStatus(targetId, compAmt));
    }

    public void cmdDeclareWar(final String targetId, final String host,
            final int port, final int weaponsAmt, final int vehiclesAmt) {
        issueCmd(CmdHelper.declearWar(targetId, host, port, weaponsAmt,
                vehiclesAmt));
    }

    public void cmdDefendWar(int resourceAmt) {
        issueCmd(CmdHelper.defendWar(resourceAmt, resourceAmt));
    }

    public void cmdGameIdents() {
        issueCmd(CmdHelper.gameIdents());
    }

    public void cmdHostPort() {
        issueCmd(CmdHelper.hostPort(WarPlayer.ins().getHost(), WarPlayer.ins()
                .getPort()));
    }

    public void cmdIdent() throws NoPlayerIdException, SecurityServiceException {
        if (cAdp.karnReady()) {
            cmdIdentWithCrypto();
        } else {
            issueCmd(CmdHelper.ident(WarPlayer.ins().getId()));
        }
    }

    public void cmdIdentWithCrypto() throws NoPlayerIdException,
            SecurityServiceException {
        issueCmd(CmdHelper.ident(WarPlayer.ins().getId(), cAdp.getMyHalfStr()));
    }

    public void cmdMakeCert() throws SecurityServiceException {
        final String myPubExp = cAdp.getMyPublicKeyExpStr();
        final String myPubMod = cAdp.getMyPublicKeyModStr();
        issueCmd(CmdHelper.makeCert(myPubExp, myPubMod));
    }

    public void cmdPlayerStatus() {
        issueCmd(CmdHelper.playerStatus());
    }

    public void cmdPwd() {
        issueCmd(CmdHelper.pwd(WarPlayer.ins().getPw()));
    }

    public void cmdQuit() {
        issueCmd(CmdHelper.quit());
    }

    public void cmdRandomPlayerHp() {
        issueCmd(CmdHelper.randomPlayerHp());
    }

    public void cmdSignOff() {
        issueCmd(ProtoKw.CMD_SIGN_OFF);
    }

    public void cmdSynth(final String resource) {
        issueCmd(CmdHelper.synth() + " " + resource);
    }

    public void cmdTradeAccepted() {
        issueCmd(CmdHelper.tradeAccepted());
    }

    public void cmdTradeDeclined() {
        issueCmd(CmdHelper.tradeDeclined());
    }

    public void cmdTradeReq(String myRes, String myResAmt, String targetId,
            String forRes, String forResAmt) throws NoPlayerIdException {
        issueCmd(CmdHelper.tradeReq(WarPlayer.ins().getId(), myRes, myResAmt,
                targetId, forRes, forResAmt));
    }

    public void cmdWarStatus(final String warTargetId) {
        issueCmd(CmdHelper.warStatus(warTargetId));
    }

    public void cmdWarTruce(String id, int rupy, int comp, int weap, int vehi,
            int steel, int copper, int oil, int glass, int plastic, int rubber)
            throws NoPlayerIdException {
        issueCmd(CmdHelper.warTruce(WarPlayer.ins().getId(), id, rupy, comp,
                weap, vehi, steel, copper, oil, glass, plastic, rubber));
    }

    public void dispatchMonitorDirectives() throws IOException,
            NoPlayerIdException, SecurityServiceException {
        for (MsgGroup mg = nextMsgGroup(); mg != null; mg = nextMsgGroup()) {
            // lastMsgGroup = mg;
            pLog.log("[" + new Date() + "]\n");
            pLog.log(mg.toString() + "\n");

            switch (mg.getResultArg()) {
            case ProtoKw.CMD_CERT:
                validateMonCert(mg);
                break;
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
                log.debug("unexpected result argument: " + mg.getResultArg());
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

    public MsgGroup getLastMsgGroup() {
        return this.lastMsgGroup;
    }

    private void issueCmd(final String cmd) {
        if (karnOut != null) {
            karnOut.println(cmd);
        } else {
            out.println(cmd);
        }
        pLog.log(cmd + "\n\n");
    }

    private MsgGroup nextMsgGroup() throws IOException {
        final MsgGroup mg = new MsgGroup();
        String directive = readDirective();
        while (true) {
            if (directive == null || directive.equals("")) {
                log.debug("No incoming message, parsing aborted.");
                return null;
            }
            log.debug("DIRECTIVE: " + directive);
            if (!mg.addMsg(directive)) {
                break;
            }
            if (mg.getResultArg().equals(ProtoKw.CMD_ID)
                    && !mg.getResultStr().equals("")) {
                setupKarnChannel(mg.getResultStr());
            }
            directive = readDirective();
        }
        lastMsgGroup = mg;
        return mg;
    }

    private String readDirective() throws IOException {
        if (karnIn != null) {
            log.debug("reading from karn channel");
            return karnIn.readLine();
        } else {
            log.debug("reading from unencrypted channel");
            return in.readLine();
        }
    }

    private void validateMonCert(final MsgGroup mg) {
        try {
            final String monNum = mg.getResultStr().split("\\s+")[1];
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

    private void setupKarnChannel(final String resultStr) {
        try {
            if (karnIn == null && karnOut == null) {
                log.debug("Gonna setup karn channel with monitor half key: "
                        + resultStr);
                final BigInteger sharedSecret = cAdp
                        .getShareKarnSecret(resultStr);
                karnIn = new KarnBufferedReader(in, sharedSecret);
                karnOut = new KarnPrintWriter(out, true, sharedSecret);
                log.debug("karn channel setup successfully.");
            }
        } catch (NoSuchAlgorithmException | SecurityServiceException e) {
            log.error(e);
            log.debug("karn channel setup failed.");
        }
    }
}