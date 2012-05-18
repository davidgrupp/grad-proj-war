package uc.ap.war;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.protocol.CmdHelper;
import uc.ap.war.protocol.MsgGroupParser;
import uc.ap.war.protocol.DirectiveHandler;
import uc.ap.war.protocol.WarMonitorProxy;
import uc.ap.war.protocol.exp.NoCommandHandlerException;
import uc.ap.war.protocol.exp.PlayerIdException;

public class WarServer implements Runnable {
	static Logger log = Logger.getLogger(WarServer.class);
	private ServerSocket sock;
	private int port;

	public WarServer(final int port) {
		this.port = port;
		try {
			sock = new ServerSocket(this.port);
		} catch (IOException e) {
			log.error(e);
			System.exit(1);
		}
	}

	@Override
	public void run() {
		log.info("war server(passive client) started");
		while (true) {
			try {
				final Socket sock = this.sock.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							final Reader r = new InputStreamReader(
									sock.getInputStream());
							final Writer w = new OutputStreamWriter(
									sock.getOutputStream());
							AutoDirHandler cmdr = new AutoDirHandler();
							WarMonitorProxy mon = new WarMonitorProxy(r, w,
									cmdr);
							cmdr.setWarMonitoProxy(mon);
							mon.dispatchMonitorDirectives();
						} catch (IOException e) {
							log.error(e);
						} catch (PlayerIdException e) {
							log.error(e);
						}
					}
				}).start();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

	public static void main(String args[]) {
		PropertyConfigurator.configure("log4j.properties");
		new Thread(new WarServer(20000)).start();
	}
}

class AutoDirHandler implements DirectiveHandler {
	static Logger log = Logger.getLogger(AutoDirHandler.class);
	private WarMonitorProxy mon;

	public void setWarMonitoProxy(final WarMonitorProxy monitorProxy) {
		this.mon = monitorProxy;
	}

	@Override
	public void requireIdent() throws PlayerIdException {
		this.mon.cmdIdent();
	}

	@Override
	public void requirePwd() {
		this.mon.cmdPwd();
	}

	@Override
	public void requireHostPort() {
		this.mon.cmdHostPort();
	}

	@Override
	public void requireAlive() {
		this.mon.cmdAlive();
	}

	@Override
	public void resultPwd() {
		log.debug("result: " + mon.getLastMsgGroup().getResultArg());
	}

	@Override
	public void requireQuit() {
		this.mon.cmdQuit();
	}

}

// class MessageGroupWorker {
// static Logger log = Logger.getLogger(MessageGroupWorker.class);
// private MessageGroupParser mgp;
// private PrintWriter out;
//
// public MessageGroupWorker(final Socket inSock) throws IOException {
// this.mgp = new MessageGroupParser(new InputStreamReader(
// inSock.getInputStream()));
// this.out = new PrintWriter(inSock.getOutputStream(), true);
// }
//
// public void go() {
// log.info("worker starting...");
// try {
// while (this.mgp.parsePendingDirectives()) {
// log.debug("required cmd: " + mgp.getRequiredCmd());
// if (mgp.getRequiredCmd().equals("IDENT")) {
// this.out.println(CmdHelper.ident("rsun"));
// }
// if (mgp.getRequiredCmd().equals("ALIVE")) {
// log.debug("requiring ALIVE");
// }
// }
// log.info("worker ending...");
// } catch (IOException e) {
// log.error(e);
// }
// }
//
// }
