package uc.ap.war.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

public class MsgGroupParser {
	static Logger log = Logger.getLogger(MsgGroupParser.class);
	public static final String DIR_WAIT = "WAITING";
	public static final String DIR_REQ = "REQUIRE";
	public static final String DIR_RESULT = "RESULT";
	public static final String DIR_CMD_ERR = "COMMAND_ERROR";
	public static final String DIR_QUIT = "QUIT";
	private BufferedReader bReader = null;
//	private String result;
//	private String cmdError;
//	private String requiredCmd;
	private StringBuilder parsedMsgGroup;

	public MsgGroupParser(final Reader reader) {
		this.bReader = new BufferedReader(reader);
//		this.clearParsedValues();
	}

//	public boolean parsePendingDirectives() throws IOException {
//		String directive = this.bReader.readLine();
//		this.parsedMsgGroup = new StringBuilder();
//		if (directive == null) {
//			log.debug("No incoming message, parsing aborted.");
//			this.clearParsedValues();
//			return false;
//		}
//		while (directive != null && !directive.startsWith(DIR_WAIT)) {
//			log.debug("parsing: " + directive);
//			this.parsedMsgGroup.append(directive).append("\n");
//			if (directive.startsWith(DIR_RESULT)) {
//				result = directive.split("\\s+", 2)[1];
//				if (result.equals(DIR_QUIT)) {
//					log.debug("End of transaction, parsing aborted.");
//					cmdError = "";
//					requiredCmd = "";
//					return false;
//				}
//			} else if (directive.startsWith(DIR_CMD_ERR)) {
//				cmdError = directive.split("\\s+", 2)[1];
//			}
//			if (directive.startsWith(DIR_REQ)) {
//				requiredCmd = directive.split("\\s+")[1];
//			}
//			directive = this.bReader.readLine();
//		}
//		return true;
//	}

	public MsgGroup next() throws IOException {
		String directive = this.bReader.readLine();
		if (directive == null) {
			log.debug("No incoming message, parsing aborted.");
			return null;
		}
		final MsgGroup mg = new MsgGroup();
		while (directive != null) {
			log.debug("parsing: " + directive);
			mg.addMsg(directive);
			if (directive.startsWith(DIR_WAIT)) {
				break;
			} else if (directive.startsWith(DIR_RESULT)) {
				final String res = directive.split("\\s+", 2)[1];
				if (res.equals(DIR_QUIT)) {
					log.debug("End of transaction, parsing aborted.");
					return null;
				}
				mg.setResult(res);
			} else if (directive.startsWith(DIR_CMD_ERR)) {
				mg.setCmdError(directive.split("\\s+", 2)[1]);
			} else if (directive.startsWith(DIR_REQ)) {
				mg.setRequiredCmd(directive.split("\\s+")[1]);
			}
			directive = this.bReader.readLine();
		}
		return mg;
	}

	public String getParsedMsgGroup() {
		return this.parsedMsgGroup.toString();
	}

//	public String getResult() {
//		return this.result;
//	}
//
//	public String getCmdError() {
//		return this.cmdError;
//	}
//
//	public String getRequiredCmd() {
//		return this.requiredCmd;
//	}
//
//	private void clearParsedValues() {
//		result = "";
//		cmdError = "";
//		requiredCmd = "";
//	}
}
