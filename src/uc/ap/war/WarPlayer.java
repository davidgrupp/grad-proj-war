package uc.ap.war;

import org.apache.log4j.Logger;

import uc.ap.war.protocol.exp.PlayerIdException;

public class WarPlayer {
	static Logger log = Logger.getLogger(WarPlayer.class);
	private static String PW = "default_pw";
	private static String NEW_PW = "";
	private static String COOKIE = "";
	private static String ID = "";
	private static String HOST = "";
	private static Integer PORT = 0;

	public static void setPw(final String pw) {
		synchronized (PW) {
			PW = pw;
		}
		log.debug("password updated: " + pw);
	}

	public static void genNewPw() {
		// todo: generate random new pwd
	}

	public static String getNewPw() {
		return NEW_PW;
	}

	public static String getPw() {
		return PW;
	}

	public static void setCookie(final String cookie) {
		synchronized (COOKIE) {
			COOKIE = cookie;
		}
		log.debug("cookie updated: " + cookie);
	}

	public static String getCookie() {
		return COOKIE;
	}

	public static void setId(final String id) {
		synchronized (ID) {
			ID = id;
		}
		log.debug("id updated: " + id);
	}

	public static String getId() throws PlayerIdException {
		if (ID.equals("")) {
			throw new PlayerIdException();
		}
		return ID;
	}

	public static void setHost(final String host) {
		synchronized (HOST) {
			HOST = host;
		}
		log.debug("host updated: " + host);
	}

	public static String getHost() {
		return HOST;
	}

	public static void setPort(final int port) {
		synchronized (PORT) {
			PORT = port;
		}
		log.debug("port updated: " + port);
	}

	public static int getPort() {
		return PORT;
	}
}
