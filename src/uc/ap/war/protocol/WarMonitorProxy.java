package uc.ap.war.protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.log4j.Logger;

import uc.ap.war.protocol.exp.PlayerIdException;

public class WarMonitorProxy {
    static Logger log = Logger.getLogger(WarMonitorProxy.class);
    private WarMonitorProxyLogger pLog;
    private MsgGroupParser mgp;
    private PrintWriter out;
    private DirectiveHandler hdlr;
    private MsgGroup lastMsgGroup;

    public WarMonitorProxy(final Reader monitorReader,
            final Writer monitorWriter, final DirectiveHandler cmdHandler)
            throws UnknownHostException, IOException {
        this(monitorReader, monitorWriter, cmdHandler, null);
    }

    public WarMonitorProxy(final Reader monitorReader,
            final Writer monitorWriter, final DirectiveHandler cmdHandler,
            final WarMonitorProxyLogger proxyLogger)
            throws UnknownHostException, IOException {
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

    public void cmdHostPort() {
        issueCmd(CmdHelper.hostPort(WarPlayer.ins().getHost(), WarPlayer.ins()
                .getPort()));
    }

    public void cmdIdent() throws PlayerIdException {
        issueCmd(CmdHelper.ident(WarPlayer.ins().getId()));
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

    public void cmdSignOff() {
        issueCmd(ProtoKw.CMD_SIGN_OFF);
    }

    public void cmdSynth(final String resource) {
        issueCmd(CmdHelper.synth() + " " + resource);
    }

    public void dispatchMonitorDirectives() throws IOException,
            PlayerIdException {
        for (MsgGroup mg = mgp.next(); mg != null; mg = mgp.next()) {
            lastMsgGroup = mg;
            pLog.log("[" + new Date() + "]\n");
            pLog.log(mg.toString() + "\n");
            final String resArg = mg.getResultArg();
            if (resArg.startsWith(ProtoKw.CMD_PWD)) {
                this.hdlr.resultPwd(mg);
            } else if (resArg.startsWith(ProtoKw.CMD_PSTAT)) {
                this.hdlr.resultPlayerStatus(mg);
            } else if (resArg.startsWith(ProtoKw.CMD_QUIT)) {
                this.hdlr.resultQuit(mg);
            } else if (resArg.startsWith(ProtoKw.CMD_GAME_IDS)) {
                this.hdlr.resultGameIds(mg);
            } else if (resArg.startsWith(ProtoKw.CMD_RANDOM_PLAYER_HP)) {
                this.hdlr.resultRandomPlayerHp(mg);
            } else if (resArg.startsWith(ProtoKw.CMD_PLAYER_HP)) {
                this.hdlr.resultPlayerHp(mg);
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
            default:
                log.debug("No command required by monitor, free form transaction begins...");
            }
        }
    }

    public MsgGroup getLastMsgGroup() {
        return this.lastMsgGroup;
    }

    private void issueCmd(final String cmd) {
        this.out.println(cmd);
        pLog.log(cmd + "\n\n");
    }

    public void cmdGameIdents() {
        issueCmd(CmdHelper.gameIdents());
    }

    public void cmdRandomPlayerHp() {
        issueCmd(CmdHelper.randomPlayerHp());
    }

    public void cmdPlayerHp(String playerId) {
        issueCmd(CmdHelper.playerHp(playerId));
    }

    public void cmdTradeReq(String myRes, String myResAmt, String targetId,
            String forRes, String forResAmt) throws PlayerIdException {
        issueCmd(CmdHelper.tradeReq(WarPlayer.INS.getId(), myRes, myResAmt,
                targetId, forRes, forResAmt));
    }

    public void cmdTradeAccepted() {
        issueCmd(CmdHelper.tradeAccepted());
    }

    public void cmdTradeDeclined() {
        issueCmd(CmdHelper.tradeDeclined());
    }
}