package uc.ap.war.core.protocol;

public class CmdHelper {

    public static String alive(final String cookie) {
        return ProtoKw.CMD_ALIVE + " " + cookie;
    }

    public static String changePwd(final String oldPwd, final String newPwd) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_CHANGE_PWD);
        cmd.append(" ").append(oldPwd);
        cmd.append(" ").append(newPwd);
        return cmd.toString();
    }

    public static String crackCookie(final String targetId, int compAmt) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_CRACK_COOKIE);
        cmd.append(" ").append(targetId);
        cmd.append(" ").append(compAmt);
        return cmd.toString();
    }

    public static String crackHostPort(final String targetId) {
        return ProtoKw.CMD_CRACK_HP + " " + targetId;
    }

    public static String crackStatus(final String targetId, int compAmt) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_CRACK_STATUS);
        cmd.append(" ").append(targetId);
        cmd.append(" ").append(compAmt);
        return cmd.toString();
    }

    public static String declearWar(final String targetId, String host,
            final int port, final int weaponsAmt, int vehiclesAmt) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_WAR_DECLARE);
        cmd.append(" ").append(targetId);
        cmd.append(" ").append(host);
        cmd.append(" ").append(port);
        cmd.append(" ").append(weaponsAmt);
        cmd.append(" ").append(vehiclesAmt);
        return cmd.toString();
    }

    public static String defendWar(int weaponsAmt, int vehiclesAmt) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_WAR_DEFEND);
        cmd.append(" ").append(weaponsAmt);
        cmd.append(" ").append(vehiclesAmt);
        return cmd.toString();
    }

    public static String gameIdents() {
        return ProtoKw.CMD_GAME_IDS;
    }

    public static String hostPort(final String host, final int port) {
        final StringBuilder sb = new StringBuilder(ProtoKw.CMD_HP);
        sb.append(" ").append(host).append(" ").append(port);
        return sb.toString();
    }

    public static String ident(final String id) {
        return ProtoKw.CMD_ID + " " + id;
    }

    public static String ident(final String id, final String myHalfKey) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_ID);
        cmd.append(" ").append(id);
        cmd.append(" ").append(myHalfKey);
        return cmd.toString();
    }

    public static String makeCert(final String myPubExp, final String myPubMod) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_MAKE_CERT);
        cmd.append(" ").append(myPubExp);
        cmd.append(" ").append(myPubMod);
        return cmd.toString();
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

    public static String randomPlayerHp() {
        return ProtoKw.CMD_RANDOM_PLAYER_HP;
    }

    public static String signOff() {
        return ProtoKw.CMD_SIGN_OFF;
    }

    public static String synth() {
        return ProtoKw.CMD_SYNTH;
    }

    public static String tradeAccepted() {
        return ProtoKw.CMD_TRADE_RESP + " " + ProtoKw.CMD_ARG_ACCEPT;
    }

    public static String tradeDeclined() {
        return ProtoKw.CMD_TRADE_RESP + " " + ProtoKw.CMD_ARG_DECLINE;
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

    public static String warStatus(String warTargetId) {
        return ProtoKw.CMD_WAR_STATUS + " " + warTargetId;
    }

    public static String warTruce(String offeringId, String offerToId,
            int rupy, int comp, int weap, int vehi, int steel, int copper,
            int oil, int glass, int plastic, int rubber) {
        final StringBuilder cmd = new StringBuilder(ProtoKw.CMD_WAR_TRUCE_OFFER);
        cmd.append(" ").append(offeringId);
        cmd.append(" to ").append(offerToId);
        if (rupy > 0) {
            cmd.append(" ").append(rupy);
        }
        if (comp > 0) {
            cmd.append(" ").append(comp);
        }
        if (weap > 0) {
            cmd.append(" ").append(weap);
        }
        if (vehi > 0) {
            cmd.append(" ").append(vehi);
        }
        if (steel > 0) {
            cmd.append(" ").append(steel);
        }
        if (copper > 0) {
            cmd.append(" ").append(copper);
        }
        if (oil > 0) {
            cmd.append(" ").append(oil);
        }
        if (glass > 0) {
            cmd.append(" ").append(glass);
        }
        if (plastic > 0) {
            cmd.append(" ").append(plastic);
        }
        if (rubber > 0) {
            cmd.append(" ").append(rubber);
        }
        return cmd.toString();
    }
}
