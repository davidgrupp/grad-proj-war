package uc.ap.war.core;

import org.apache.log4j.Logger;

import uc.ap.war.core.protocol.ProtoKw;

public class MsgGroup {
    private static final Logger log = Logger.getLogger(MsgGroup.class);
    private String resultArg;
    private String resultStr;
    private String cmdError;
    private String requiredCmd;
    private String pwChecksum;
    private StringBuilder msgs;
    private boolean done;

    /**
     * Prevent initiation by classes in other packages.
     */
    MsgGroup() {
        resultArg = "";
        resultStr = "";
        cmdError = "";
        requiredCmd = "";
        pwChecksum = "";
        done = false;
        msgs = new StringBuilder();
    }

    public String getCmdError() {
        return cmdError;
    }

    public String getPwCheckSum() {
        return pwChecksum;
    }

    public String getRequiredCmd() {
        return requiredCmd;
    }

    public String getResult() {
        return resultArg + " " + resultStr;
    }

    public String getResultArg() {
        return resultArg;
    }

    public String getResultStr() {
        return resultStr;
    }

    public String toString() {
        return this.msgs.toString();
    }

    /**
     * Prevent change in state by classes in other packages.
     * 
     * @return True if end of message group is encountered. False if more
     *         messages(directives) are expected.
     */
    boolean addMsg(final String msg) {
        // FIXME: this method's logic is obscured and ugly, fix it if we have
        // time
        if (done) {
            return true;
        }
        if (msg == null) {
            done = true;
            return true;
        }
        if (msg.startsWith(ProtoKw.DIR_WAIT)) {
            log.debug("WAITING directive encounted, end of message group.");
            done = true;
            return true;
        }
        final String[] tokens = msg.split("\\s+", 3);
        if (msg.startsWith(ProtoKw.DIR_RESULT) && tokens.length >= 2) {
            final String arg = tokens[1];
            this.resultArg = arg;
            if (arg.equals(ProtoKw.DIR_QUIT)) {
                log.debug("End of transaction, parsing aborted.");
                done = true;
                return true;
            }
            if (tokens.length == 3) {
                this.resultStr = tokens[2];
            }
        } else if (msg.startsWith(ProtoKw.DIR_CMD_ERR) && tokens.length == 2) {
            this.cmdError = tokens[1];
        } else if (msg.startsWith(ProtoKw.DIR_REQ) && tokens.length == 2) {
            this.requiredCmd = tokens[1];
        } else if (msg.startsWith(ProtoKw.DIR_PW_CHECKSUM)
                && tokens.length == 2) {
            this.pwChecksum = tokens[1];
        }
        this.msgs.append(msg).append("\n");
        return false;
    }

}
