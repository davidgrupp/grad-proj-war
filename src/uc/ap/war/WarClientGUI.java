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
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uc.ap.war.protocol.MsgGroup;
import uc.ap.war.protocol.RequiredCommandHandler;
import uc.ap.war.protocol.WarMonitorProxy;
import uc.ap.war.protocol.exp.PlayerIdException;

@SuppressWarnings("serial")
public class WarClientGUI extends JFrame {
	static Logger log = Logger.getLogger(WarClientGUI.class);
	private JTextArea taLog;
	private JTextField tfId;
	private JComboBox<String> cbMonHost;
	private JComboBox<Integer> cbMonPort;
	private JButton btnConn;
	private JButton btnId;
	private JButton btnPwd;
	private WarMonitorProxy mon;

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
		ctlPane.setLayout(new GridLayout(5, 5));
		super.add(ctlPane);
		// player id
		tfId = new JTextField();
		tfId.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				updatePlayerId();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				updatePlayerId();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				updatePlayerId();
			}

			private void updatePlayerId() {
				WarPlayer.setId(tfId.getText());
			}
		});
		ctlPane.add(tfId);
		// monitor host
		cbMonHost = new JComboBox<String>();
		cbMonHost.addItem("localhost");
		cbMonHost.addItem("gauss.ececs.uc.edu");
		// monitor port
		cbMonPort = new JComboBox<Integer>();
		cbMonPort.addItem(8180);
		cbMonPort.addItem(8160);
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

		log.debug("CheckerGameFrame inited...");
	}

	class GUIRequiredCmdHandler implements RequiredCommandHandler {

		@Override
		public void ident() throws PlayerIdException {
			taLog.append(mon.getLastMsgGroup().toString());
			btnId.setForeground(new Color(255, 0, 0));
		}

		@Override
		public void pwd() {
			final MsgGroup mg = mon.getLastMsgGroup();
			taLog.append(mg.toString());
			log.debug("got monitor cookie: " + mg.getResult());
			WarPlayer.setCookie(mg.getResult());
			btnPwd.setForeground(new Color(255, 0, 0));
		}

		@Override
		public void hostPort() {
			taLog.append(mon.getLastMsgGroup().toString());

		}

		@Override
		public void alive() {
			taLog.append(mon.getLastMsgGroup().toString());
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
