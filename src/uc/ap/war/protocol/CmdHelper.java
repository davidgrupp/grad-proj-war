package uc.ap.war.protocol;

public class CmdHelper {

    public static String alive(final String cookie) {
        return ProtoKw.CMD_ALIVE + " " + cookie;
    }

    public static String hostPort(final String host, final int port) {
        StringBuilder sb = new StringBuilder(ProtoKw.CMD_HP);
        sb.append(" ").append(host).append(" ").append(port);
        return sb.toString();
    }

    public static String ident(final String id) {
        return ProtoKw.CMD_ID + " " + id;
    }

    public static String playerStatus() {
        return ProtoKw.CMD_PSTAT;
    }

    public static String pwd(final String pwd) {
        return ProtoKw.CMD_PWD + " " + pwd;
    }

    public static String quit() {
        return ProtoKw.CMD_QUIT;
    }

    public static String signOff() {
        return ProtoKw.CMD_SIGN_OFF;
    }

    public static String synth() {
        return ProtoKw.CMD_SYNTH;
    }

    public static String gameIdents() {
        return ProtoKw.CMD_GAME_IDS;
    }

    public static String randomPlayerHp() {
        return ProtoKw.CMD_RANDOM_PLAYER_HP;
    }

    public static String playerHp(String playerId) {
        return ProtoKw.CMD_PLAYER_HP + " " + playerId;
    }

    public static String tradeReq(String myId, String myRes, String myResAmt,
            String targetId, String forRes, String forResAmt) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_TRADE_REQ);
        cmd.append(" ").append(myId);
        cmd.append(" ").append(myRes).append(" ").append(myResAmt);
        cmd.append(" ").append(ProtoKw.CMD_TRADE_REQ_FOR);
        cmd.append(" ").append(targetId);
        cmd.append(" ").append(forRes).append(" ").append(forResAmt);
        return cmd.toString();
    }

    public static String tradeAccepted() {
        return ProtoKw.CMD_TRADE_RESP + " " + ProtoKw.CMD_ARG_ACCEPT;
    }
    
    public static String tradeDeclined() {
        return ProtoKw.CMD_TRADE_RESP + " " + ProtoKw.CMD_ARG_DECLINE;
    }
}
