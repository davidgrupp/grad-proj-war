package uc.ap.war.core;

import org.apache.log4j.Logger;

import uc.ap.war.core.protocol.ProtoKw;

public class MsgGroup {
    private static final Logger log = Logger.getLogger(MsgGroup.class);
    private String resultArg;
    private String resultStr;
    private String cmdError;
    private String requiredCmd;
    private StringBuilder msgs;

    /**
     * Prevent initiation by classes in other packages.
     */
    MsgGroup() {
        resultArg = "";
        resultStr = "";
        cmdError = "";
        requiredCmd = "";
        msgs = new StringBuilder();
    }

    /**
     * Prevent change in state by classes in other packages.
     */
    boolean addMsg(final String msg) {
        if (msg == null) {
            return false;
        }
        if (msg.startsWith(ProtoKw.DIR_WAIT)) {
            log.debug("WAITING directive encounted, end of message group.");
            return false;
        }
        if (msg.startsWith(ProtoKw.DIR_RESULT)) {
            final String[] tokens = msg.split("\\s+", 3);
            final String arg = tokens[1];
            if (arg.equals(ProtoKw.DIR_QUIT)) {
                log.debug("End of transaction, parsing aborted.");
                return false;
            }
            this.resultArg = arg;
            if (tokens.length == 3) {
                this.resultStr = tokens[2];
            }
        } else if (msg.startsWith(ProtoKw.DIR_CMD_ERR)) {
            this.cmdError = msg.split("\\s+", 2)[1];
        } else if (msg.startsWith(ProtoKw.DIR_REQ)) {
            this.requiredCmd = msg.split("\\s+")[1];
        }
        this.msgs.append(msg).append("\n");
        return true;
    }

    public String toString() {
        return this.msgs.toString();
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

    public String getCmdError() {
        return cmdError;
    }

    public String getRequiredCmd() {
        return requiredCmd;
    }

}
