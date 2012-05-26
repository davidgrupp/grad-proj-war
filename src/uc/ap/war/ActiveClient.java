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
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.DefaultCaret;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;

import uc.ap.war.core.BasicDirectiveHandler;
import uc.ap.war.core.WarMonitorProxy;
import uc.ap.war.core.WarMonitorProxyLogger;
import uc.ap.war.core.crypto.CertMgrAdapter;
import uc.ap.war.core.ex.PlayerIdException;
import uc.ap.war.core.ex.SecurityServiceException;
import uc.ap.war.core.ex.SecurityServiceNotReadyException;
import uc.ap.war.core.model.WarInfo;
import uc.ap.war.core.model.WarModelManager;
import uc.ap.war.core.model.WarPlayer;
import uc.ap.war.core.protocol.DirectiveHelper;
import uc.ap.war.core.protocol.MsgGroup;
import uc.ap.war.core.protocol.ProtoKw;

@SuppressWarnings("serial")
public class ActiveClient extends JFrame {
    private static final int FRAME_WIDTH = 1200;
    private static final int CTL_PANE_WIDTH = 380;
    private static final Logger log = Logger.getLogger(ActiveClient.class);

    private Socket monitorSock;
    private WarMonitorProxy mon;
    private PassiveClient svr;
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
    private JFormattedTextField tfMyPort;
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
    private JComboBox<String> cbCrackTargetIds;
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
    private JButton btnWarStatus;
    private JFrame frameForDialog = this;
    private JComboBox<String> cbWarTargetId;
    private JButton btnWarTruce;
    private JTextField tfOtherCookie;
    private JTextField tfOtherRupy;
    private JTextField tfOtherComputer;
    private JTextField tfOtherWeapon;
    private JTextField tfOtherVehicle;
    private JTextField tfOtherSteel;
    private JTextField tfOtherCopper;
    private JTextField tfOtherOil;
    private JTextField tfOtherGlass;
    private JTextField tfOtherPlastic;
    private JTextField tfOtherRubber;
    private JButton btnMakeCert;
    private JComboBox<Integer> cbRegPort;

    public ActiveClient() {
        super.setPreferredSize(new Dimension(FRAME_WIDTH, 700));
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // a 3-column layout
        super.setLayout(new GridLayout(0, 3));

        // left pane
        final JPanel leftPane = new JPanel();
        leftPane.setLayout(new BorderLayout());
        leftPane.add(buildConfigPane(), BorderLayout.NORTH);
        leftPane.add(buildLogPane(), BorderLayout.CENTER);
        super.add(leftPane);

        // middle pane
        final JPanel midPane = new JPanel();
        midPane.setLayout(new FlowLayout());
        super.add(midPane);
        midPane.add(buildRegPane());
        midPane.add(buildPlayerPane());
        midPane.add(buildResoursePane());

        // right pane
        final JPanel rightPane = new JPanel();
        rightPane.setLayout(new FlowLayout());
        super.add(rightPane);
        rightPane.add(buildCrackPane());
        rightPane.add(buildOthersPane());
        rightPane.add(buildWarPane());

        log.debug("WarActiveClient inited...");
    }

