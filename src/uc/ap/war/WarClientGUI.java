package uc.ap.war;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.protocol.BasicDirectiveHandler;
import uc.ap.war.protocol.MsgGroup;
import uc.ap.war.protocol.ProtoKw;
import uc.ap.war.protocol.ProtocolHelper;
import uc.ap.war.protocol.WarInfo;
import uc.ap.war.protocol.WarModelManager;
import uc.ap.war.protocol.WarMonitorProxy;
import uc.ap.war.protocol.WarMonitorProxyLogger;
import uc.ap.war.protocol.WarPlayer;
import uc.ap.war.protocol.exp.PlayerIdException;

@SuppressWarnings("serial")
public class WarClientGUI extends JFrame {
    private static final int FRAME_WIDTH = 1200;
    private static final int CTL_PANE_WIDTH = 380;
    private static Logger log = Logger.getLogger(WarClientGUI.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");

        final WarClientGUI f = new WarClientGUI();
        f.pack();
        f.setVisible(true);

        log.debug("WarClient started.");
    }

    private Socket monitorSock;
    private WarMonitorProxy mon;
    private WarServer svr;
    private Thread svrThread;
    private JTextArea taClientLog;
    private JTextArea taSvrLog;
    private JComboBox<String> cbMonHost;
    private JComboBox<Integer> cbMonPort;
    private JButton btnConn;
    private JButton btnDisconn;
    private JButton btnId;
    private JButton btnPwd;
    private JButton btnHostPort;
    private JTextField tfMyId;
    private JTextField tfMyHost;
    private JTextField tfMyPort;
    private JButton btnSvrUp;
    private JButton btnSvrDown;
    private JButton btnAlive;
    private JButton btnPlayerStatus;
    private JButton btnQuit;
    private JButton btnStorePlayer;
    private JButton btnLoadPlayer;
    private JButton btnSignOff;
    private JComboBox<String> cbCompRes;
    private JButton btnSynth;
    private JButton btnGameIdents;
    private JComboBox<String> cbGameIds;
    private JTextField tfOtherId;
    private JTextField tfOtherHost;
    private JTextField tfOtherPort;
    private JButton btnRandomPlayerHp;
    private JButton btnOtherHp;
    private JButton btnTradeReq;
    private JComboBox<String> cbTradeTargetId;
    private JTextField tfMyCookie;
    private JTextField tfMyRupy;
    private JTextField tfMyComputer;
    private JTextField tfMyWeapon;
    private JTextField tfMyVehicle;
    private JTextField tfMySteel;
    private JTextField tfMyCopper;
    private JTextField tfMyOil;
    private JTextField tfMyGlass;
    private JTextField tfMyPlastic;
    private JTextField tfMyRubber;
    private JComboBox<String> cbTradeMyRes;
    private JComboBox<String> cbTradeForRes;
    private JTextField tfTradeMyResAmt;
    private JTextField tfTradeForResAmt;
    private JButton btnDeclareWar;
    private JButton btnDefendWar;

    public WarClientGUI() {
        super.setPreferredSize(new Dimension(FRAME_WIDTH, 600));
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // a 3-column layout
        super.setLayout(new GridLayout(0, 3));

        // left pane
        super.add(buildLogPane());

        // middle pane
        final JPanel midPane = new JPanel();
        midPane.setLayout(new FlowLayout());
        super.add(midPane);
        midPane.add(buildConfigPane());
        midPane.add(buildRegPane());
        midPane.add(buildResoursePane());
        midPane.add(buildWarPane());

        // right pane
        final JPanel rightPane = new JPanel();
        rightPane.setLayout(new FlowLayout());
        super.add(rightPane);
        rightPane.add(buildPlayerPane());
        rightPane.add(buildOthersPane());

        log.debug("CheckerGameFrame inited...");
    }

