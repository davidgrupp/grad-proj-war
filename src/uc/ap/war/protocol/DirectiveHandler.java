package uc.ap.war.protocol;

import uc.ap.war.protocol.exp.PlayerIdException;

public interface DirectiveHandler {
	public void requireIdent() throws PlayerIdException;
	public void requirePwd();
	public void requireHostPort();
	public void requireAlive();
	public void requireQuit();
	public void resultPwd();
}
