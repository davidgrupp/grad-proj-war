package uc.ap.war;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import uc.ap.war.protocol.BasicDirectiveHandler;
import uc.ap.war.protocol.DirectiveHandler;
import uc.ap.war.protocol.WarMonitorProxy;
import uc.ap.war.protocol.WarMonitorProxyLogger;
import uc.ap.war.protocol.exp.PlayerIdException;

public class WarServer implements Runnable {
    static Logger log = Logger.getLogger(WarServer.class);
    private volatile boolean threadStoped;
    private ServerSocket svrSock;
    private int port;
    private WarMonitorProxyLogger pLog;
    private DirectiveHandler hdlr;

    public WarServer(final int port, final DirectiveHandler hdlr) {
        this(port, hdlr, null);
    }

    public WarServer(final int port, final DirectiveHandler hdlr,
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
                            final Reader r = new InputStreamReader(inSock
                                    .getInputStream());
                            final Writer w = new OutputStreamWriter(inSock
                                    .getOutputStream());
                            new WarMonitorProxy(r, w, hdlr, pLog)
                                    .dispatchMonitorDirectives();
                        } catch (IOException | PlayerIdException e) {
                            log.error(e);
                        }
                    }
                }).start();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