    private JPanel buildConfigPane() {
        final JPanel pane = initControlPane("Config", 200);

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
                svr = new PassiveClient(WarPlayer.ins().getPort(),
                        new PassiveDirectiveHandler(), pLog);
                svrThread = new Thread(svr);
                svrThread.start();
            }
        });
        pane.add(btnSvrUp);

        btnSvrDown = new JButton("Server Down");
        btnSvrDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                svrThread.interrupt();
            }
        });
        pane.add(btnSvrDown);

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

        pane.add(new JLabel("Monitor Port"));
        cbMonPort = new JComboBox<Integer>();
        WarPlayer.ins().setPort(8180);
        cbMonPort.addItem(WarPlayer.ins().getPort());
        cbMonPort.addItem(8160);
        pane.add(cbMonPort);

        pane.add(new JLabel("RMI Registry Port"));
        cbRegPort = new JComboBox<Integer>();
        cbRegPort.addItem(1099);
        cbRegPort.addItem(1098);
        cbRegPort.addItem(1097);
        pane.add(cbRegPort);

        btnConn = new JButton("Connect");
        btnConn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final ActiveDirectiveHandler cmdr = new ActiveDirectiveHandler();
                final String h = (String) cbMonHost.getSelectedItem();
                final int monP = (Integer) cbMonPort.getSelectedItem();
                final int regP = (Integer) cbRegPort.getSelectedItem();
                try {
                    CertMgrAdapter.ins().init(h, regP);
                } catch (SecurityServiceNotReadyException e) {
                    log.error(e);
                    // TODO: disable security relevant ui controls and notify
                    // user this failure
                }
                try {
                    monitorSock = new Socket(h, monP);
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
                            } catch (PlayerIdException | IOException
                                    | SecurityServiceException e) {
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

        btnMakeCert = new JButton("Make Certificate");
        btnMakeCert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mon.cmdMakeCert();
                } catch (SecurityServiceException e1) {
                    log.error(e1);
                }
            }
        });
        pane.add(btnMakeCert);

        final JButton btnIdentEnc = new JButton("Ident Enc");
        btnIdentEnc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mon.cmdIdentWithCrypto();
                } catch (PlayerIdException | SecurityServiceException e1) {
                    log.error(e1);
                }
            }
        });
        pane.add(btnIdentEnc);

        btnGameIdents = new JButton("Get Game Idents");
        btnGameIdents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdGameIdents();
            }
        });
        pane.add(btnGameIdents);

        return pane;
    }

    private JPanel buildCrackPane() {
        final JPanel pane = initControlPane("Crack", 100);

        pane.add(new JLabel("Target Id"));
        cbCrackTargetIds = new JComboBox<String>();
        cbCrackTargetIds.setModel(new DefaultComboBoxModel<String>(WarInfo
                .ins().getOtherPlayerIds()));
        pane.add(cbCrackTargetIds);

        final JTextField tfCrackStatusComputers = new JFormattedTextField(
                new RegexFormatter("\\d+"));
        tfCrackStatusComputers.setToolTipText("number of computers");
        final JButton btnCrackStatus = new JButton("Crack Status using:");
        btnCrackStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String pid = (String) cbCrackTargetIds.getSelectedItem();
                final int compAmt = Integer.parseInt(tfCrackStatusComputers
                        .getText());
                mon.cmdCrackStatus(pid, compAmt);
            }
        });
        pane.add(btnCrackStatus);
        pane.add(tfCrackStatusComputers);

        final JTextField tfCrackCookieComputers = new JFormattedTextField(
                new RegexFormatter("\\d+"));
        tfCrackCookieComputers.setToolTipText("number of computers");
        final JButton btnCrackCookie = new JButton("Crack Cookie using: ");
        btnCrackCookie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String pid = (String) cbCrackTargetIds.getSelectedItem();
                final int compAmt = Integer.parseInt(tfCrackCookieComputers
                        .getText());
                mon.cmdCrackCookie(pid, compAmt);
            }
        });
        pane.add(btnCrackCookie);
        pane.add(tfCrackCookieComputers);

        btnOtherHp = new JButton("Crack Host Port");
        btnOtherHp.setToolTipText("using 1 computer");
        btnOtherHp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdCrackHostPort((String) cbCrackTargetIds
                        .getSelectedItem());
            }
        });
        pane.add(btnOtherHp);

        btnRandomPlayerHp = new JButton("Random Host Port");
        btnRandomPlayerHp.setToolTipText("available every xxx minutes");
        btnRandomPlayerHp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdRandomPlayerHp();
            }
        });
        pane.add(btnRandomPlayerHp);

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

    private JPanel buildOthersPane() {
        final JPanel pane = initControlPane("Other Player", 400);

        pane.add(new JLabel("Id"));
        tfOtherId = new JTextField();
        pane.add(tfOtherId);
        pane.add(new JLabel("Host"));
        tfOtherHost = new JTextField();
        pane.add(tfOtherHost);
        pane.add(new JLabel("Port"));
        tfOtherPort = new JTextField();
        pane.add(tfOtherPort);

        pane.add(new JLabel("Cookie"));
        tfOtherCookie = new JTextField();
        pane.add(tfOtherCookie);

        pane.add(new JLabel("Rupyulars"));
        tfOtherRupy = new JTextField();
        tfOtherRupy.setEnabled(false);
        pane.add(tfOtherRupy);
        pane.add(new JLabel("Computers"));
        tfOtherComputer = new JTextField();
        tfOtherComputer.setEnabled(false);
        pane.add(tfOtherComputer);
        pane.add(new JLabel("Weapons"));
        tfOtherWeapon = new JTextField();
        tfOtherWeapon.setEnabled(false);
        pane.add(tfOtherWeapon);
        pane.add(new JLabel("Vehicles"));
        tfOtherVehicle = new JTextField();
        tfOtherVehicle.setEnabled(false);
        pane.add(tfOtherVehicle);
        pane.add(new JLabel("steel"));
        tfOtherSteel = new JTextField();
        tfOtherSteel.setEnabled(false);
        pane.add(tfOtherSteel);
        pane.add(new JLabel("copper"));
        tfOtherCopper = new JTextField();
        tfOtherCopper.setEnabled(false);
        pane.add(tfOtherCopper);
        pane.add(new JLabel("oil"));
        tfOtherOil = new JTextField();
        tfOtherOil.setEnabled(false);
        pane.add(tfOtherOil);
        pane.add(new JLabel("glass"));
        tfOtherGlass = new JTextField();
        tfOtherGlass.setEnabled(false);
        pane.add(tfOtherGlass);
        pane.add(new JLabel("plastic"));
        tfOtherPlastic = new JTextField();
        tfOtherPlastic.setEnabled(false);
        pane.add(tfOtherPlastic);
        pane.add(new JLabel("rubber"));
        tfOtherRubber = new JTextField();
        tfOtherRubber.setEnabled(false);
        pane.add(tfOtherRubber);

        return pane;
    }

    private JPanel buildPlayerPane() {
        final JPanel pane = new JPanel();
        pane.setPreferredSize(new Dimension(CTL_PANE_WIDTH, 400));
        pane.setBorder(BorderFactory.createTitledBorder("This Player"));
        pane.setLayout(new GridLayout(0, 2));

        // player id
        pane.add(new JLabel("Id"));
        tfMyId = new JTextField();
        tfMyId.getDocument().addDocumentListener(new TextFieldChangeHandler() {
            @Override
            protected void docChanged(DocumentEvent e) {
                WarPlayer.ins().setId(tfMyId.getText());
            }
        });
        pane.add(tfMyId);

        pane.add(new JLabel("Host"));
        tfMyHost = new JTextField();
        tfMyHost.getDocument().addDocumentListener(
                new TextFieldChangeHandler() {
                    @Override
                    protected void docChanged(DocumentEvent e) {
                        WarPlayer.ins().setHost(tfMyHost.getText());
                    }
                });
        pane.add(tfMyHost);

        pane.add(new JLabel("Port"));
        final NumberFormatter nf = new NumberFormatter();
        nf.setMinimum(new Integer(2048));
        nf.setMaximum(new Integer(65000));
        tfMyPort = new JFormattedTextField(nf);
        tfMyPort.addPropertyChangeListener("value",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        final Object v = evt.getNewValue();
                        if (v != null) {
                            final int port = ((Number) v).intValue();
                            WarPlayer.ins().setPort(port);
                        }
                    }
                });
        pane.add(tfMyPort);

        pane.add(new JLabel("Cookie"));
        tfMyCookie = new JTextField();
        tfMyCookie.setEnabled(false);
        pane.add(tfMyCookie);

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
                } catch (SecurityServiceException ex2) {
                    log.error(ex2);
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

    private JPanel buildResoursePane() {
        final JPanel pane = initControlPane("Resource", 120);

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

        btnTradeReq = new JButton("Request Trade with: ");
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

        final JPanel myResPane = new JPanel();
        myResPane.setLayout(new GridLayout(1, 2));
        myResPane.add(new JLabel("Trade"));
        tfTradeMyResAmt = new JTextField();
        myResPane.add(tfTradeMyResAmt);
        pane.add(myResPane);
        cbTradeMyRes = new JComboBox<String>(new DefaultComboBoxModel<String>(
                ProtoKw.RES_NAMES));
        pane.add(cbTradeMyRes);

        final JPanel forResPane = new JPanel();
        forResPane.setLayout(new GridLayout(1, 2));
        forResPane.add(new JLabel("For"));
        tfTradeForResAmt = new JTextField();
        forResPane.add(tfTradeForResAmt);
        pane.add(forResPane);
        cbTradeForRes = new JComboBox<String>(new DefaultComboBoxModel<String>(
                ProtoKw.RES_NAMES));
        pane.add(cbTradeForRes);

        return pane;
    }

    private JPanel buildWarPane() {
        final JPanel pane = initControlPane("War", 80);

        btnWarStatus = new JButton("War Status with:");
        btnWarStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mon.cmdWarStatus((String) cbWarTargetId.getSelectedItem());
            }
        });
        pane.add(btnWarStatus);
        cbWarTargetId = new JComboBox<String>();
        pane.add(cbWarTargetId);

        btnDeclareWar = new JButton("Declare War...");
        btnDeclareWar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog dialog = new JDialog(frameForDialog,
                        "Declare War", false);
                dialog.setLayout(new BorderLayout());

                final JPanel pane = new JPanel();
                pane.setPreferredSize(new Dimension(250, 150));
                pane.setLayout(new GridLayout(0, 2));
                dialog.add(pane, BorderLayout.CENTER);

                pane.add(new JLabel("Target Id"));
                final JTextField tfTargetId = new JTextField();
                pane.add(tfTargetId);
                pane.add(new JLabel("Target Host"));
                final JTextField tfTargetHost = new JTextField();
                pane.add(tfTargetHost);
                pane.add(new JLabel("Target Port"));
                final JTextField tfTargetPort = new JTextField();
                pane.add(tfTargetPort);
                pane.add(new JLabel("# of Weapons"));
                final JTextField tfWeaponsAmt = new JTextField();
                pane.add(tfWeaponsAmt);
                pane.add(new JLabel("# of Vehicles"));
                final JTextField tfVehiclesAmt = new JTextField();
                pane.add(tfVehiclesAmt);
                final JButton btnOk = new JButton("Ok");
                btnOk.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final String id = tfTargetId.getText();
                        final String host = tfTargetHost.getText();
                        final int port = Integer.parseInt(tfTargetPort
                                .getText());
                        final int wAmt = Integer.parseInt(tfWeaponsAmt
                                .getText());
                        final int vAmt = Integer.parseInt(tfVehiclesAmt
                                .getText());
                        mon.cmdDeclareWar(id, host, port, wAmt, vAmt);
                        dialog.setVisible(false);
                    }
                });
                pane.add(btnOk);
                final JButton btnCancel = new JButton("Cancel");
                btnCancel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.setVisible(false);
                    }
                });
                pane.add(btnCancel);

                dialog.pack();
                dialog.setVisible(true);
            }
        });
        pane.add(btnDeclareWar);

        btnWarTruce = new JButton("War Truce...");
        btnWarTruce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog dialog = new JDialog(frameForDialog,
                        "Offer War Truce", false);
                dialog.setLayout(new BorderLayout());

                final JPanel pane = new JPanel();
                pane.setPreferredSize(new Dimension(250, 400));
                pane.setLayout(new GridLayout(0, 2));
                dialog.add(pane, BorderLayout.CENTER);

                pane.add(new JLabel("Target Id"));
                final JTextField tfTargetId = new JTextField();
                pane.add(tfTargetId);
                pane.add(new JLabel(ProtoKw.RES_RUPYULARS));
                final JTextField tfRupy = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfRupy.setText("0");
                pane.add(tfRupy);
                pane.add(new JLabel(ProtoKw.RES_COMPUTERS));
                final JTextField tfComputer = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfComputer.setText("0");
                pane.add(tfComputer);
                pane.add(new JLabel(ProtoKw.RES_WEAPONS));
                final JTextField tfWeapon = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfWeapon.setText("0");
                pane.add(tfWeapon);
                pane.add(new JLabel(ProtoKw.RES_VEHICLES));
                final JTextField tfVehicle = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfVehicle.setText("0");
                pane.add(tfVehicle);
                pane.add(new JLabel(ProtoKw.RES_STEEL));
                final JTextField tfSteel = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfSteel.setText("0");
                pane.add(tfSteel);
                pane.add(new JLabel(ProtoKw.RES_COPPER));
                final JTextField tfCopper = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfCopper.setText("0");
                pane.add(tfCopper);
                pane.add(new JLabel(ProtoKw.RES_OIL));
                final JTextField tfOil = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfOil.setText("0");
                pane.add(tfOil);
                pane.add(new JLabel(ProtoKw.RES_GLASS));
                final JTextField tfGlass = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfGlass.setText("0");
                pane.add(tfGlass);
                pane.add(new JLabel(ProtoKw.RES_PLASTIC));
                final JTextField tfPlastic = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfPlastic.setText("0");
                pane.add(tfPlastic);
                pane.add(new JLabel(ProtoKw.RES_RUBBER));
                final JTextField tfRubber = new JFormattedTextField(
                        new RegexFormatter("\\d+"));
                tfRubber.setText("0");
                pane.add(tfRubber);
                final JButton btnOk = new JButton("Ok");
                btnOk.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final String id = tfTargetId.getText();
                        final int rupy = Integer.parseInt(tfRupy.getText());
                        final int comp = Integer.parseInt(tfComputer.getText());
                        final int weap = Integer.parseInt(tfWeapon.getText());
                        final int vehi = Integer.parseInt(tfVehicle.getText());
                        final int steel = Integer.parseInt(tfSteel.getText());
                        final int copper = Integer.parseInt(tfCopper.getText());
                        final int oil = Integer.parseInt(tfOil.getText());
                        final int glass = Integer.parseInt(tfGlass.getText());
                        final int plastic = Integer.parseInt(tfPlastic
                                .getText());
                        final int rubber = Integer.parseInt(tfRubber.getText());
                        try {
                            mon.cmdWarTruce(id, rupy, comp, weap, vehi, steel,
                                    copper, oil, glass, plastic, rubber);
                        } catch (PlayerIdException ex) {
                            log.error(ex);
                        }
                        dialog.setVisible(false);
                    }
                });
                pane.add(btnOk);
                final JButton btnCancel = new JButton("Cancel");
                btnCancel.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.setVisible(false);
                    }
                });
                pane.add(btnCancel);

                dialog.pack();
                dialog.setVisible(true);
            }
        });
        pane.add(btnWarTruce);

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

    class ActiveDirectiveHandler extends BasicDirectiveHandler {

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
        public void resultCrackCookie(final MsgGroup mg) {
            final String playerCookie = DirectiveHelper.parseCrackCookie(mg);
            if (playerCookie != null) {
                tfOtherCookie.setText(playerCookie);
            }
        }

        @Override
        public void resultCrackHostPort(final MsgGroup mg) {
            final String[] playerHp = DirectiveHelper.parseCrackHp(mg);
            if (playerHp != null && playerHp.length == 3) {
                tfOtherId.setText(playerHp[0]);
                tfOtherHost.setText(playerHp[1]);
                tfOtherPort.setText(playerHp[2]);
            }
        }

        @Override
        public void resultCrackStatus(final MsgGroup mg) {
            final HashMap<String, Integer> res = DirectiveHelper
                    .parseCrackStatus(mg);
            tfOtherRupy.setText(String.valueOf(res.get(ProtoKw.RES_RUPYULARS)));
            tfOtherComputer.setText(String.valueOf(res
                    .get(ProtoKw.RES_COMPUTERS)));
            tfOtherWeapon.setText(String.valueOf(res.get(ProtoKw.RES_WEAPONS)));
            tfOtherVehicle
                    .setText(String.valueOf(res.get(ProtoKw.RES_VEHICLES)));
            tfOtherSteel.setText(String.valueOf(res.get(ProtoKw.RES_STEEL)));
            tfOtherCopper.setText(String.valueOf(res.get(ProtoKw.RES_COPPER)));
            tfOtherOil.setText(String.valueOf(res.get(ProtoKw.RES_OIL)));
            tfOtherGlass.setText(String.valueOf(res.get(ProtoKw.RES_GLASS)));
            tfOtherPlastic
                    .setText(String.valueOf(res.get(ProtoKw.RES_PLASTIC)));
            tfOtherRubber.setText(String.valueOf(res.get(ProtoKw.RES_RUBBER)));
        }

        @Override
        public void resultGameIds(final MsgGroup mg) {
            final String[] gameIds = DirectiveHelper.parseGameIds(mg);
            log.debug("Got game ids: " + gameIds);
            cbCrackTargetIds
                    .setModel(new DefaultComboBoxModel<String>(gameIds));
            cbTradeTargetId.setModel(new DefaultComboBoxModel<String>(gameIds));
            cbTradeTargetId.addItem("MONITOR");
            cbWarTargetId.setModel(new DefaultComboBoxModel<String>(gameIds));
        }

        @Override
        public void resultPlayerStatus(final MsgGroup mg) {
            super.resultPlayerStatus(mg);
            refreshMyResourceInfo();
        }

        @Override
        public void resultRandomHostPort(final MsgGroup mg) {
            final String[] playerHp = DirectiveHelper.parseCrackHp(mg);
            if (playerHp != null && playerHp.length == 3) {
                tfOtherId.setText(playerHp[0]);
                tfOtherHost.setText(playerHp[1]);
                tfOtherPort.setText(playerHp[2]);
            }
        }

    }

    class PassiveDirectiveHandler extends BasicDirectiveHandler {

        @Override
        public void requireTradeResp(final WarMonitorProxy mon,
                final MsgGroup mg) {
            final String[] options = { "Accept", "Decline" };
            final int optIndex = JOptionPane.showOptionDialog(frameForDialog,
                    "Trade requested from ...", "Trade Requested",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (optIndex == 0) {
                log.debug("Trade request accepted...");
                mon.cmdTradeAccepted();
            } else {
                log.debug("Trade request declined");
                mon.cmdTradeDeclined();
            }
        }

        @Override
        public void requireWarDefend(WarMonitorProxy warMonitorProxy,
                MsgGroup mg) {
            final JDialog dialog = new JDialog(frameForDialog, "War Declared",
                    false);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    log.debug("Not allowed to close dialog directly...");
                }
            });

            final JPanel pane = new JPanel();
            dialog.add(pane, BorderLayout.CENTER);
            pane.setPreferredSize(new Dimension(150, 100));

            pane.setLayout(new GridLayout(0, 1));
            pane.add(new JLabel("xxx declared war!"));
            pane.add(new JLabel("How many resource used for depending?"));
            final JFormattedTextField tfResAmt = new JFormattedTextField(
                    new RegexFormatter("\\d+"));
            pane.add(tfResAmt);
            final JButton btnOk = new JButton("Ok");
            btnOk.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (tfResAmt.getValue() != null) {
                        final int resAmt = Integer.parseInt((String) tfResAmt
                                .getValue());
                        log.debug("gonna defend war with " + resAmt
                                + "weapons and vehicles");
                        mon.cmdDefendWar(resAmt);
                        dialog.setVisible(false);
                    } else {
                        log.debug("waiting for valid rsource amount ...");
                    }
                }
            });
            pane.add(btnOk);

            dialog.pack();
            dialog.setVisible(true);
        }

    }
}
