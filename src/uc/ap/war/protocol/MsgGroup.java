package uc.ap.war.protocol;

import org.apache.log4j.Logger;

public class MsgGroup {
    private static final Logger log = Logger.getLogger(MsgGroup.class);
    private String resultArg;
    private String resultStr;
    private String cmdError;
    private String requiredCmd;
    private StringBuilder msgs;
    private boolean completed;

    MsgGroup() {
        resultArg = "";
        resultStr = "";
        cmdError = "";
        requiredCmd = "";
        completed = false;
        msgs = new StringBuilder();
    }

    boolean addMsg(final String msg) {
        if (msg == null) {
            completed = true;
            return false;
        }
        if (msg.startsWith(ProtoKw.DIR_WAIT)) {
            log.debug("WAITING directive encounted, end of message group.");
            completed = true;
            return false;
        }
        if (msg.startsWith(ProtoKw.DIR_RESULT)) {
            final String[] tokens = msg.split("\\s+", 3);
            final String arg = tokens[1];
            if (arg.equals(ProtoKw.DIR_QUIT)) {
                log.debug("End of transaction, parsing aborted.");
                completed = true;
                return false;
            }
            setResultArg(arg);
            if (tokens.length == 3) {
                setResultStr(tokens[2]);
            }
        } else if (msg.startsWith(ProtoKw.DIR_CMD_ERR)) {
            setCmdError(msg.split("\\s+", 2)[1]);
        } else if (msg.startsWith(ProtoKw.DIR_REQ)) {
            setRequiredCmd(msg.split("\\s+")[1]);
        }
        this.msgs.append(msg).append("\n");
        return true;
    }

    public String toString() {
        return this.msgs.toString();
    }

    public String getResult() {
        // TODO: move result string parsing logic here
        return resultArg + " " + resultStr;
    }

    public String getResultArg() {
        return resultArg;
    }

    void setResultArg(String arg) {
        this.resultArg = arg;
    }

    public String getResultStr() {
        return resultStr;
    }

    void setResultStr(String str) {
        this.resultStr = str;
    }

    public String getCmdError() {
        return cmdError;
    }

    void setCmdError(String cmdError) {
        this.cmdError = cmdError;
    }

    public String getRequiredCmd() {
        return requiredCmd;
    }

    void setRequiredCmd(String requiredCmd) {
        this.requiredCmd = requiredCmd;
    }

}
