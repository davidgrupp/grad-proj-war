package uc.ap.war;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import uc.ap.war.protocol.DirectiveHandler;
import uc.ap.war.protocol.WarMonitorProxy;
import uc.ap.war.protocol.exp.PlayerIdException;

public class WarServer implements Runnable {
	static Logger log = Logger.getLogger(WarServer.class);
	private volatile boolean threadStoped;
	private ServerSocket svrSock;
	private int port;

	public WarServer(final int port) {
		this.port = port;
		this.threadStoped = false;
		try {
			svrSock = new ServerSocket(this.port);
		} catch (IOException e) {
			log.error(e);
			System.exit(1);
		}
	}

	public void stopSvr() {
		this.threadStoped = true;
	}

	@Override
	public void run() {
		log.info("war server(passive client) started");
		while (!this.threadStoped) {
			try {
				final Socket inSock = this.svrSock.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							final Reader r = new InputStreamReader(
									inSock.getInputStream());
							final Writer w = new OutputStreamWriter(
									inSock.getOutputStream());
							AutoDirHandler cmdr = new AutoDirHandler();
							WarMonitorProxy mon = new WarMonitorProxy(r, w,
									cmdr);
							cmdr.setWarMonitoProxy(mon);
							mon.dispatchMonitorDirectives();
						} catch (IOException | PlayerIdException e) {
							log.error(e);
						}
					}
				}).start();
			} catch (IOException e) {
				log.error(e);
			}
		}
		// try {
		// this.svrSock.close();
		// log.info("war server(passive client) stopped.");
		// } catch (IOException e) {
		// log.error(e);
		// }
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
