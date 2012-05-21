package uc.ap.war.protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.log4j.Logger;

import uc.ap.war.crypto.CertMgrAdapter;
import uc.ap.war.protocol.exp.PlayerIdException;

public class WarMonitorProxy {
    static Logger log = Logger.getLogger(WarMonitorProxy.class);
    private WarMonitorProxyLogger pLog;
    private MsgGroupParser mgp;
    private PrintWriter out;
    private DirectiveHandler hdlr;
    private MsgGroup lastMsgGroup;
    private CertMgrAdapter cAdp;

    public WarMonitorProxy(final Reader monitorReader,
            final Writer monitorWriter, final DirectiveHandler cmdHandler)
            throws UnknownHostException, IOException {
        this(monitorReader, monitorWriter, cmdHandler, null);
    }

    public WarMonitorProxy(final Reader monitorReader,
            final Writer monitorWriter, final DirectiveHandler cmdHandler,
            final WarMonitorProxyLogger proxyLogger)
            throws UnknownHostException, IOException {
        try {
            this.cAdp = new CertMgrAdapter();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            log.error(e);
        }
        this.mgp = new MsgGroupParser(monitorReader);
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

    public void cmdIdent() throws PlayerIdException {
        issueCmd(CmdHelper.ident(WarPlayer.ins().getId()));
    }
    
    public void cmdIdentWithCrypto() throws PlayerIdException,
            ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
        issueCmd(CmdHelper.ident(WarPlayer.INS.getId(), cAdp.getMyHalf()));
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
            String forRes, String forResAmt) throws PlayerIdException {
        issueCmd(CmdHelper.tradeReq(WarPlayer.INS.getId(), myRes, myResAmt,
                targetId, forRes, forResAmt));
    }

    public void cmdWarStatus(final String warTargetId) {
        issueCmd(CmdHelper.warStatus(warTargetId));
    }

    public void cmdWarTruce(String id, int rupy, int comp, int weap, int vehi,
            int steel, int copper, int oil, int glass, int plastic, int rubber)
            throws PlayerIdException {
        issueCmd(CmdHelper.warTruce(WarPlayer.INS.getId(), id, rupy, comp,
                weap, vehi, steel, copper, oil, glass, plastic, rubber));
    }

    public void cmdMakeCert() {
        final String myPubExp = cAdp.getMyPublicKeyExp();
        final String myPubMod = cAdp.getMyPublicKeyMod();
        issueCmd(CmdHelper.makeCert(myPubExp, myPubMod));
    }

    public void dispatchMonitorDirectives() throws IOException,
            PlayerIdException {
        for (MsgGroup mg = mgp.next(); mg != null; mg = mgp.next()) {
            lastMsgGroup = mg;
            pLog.log("[" + new Date() + "]\n");
            pLog.log(mg.toString() + "\n");

            switch (mg.getResultArg()) {
            case ProtoKw.CMD_ID:
                setupKarnChannel(mg.getResultStr());
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

    private boolean setupKarnChannel(String resultStr) {
        //TODO
        return false;
    }

    public MsgGroup getLastMsgGroup() {
        return this.lastMsgGroup;
    }

    private void issueCmd(final String cmd) {
        this.out.println(cmd);
        pLog.log(cmd + "\n\n");
    }
}