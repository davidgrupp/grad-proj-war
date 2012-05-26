package uc.ap.war;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import uc.ap.war.core.DirectiveHandler;
import uc.ap.war.core.WarMonitorProxy;
import uc.ap.war.core.WarMonitorProxyLogger;
import uc.ap.war.core.ex.NoPlayerIdException;
import uc.ap.war.core.ex.SecurityServiceException;

public class PassiveClient implements Runnable {
    private static final Logger log = Logger.getLogger(PassiveClient.class);
    private volatile boolean threadStoped;
    private ServerSocket svrSock;
    private int port;
    private WarMonitorProxyLogger pLog;
    private DirectiveHandler hdlr;

    public PassiveClient(final int port, final DirectiveHandler hdlr) {
        this(port, hdlr, null);
    }

    public PassiveClient(final int port, final DirectiveHandler hdlr,
            final WarMonitorProxyLogger proxyLogger) {
        this.port = port;
        this.hdlr = hdlr;
        this.pLog = proxyLogger;
        this.threadStoped = false;
        try {
            svrSock = new ServerSocket(this.port);
        } catch (IOException e) {
            log.error(e);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        final String msg = "war server(passive client) started";
        pLog.log(msg);
        log.info(msg);
        while (!this.threadStoped) {
            try {
                final Socket inSock = this.svrSock.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Reader r = new InputStreamReader(inSock
                                    .getInputStream());
                            final Writer w = new OutputStreamWriter(inSock
                                    .getOutputStream());
                            new WarMonitorProxy(r, w, hdlr, pLog)
                                    .dispatchMonitorDirectives();
                        } catch (IOException | NoPlayerIdException
                                | SecurityServiceException e) {
                            log.error(e);
                        }
                    }
                }).start();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    public void stopSvr() {
        this.threadStoped = true;
    }
}
