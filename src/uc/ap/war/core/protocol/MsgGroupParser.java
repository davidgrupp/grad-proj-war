package uc.ap.war.core.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

public class MsgGroupParser {
	static Logger log = Logger.getLogger(MsgGroupParser.class);
	private BufferedReader bReader = null;
	private StringBuilder parsedMsgGroup;

	public MsgGroupParser(final Reader reader) {
		this.bReader = new BufferedReader(reader);
	}

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
			if (directive.startsWith(ProtoKw.DIR_WAIT)) {
				break;
			} else if (directive.startsWith(ProtoKw.DIR_RESULT)) {
				final String[] tokens = directive.split("\\s+", 3);
				final String arg = tokens[1];
				if (arg.equals(ProtoKw.DIR_QUIT)) {
					log.debug("End of transaction, parsing aborted.");
					return null;
				}
				mg.setResultArg(arg);
				if (tokens.length == 3) {
					mg.setResultStr(tokens[2]);
				}
			} else if (directive.startsWith(ProtoKw.DIR_CMD_ERR)) {
				mg.setCmdError(directive.split("\\s+", 2)[1]);
			} else if (directive.startsWith(ProtoKw.DIR_REQ)) {
				mg.setRequiredCmd(directive.split("\\s+")[1]);
			}
			directive = this.bReader.readLine();
		}
		return mg;
	}

	public String getParsedMsgGroup() {
		return this.parsedMsgGroup.toString();
	}
	
}