    private JPanel buildConfigPane() {
        final JPanel pane = initControlPane("Config", 150);

        final JTextField tfPersistantPlayerId = new JTextField();
        btnLoadPlayer = new JButton("Load Player (Id): ");
        btnLoadPlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    WarModelManager.loadPlayer(tfPersistantPlayerId.getText());
                    final WarPlayer me = WarPlayer.ins();
                    tfMyId.setText(me.getId());
                    tfMyHost.setText(me.getHost());
                    tfMyPort.setText(String.valueOf(me.getPort()));
                    refreshMyResourceInfo();
                } catch (ClassNotFoundException | IOException
                        | PlayerIdException ex) {
                    log.error(ex);
                    taClientLog.append(ex.toString());
                }
            }
        });
        pane.add(btnLoadPlayer);
        pane.add(tfPersistantPlayerId);

        // monitor host
        pane.add(new JLabel("Monitor Host"));
        cbMonHost = new JComboBox<String>();
        WarPlayer.ins().setHost("localhost");
        cbMonHost.addItem(WarPlayer.ins().getHost());
        cbMonHost.addItem("gauss.ececs.uc.edu");
        cbMonHost.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WarPlayer.ins().setHost((String) cbMonHost.getSelectedItem());
            }
        });
        pane.add(cbMonHost);
        // monitor port
        pane.add(new JLabel("Monitor Port"));
        cbMonPort = new JComboBox<Integer>();
        WarPlayer.ins().setPort(8180);
        cbMonPort.addItem(WarPlayer.ins().getPort());
        cbMonPort.addItem(8160);
        pane.add(cbMonPort);

        // connect button
        btnConn = new JButton("Connect");
        btnConn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final ClientDirectiveHandler cmdr = new ClientDirectiveHandler();
                final String h = (String) cbMonHost.getSelectedItem();
                final int p = (Integer) cbMonPort.getSelectedItem();
                try {
                    monitorSock = new Socket(h, p);
                    final Reader r = new InputStreamReader(monitorSock
                            .getInputStream());
                    final Writer w = new OutputStreamWriter(monitorSock
                            .getOutputStream());
                    final WarMonitorProxyLogger l = new WarMonitorProxyLogger() {
                        @Override
                        public void log(String msg) {
                            taClientLog.append(msg);
                        }
                    };
                    mon = new WarMonitorProxy(r, w, cmdr, l);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mon.dispatchMonitorDirectives();
                            } catch (PlayerIdException e) {
                                log.error(e);
                            } catch (IOException e) {
                                log.error(e);
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        });
        pane.add(btnConn);

        // disconnect button
        btnDisconn = new JButton("Disconnect");
        btnDisconn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mon.cmdQuit();
                    monitorSock.close();
                } catch (IOException e1) {
                    log.error(e1);
                }
            }
        });
        pane.add(btnDisconn);

        // button server up
        btnSvrUp = new JButton("Server Up");
        btnSvrUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final WarMonitorProxyLogger pLog = new WarMonitorProxyLogger() {
                    @Override
                    public void log(String msg) {
                        taSvrLog.append(msg);
                    }
                };
                svr = new WarServer(WarPlayer.ins().getPort(),
                        new ServerDirectiveHandler(), pLog);
                svrThread = new Thread(svr);
                svrThread.start();
            }
        });
        pane.add(btnSvrUp);

        // button server down
        btnSvrDown = new JButton("Server Down");
        btnSvrDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                svrThread.interrupt();
            }
        });
        pane.add(btnSvrDown);

        return pane;
    }

    private JPanel buildLogPane() {
        final JPanel pane = new JPanel();
        pane.setLayout(new GridLayout(2, 0));

        taSvrLog = new JTextArea();
        ((DefaultCaret) taSvrLog.getCaret())
                .setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        final JPanel svrLogPane = new JPanel();
        svrLogPane.setBorder(BorderFactory.createTitledBorder("Server Log"));
        svrLogPane.setLayout(new BorderLayout());
        svrLogPane.add(new JScrollPane(taSvrLog), BorderLayout.CENTER);
        pane.add(svrLogPane, BorderLayout.CENTER);

        taClientLog = new JTextArea();
        ((DefaultCaret) taClientLog.getCaret())
                .setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        final JPanel clientLogPane = new JPanel();
        clientLogPane.setBorder(BorderFactory.createTitledBorder("Client Log"));
        clientLogPane.setLayout(new BorderLayout());
        clientLogPane.add(new JScrollPane(taClientLog), BorderLayout.CENTER);
        pane.add(clientLogPane);

        return pane;
    }

    private JPanel buildPlayerPane() {
        final JPanel pane = new JPanel();
        pane.setPreferredSize(new Dimension(CTL_PANE_WIDTH, 300));
        pane.setBorder(BorderFactory.createTitledBorder("This Player"));
        pane.setLayout(new GridLayout(0, 2));

        // player id
        pane.add(new JLabel("Player ID"));
        tfMyId = new JTextField();
        tfMyId.getDocument().addDocumentListener(new TextFieldChangeHandler() {
            @Override
            protected void docChanged(DocumentEvent e) {
                WarPlayer.ins().setId(tfMyId.getText());
            }
        });
        pane.add(tfMyId);

        pane.add(new JLabel("Server Host"));
        tfMyHost = new JTextField();
        tfMyHost.getDocument().addDocumentListener(
                new TextFieldChangeHandler() {
                    @Override
                    protected void docChanged(DocumentEvent e) {
                        WarPlayer.ins().setHost(tfMyHost.getText());
                    }
                });
        pane.add(tfMyHost);

        pane.add(new JLabel("Server Port"));
        tfMyPort = new JTextField();
        tfMyPort.getDocument().addDocumentListener(
                new TextFieldChangeHandler() {
                    @Override
                    protected void docChanged(DocumentEvent e) {
                        try {
                            int port = Integer.parseInt(tfMyPort.getText());
                            WarPlayer.ins().setPort(port);
                        } catch (NumberFormatException e1) {
                            // simply do nothing
                        }
                    }
                });
        pane.add(tfMyPort);

        pane.add(new JLabel("Rupyulars"));
        tfMyRupy = new JTextField();
        tfMyRupy.setEnabled(false);
        pane.add(tfMyRupy);
        pane.add(new JLabel("Computers"));
        tfMyComputer = new JTextField();
        tfMyComputer.setEnabled(false);
        pane.add(tfMyComputer);
        pane.add(new JLabel("Weapons"));
        tfMyWeapon = new JTextField();
        tfMyWeapon.setEnabled(false);
        pane.add(tfMyWeapon);
        pane.add(new JLabel("Vehicles"));
        tfMyVehicle = new JTextField();
        tfMyVehicle.setEnabled(false);
        pane.add(tfMyVehicle);
        pane.add(new JLabel("steel"));
        tfMySteel = new JTextField();
        tfMySteel.setEnabled(false);
        pane.add(tfMySteel);
        pane.add(new JLabel("copper"));
        tfMyCopper = new JTextField();
        tfMyCopper.setEnabled(false);
        pane.add(tfMyCopper);
        pane.add(new JLabel("oil"));
        tfMyOil = new JTextField();
        tfMyOil.setEnabled(false);
        pane.add(tfMyOil);
        pane.add(new JLabel("glass"));
        tfMyGlass = new JTextField();
        tfMyGlass.setEnabled(false);
        pane.add(tfMyGlass);
        pane.add(new JLabel("plastic"));
        tfMyPlastic = new JTextField();
        tfMyPlastic.setEnabled(false);
        pane.add(tfMyPlastic);
        pane.add(new JLabel("rubber"));
        tfMyRubber = new JTextField();
        tfMyRubber.setEnabled(false);
        pane.add(tfMyRubber);

        btnPlayerStatus = new JButton("Player Status");
        btnPlayerStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdPlayerStatus();
            }
        });
        pane.add(btnPlayerStatus);

        btnStorePlayer = new JButton("Save Player");
        btnStorePlayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    WarModelManager.storePlayer(tfMyId.getText());
                } catch (IOException ex) {
                    log.debug(ex);
                    taClientLog.append(ex.toString());
                }
            }
        });
        pane.add(btnStorePlayer);

        return pane;
    }

    private JPanel buildRegPane() {
        final JPanel pane = initControlPane("Registration", 100);

        // button ident
        btnId = new JButton("Ident");
        btnId.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnId.setForeground(new Color(0, 0, 0));
                try {
                    mon.cmdIdent();
                } catch (PlayerIdException ex) {
                    log.error(ex);
                }
            }
        });
        pane.add(btnId);

        // button password
        btnPwd = new JButton("Password");
        btnPwd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnPwd.setForeground(new Color(0, 0, 0));
                mon.cmdPwd();
            }
        });
        pane.add(btnPwd);

        // button alive
        btnAlive = new JButton("Alive");
        btnAlive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnAlive.setForeground(new Color(0, 0, 0));
                mon.cmdAlive();
            }
        });
        pane.add(btnAlive);

        // button host port
        btnHostPort = new JButton("Host Port");
        btnHostPort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                btnHostPort.setForeground(new Color(0, 0, 0));
                mon.cmdHostPort();
            }
        });
        pane.add(btnHostPort);

        // button quit
        btnQuit = new JButton("Quit");
        btnQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdQuit();
            }
        });
        pane.add(btnQuit);

        btnSignOff = new JButton("Sign Off");
        btnSignOff.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mon.cmdSignOff();
            }
        });
        pane.add(btnSignOff);

        return pane;
    }

    private JPanel buildWarPane() {
        final JPanel pane = initControlPane("War", 50);

        btnDeclareWar = new JButton("Declare War");
        final JFrame parentFrame = this;
        btnDeclareWar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

            }
        });
        pane.add(btnDeclareWar);

        btnDefendWar = new JButton("Defend War");
        btnDefendWar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JOptionPane optionPane = new JOptionPane(
                        "XXX declared war!\n"
                                + "how many resources used for defending?\n",
                        JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

                final JDialog dialog = new JDialog(parentFrame, "War Declared",
                        false);
                dialog.setContentPane(optionPane);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent we) {
                        log.debug("Not allowed to close dialog directly...");
                    }
                });
                optionPane
                        .addPropertyChangeListener(new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent e) {
                                String prop = e.getPropertyName();
                                if (dialog.isVisible()
                                        && (e.getSource() == optionPane)
                                        && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                                    // If you were going to check something
                                    // before closing the window, you'd do
                                    // it here.
                                    dialog.setVisible(false);
                                    log.debug("option pane property changed");
                                }
                            }
                        });
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        pane.add(btnDefendWar);

        return pane;
    }

    private JPanel buildResoursePane() {
        final JPanel pane = initControlPane("Resource", 100);

        btnSynth = new JButton("Synthesize:");
        btnSynth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mon.cmdSynth((String) cbCompRes.getSelectedItem());
            }
        });
        pane.add(btnSynth);

        cbCompRes = new JComboBox<String>(new DefaultComboBoxModel<String>(
                ProtoKw.COMP_RES_NAMES));
        pane.add(cbCompRes);

        btnTradeReq = new JButton("Trade Request: ");
        btnTradeReq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String targetId = (String) cbTradeTargetId
                        .getSelectedItem();
                final String myRes = (String) cbTradeMyRes.getSelectedItem();
                final String myResAmt = tfTradeMyResAmt.getText();
                final String forRes = (String) cbTradeForRes.getSelectedItem();
                final String forResAmt = tfTradeForResAmt.getText();
                try {
                    mon.cmdTradeReq(myRes, myResAmt, targetId, forRes,
                            forResAmt);
                } catch (PlayerIdException ex) {
                    log.error(ex);
                }
            }
        });
        pane.add(btnTradeReq);
        cbTradeTargetId = new JComboBox<String>();
        pane.add(cbTradeTargetId);

        cbTradeMyRes = new JComboBox<String>(new DefaultComboBoxModel<String>(
                ProtoKw.RES_NAMES));
        pane.add(cbTradeMyRes);
        tfTradeMyResAmt = new JTextField();
        pane.add(tfTradeMyResAmt);

        cbTradeForRes = new JComboBox<String>(new DefaultComboBoxModel<String>(
                ProtoKw.RES_NAMES));
        pane.add(cbTradeForRes);
        tfTradeForResAmt = new JTextField();
        pane.add(tfTradeForResAmt);

        return pane;
    }

    private JPanel buildOthersPane() {
        final JPanel pane = initControlPane("Other Players", 150);

        btnGameIdents = new JButton("Get Game Idents:");
        btnGameIdents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdGameIdents();
            }
        });
        pane.add(btnGameIdents);

        cbGameIds = new JComboBox<String>();
        cbGameIds.setModel(new DefaultComboBoxModel<String>(WarInfo.ins()
                .getOtherPlayerIds()));
        pane.add(cbGameIds);

        btnRandomPlayerHp = new JButton("Random Player Host/Port");
        btnRandomPlayerHp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdRandomPlayerHp();
            }
        });
        pane.add(btnRandomPlayerHp);

        btnOtherHp = new JButton("Player Host Port");
        btnOtherHp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdPlayerHp((String) cbGameIds.getSelectedItem());
            }
        });
        pane.add(btnOtherHp);

        pane.add(new JLabel("Other Player Id"));
        tfOtherId = new JTextField();
        pane.add(tfOtherId);
        pane.add(new JLabel("Other Player Host"));
        tfOtherHost = new JTextField();
        pane.add(tfOtherHost);
        pane.add(new JLabel("Other Player Port"));
        tfOtherPort = new JTextField();
        pane.add(tfOtherPort);

        return pane;
    }

    private JPanel initControlPane(final String title, final int height) {
        final JPanel pane = new JPanel();
        pane.setPreferredSize(new Dimension(CTL_PANE_WIDTH, height));
        pane.setBorder(BorderFactory.createTitledBorder(title));
        pane.setLayout(new GridLayout(0, 2));
        return pane;
    }

    private void refreshMyResourceInfo() {
        final WarPlayer me = WarPlayer.ins();
        tfMyRupy.setText(String.valueOf(me.getRupyulars()));
        tfMyComputer.setText(String.valueOf(me.getComputers()));
        tfMyWeapon.setText(String.valueOf(me.getWeapons()));
        tfMyVehicle.setText(String.valueOf(me.getVehicles()));
        tfMySteel.setText(String.valueOf(me.getSteel()));
        tfMyCopper.setText(String.valueOf(me.getCopper()));
        tfMyOil.setText(String.valueOf(me.getOil()));
        tfMyGlass.setText(String.valueOf(me.getGlass()));
        tfMyPlastic.setText(String.valueOf(me.getPlastic()));
        tfMyRubber.setText(String.valueOf(me.getRubber()));
    }

    private boolean showTradeRequestDialog() {
        final String[] options = { "Accept", "Decline" };
        final JFrame parentFrame = this;
        final int optIndex = JOptionPane.showOptionDialog(parentFrame,
                "Trade request from ...", "Trade Request",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (optIndex == 0) {
            log.debug("Trade request accepted...");
            return true;
            // mon.cmdTradeAccepted();
        } else {
            log.debug("Trade request declined");
            return false;
            // mon.cmdTradeDeclined();
        }
    }

    class ServerDirectiveHandler extends BasicDirectiveHandler {
        @Override
        public void requireTradeResp(final WarMonitorProxy mon,
                final MsgGroup mg) {
            boolean tradeAccepted = showTradeRequestDialog();
            if (tradeAccepted) {
                mon.cmdTradeAccepted();
            } else {
                mon.cmdTradeDeclined();
            }
        }
    }

    class ClientDirectiveHandler extends BasicDirectiveHandler {

        @Override
        public void requireAlive(final WarMonitorProxy mon) {
            btnAlive.setForeground(new Color(255, 0, 0));
        }

        @Override
        public void requireHostPort(final WarMonitorProxy mon) {
            btnHostPort.setForeground(new Color(255, 0, 0));
        }

        @Override
        public void requireIdent(final WarMonitorProxy mon)
                throws PlayerIdException {
            btnId.setForeground(new Color(255, 0, 0));
        }

        @Override
        public void requirePwd(final WarMonitorProxy mon) {
            btnPwd.setForeground(new Color(255, 0, 0));
        }

        @Override
        public void resultPlayerStatus(final MsgGroup mg) {
            super.resultPlayerStatus(mg);
            refreshMyResourceInfo();
        }

        @Override
        public void resultGameIds(final MsgGroup mg) {
            final String[] gameIds = ProtocolHelper.parseGameIds(mg);
            log.debug("Got game ids: " + gameIds);
            cbGameIds.setModel(new DefaultComboBoxModel<String>(gameIds));
            cbTradeTargetId.setModel(new DefaultComboBoxModel<String>(gameIds));
            cbTradeTargetId.addItem("MONITOR");
        }

        @Override
        public void resultRandomPlayerHp(final MsgGroup mg) {
            final String[] playerHp = ProtocolHelper.parseRandomPlayerHp(mg);
            if (playerHp != null && playerHp.length == 3) {
                tfOtherId.setText(playerHp[0]);
                tfOtherHost.setText(playerHp[1]);
                tfOtherPort.setText(playerHp[2]);
            }
        }

        @Override
        public void resultPlayerHp(final MsgGroup mg) {
            final String[] playerHp = ProtocolHelper.parseRandomPlayerHp(mg);
            if (playerHp != null && playerHp.length == 3) {
                tfOtherId.setText(playerHp[0]);
                tfOtherHost.setText(playerHp[1]);
                tfOtherPort.setText(playerHp[2]);
            }
        }

        @Override
        public void requireTradeResp(final WarMonitorProxy mon,
                final MsgGroup mg) {
            log.debug("got trade request...");
        }

        @Override
        public void requestWar(final WarMonitorProxy mon, final MsgGroup mg) {
            // TODO Auto-generated method stub

        }
    }
}
