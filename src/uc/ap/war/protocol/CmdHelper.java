package uc.ap.war.protocol;

public class CmdHelper {

	public static final String CMD_ID = "IDENT";
	public static final String CMD_PWD = "PASSWORD";
	public static final String CMD_HP = "HOST_PORT";
	public static final String CMD_ALIVE = "ALIVE";
	public static final String CMD_QUIT = "QUIT";

	public static String ident(final String id) {
		return CMD_ID + " " + id;
	}

	public static String pwd(final String pwd) {
		return CMD_PWD + " " + pwd;
	}

	public static String hostPort(final String host, final int port) {
		StringBuilder sb = new StringBuilder(CMD_HP);
		sb.append(" ").append(host).append(" ").append(port);
		return sb.toString();
	}

	public static String alive(final String cookie) {
		return CMD_ALIVE + " " + cookie;
	}

	public static String quit() {
		return CMD_QUIT;
	}
}
