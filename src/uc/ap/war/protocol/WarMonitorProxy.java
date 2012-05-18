package uc.ap.war.protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import uc.ap.war.WarPlayer;
import uc.ap.war.protocol.exp.PlayerIdException;

public class WarMonitorProxy {
	static Logger log = Logger.getLogger(WarMonitorProxy.class);
	private MsgGroupParser mgp;
	private PrintWriter out;
	private DirectiveHandler hdlr;
	private MsgGroup lastMsgGroup;

	public WarMonitorProxy(final Reader monitorReader,
			final Writer monitorWriter, final DirectiveHandler cmdHandler)
			throws UnknownHostException, IOException {
		this.mgp = new MsgGroupParser(monitorReader);
		this.out = new PrintWriter(monitorWriter, true);
		this.hdlr = cmdHandler;
	}

	public void dispatchMonitorDirectives() throws IOException,
			PlayerIdException {
		for (MsgGroup mg = mgp.next(); mg != null; mg = mgp.next()) {
			lastMsgGroup = mg;
			final String resArg = mg.getResultArg();
			if (resArg.startsWith(CmdHelper.CMD_PWD)) {
				this.hdlr.resultPwd();
			}
			switch (mg.getRequiredCmd()) {
			case CmdHelper.CMD_ID:
				this.hdlr.requireIdent();
				break;
			case CmdHelper.CMD_PWD:
				this.hdlr.requirePwd();
				break;
			case CmdHelper.CMD_HP:
				this.hdlr.requireHostPort();
				break;
			case CmdHelper.CMD_ALIVE:
				this.hdlr.requireAlive();
				break;
			case CmdHelper.CMD_QUIT:
				this.hdlr.requireQuit();
				break;
			default:
				log.debug("No command required by monitor, free form transaction begins...");
			}
		}
	}

	public MsgGroup getLastMsgGroup() {
		return this.lastMsgGroup;
	}

	public void cmdIdent() throws PlayerIdException {
		this.out.println(CmdHelper.ident(WarPlayer.getId()));
	}

	public void cmdPwd() {
		this.out.println(CmdHelper.pwd(WarPlayer.getPw()));
	}

	public void cmdAlive() {
		this.out.println(CmdHelper.alive(WarPlayer.getCookie()));
	}

	public void cmdHostPort() {
		this.out.println(CmdHelper.hostPort(WarPlayer.getHost(),
				WarPlayer.getPort()));
	}

	public void cmdQuit() {
		this.out.println(CmdHelper.quit());
	}
}