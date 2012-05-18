package uc.ap.war.protocol;

public class MsgGroup {
	private String resultArg;
	private String resultStr;
	private String cmdError;
	private String requiredCmd;
	private StringBuilder msgs;

	MsgGroup() {
		resultArg = "";
		resultStr = "";
		cmdError = "";
		requiredCmd = "";
		msgs = new StringBuilder();
	}

	void addMsg(final String msg) {
		this.msgs.append(msg).append("\n");
	}

	public String toString() {
		return this.msgs.toString();
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
