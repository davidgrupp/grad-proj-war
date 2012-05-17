package uc.ap.war.protocol;

import uc.ap.war.protocol.exp.PlayerIdException;

public interface RequiredCommandHandler {
	public void ident() throws PlayerIdException;
	public void pwd();
	public void hostPort();
	public void alive();
}
