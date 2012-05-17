package uc.ap.war.protocol;

public class MsgGroup {
	private String result;
	private String cmdError;
	private String requiredCmd;
	private StringBuilder msgs;

	MsgGroup() {
		result = "";
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

	public String getResult() {
		return result;
	}

	void setResult(String result) {
		this.result = result;
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
