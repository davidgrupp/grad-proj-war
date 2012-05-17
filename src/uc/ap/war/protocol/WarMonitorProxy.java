package uc.ap.war.protocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import uc.ap.war.WarPlayer;
import uc.ap.war.protocol.exp.NoCommandHandlerException;
import uc.ap.war.protocol.exp.PlayerIdException;

public class WarMonitorProxy {
	static Logger log = Logger.getLogger(WarMonitorProxy.class);
	private MsgGroupParser mgp;
	private PrintWriter out;
	private RequiredCommandHandler cmdr;
	private MsgGroup lastMsgGroup;

	public WarMonitorProxy(final Reader monitorReader,
			final Writer monitorWriter, final RequiredCommandHandler cmdHandler)
			throws UnknownHostException, IOException {
		this.mgp = new MsgGroupParser(monitorReader);
		this.out = new PrintWriter(monitorWriter, true);
		this.cmdr = cmdHandler;
	}

	// public void setCmdHandler(final RequiredCommandHandler cmdHandle) {
	// this.cmdr = cmdHandle;
	// }

	public void dispatchMonitorDirectives() throws IOException,
			PlayerIdException {
		// if (this.cmdr == null) {
		// throw new NoCommandHandlerException();
		// }
		for (MsgGroup mg = mgp.next(); mg != null; mg = mgp.next()) {
			lastMsgGroup = mg;
			switch (mg.getRequiredCmd()) {
			case CmdHelper.CMD_ID:
				this.cmdr.ident();
				break;
			case CmdHelper.CMD_PWD:
				this.cmdr.pwd();
				break;
			case CmdHelper.CMD_HP:
				this.cmdr.hostPort();
				break;
			case CmdHelper.CMD_ALIVE:
				this.cmdr.alive();
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
}