package uc.ap.war;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.protocol.DirectiveHandler;
import uc.ap.war.protocol.MsgGroup;
import uc.ap.war.protocol.WarMonitorProxy;
import uc.ap.war.protocol.exp.PlayerIdException;

@SuppressWarnings("serial")
public class WarClientGUI extends JFrame {
	static Logger log = Logger.getLogger(WarClientGUI.class);
	private WarMonitorProxy mon;
	private WarServer svr;
	private Thread svrThread;
	private JTextArea taLog;
	private JTextField tfId;
	private JComboBox<String> cbMonHost;
	private JComboBox<Integer> cbMonPort;
	private JButton btnConn;
	private JButton btnId;
	private JButton btnPwd;
	private JButton btnHostPort;
	private JTextField tfSvrHost;
	private JTextField tfSvrPort;
	private JButton btnSvrUp;
	private JButton btnSvrDown;

	public WarClientGUI() {
		super.setPreferredSize(new Dimension(300, 300));
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		super.setLayout(new GridLayout(2, 0));

		// final JScrollPane logPane = new JScrollPane();
		// logPane.setPreferredSize(new Dimension(200, 100));
		// super.add(logPane);
		taLog = new JTextArea(5, 20);
		super.add(taLog);
		// ui control pane
		final JPanel ctlPane = new JPanel();
		ctlPane.setLayout(new GridLayout(0, 2));
		super.add(ctlPane);

		// monitor host
		WarPlayer.setHost("localhost");
		cbMonHost = new JComboBox<String>();
		cbMonHost.addItem(WarPlayer.getHost());
		cbMonHost.addItem("gauss.ececs.uc.edu");
		cbMonHost.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WarPlayer.setHost((String) cbMonHost.getSelectedItem());
			}
		});
		ctlPane.add(cbMonHost);
		// monitor port
		cbMonPort = new JComboBox<Integer>();
		WarPlayer.setPort(8180);
		cbMonPort.addItem(WarPlayer.getPort());
		cbMonPort.addItem(8160);
		ctlPane.add(cbMonPort);

		// connect button
		btnConn = new JButton("Connect");
		btnConn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final GUIRequiredCmdHandler cmdr = new GUIRequiredCmdHandler();
				final String h = (String) cbMonHost.getSelectedItem();
				final int p = (Integer) cbMonPort.getSelectedItem();
				try {
					final Socket s = new Socket(h, p);
					final Reader r = new InputStreamReader(s.getInputStream());
					final Writer w = new OutputStreamWriter(s.getOutputStream());
					mon = new WarMonitorProxy(r, w, cmdr);

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
		ctlPane.add(btnConn);

		// player id
		tfId = new JTextField();
		tfId.getDocument().addDocumentListener(new TextFieldChangeHandler() {
			@Override
			protected void docChanged(DocumentEvent e) {
				WarPlayer.setId(tfId.getText());
			}
		});
		ctlPane.add(tfId);

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
		ctlPane.add(btnId);

		// button password
		btnPwd = new JButton("Password");
		btnPwd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnPwd.setForeground(new Color(0, 0, 0));
				mon.cmdPwd();
			}
		});
		ctlPane.add(btnPwd);

		tfSvrHost = new JTextField();
		tfSvrHost.getDocument().addDocumentListener(
				new TextFieldChangeHandler() {
					@Override
					protected void docChanged(DocumentEvent e) {
						WarPlayer.setHost(tfSvrHost.getText());
					}
				});
		ctlPane.add(tfSvrHost);
		tfSvrPort = new JTextField();
		tfSvrPort.getDocument().addDocumentListener(
				new TextFieldChangeHandler() {
					@Override
					protected void docChanged(DocumentEvent e) {
						try {
							int port = Integer.parseInt(tfSvrPort.getText());
							WarPlayer.setPort(port);
						} catch (NumberFormatException e1) {
							// simply do nothing
						}
					}
				});
		ctlPane.add(tfSvrPort);

		// button server up
		btnSvrUp = new JButton("Server Up");
		btnSvrUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svr = new WarServer(WarPlayer.getPort());
				svrThread = new Thread(svr);
				svrThread.start();
			}
		});
		ctlPane.add(btnSvrUp);

		// button server down
		btnSvrDown = new JButton("Server Down");
		btnSvrDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				svrThread.interrupt();
			}
		});
		ctlPane.add(btnSvrDown);

		// button host port
		btnHostPort = new JButton("Host Port");
		btnHostPort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				mon.cmdHostPort();
			}
		});
		ctlPane.add(btnHostPort);

		log.debug("CheckerGameFrame inited...");
	}

	class GUIRequiredCmdHandler implements DirectiveHandler {

		@Override
		public void requireIdent() throws PlayerIdException {
			taLog.append(mon.getLastMsgGroup().toString());
			btnId.setForeground(new Color(255, 0, 0));
		}

		@Override
		public void requirePwd() {
			final MsgGroup mg = mon.getLastMsgGroup();
			taLog.append(mg.toString());
			btnPwd.setForeground(new Color(255, 0, 0));
		}

		@Override
		public void requireHostPort() {
			taLog.append(mon.getLastMsgGroup().toString());
		}

		@Override
		public void requireAlive() {
			taLog.append(mon.getLastMsgGroup().toString());
		}

		@Override
		public void resultPwd() {
			final MsgGroup mg = mon.getLastMsgGroup();
			log.debug("got monitor cookie: " + mg.getResultStr());
			WarPlayer.setCookie(mg.getResultStr());
		}

		@Override
		public void requireQuit() {
			log.debug("quit command required");
		}

	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");

		final WarClientGUI f = new WarClientGUI();
		f.pack();
		f.setVisible(true);

		log.debug("WarClient started.");
	}
}
