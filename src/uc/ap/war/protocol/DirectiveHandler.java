package uc.ap.war.protocol;

import uc.ap.war.protocol.exp.PlayerIdException;

public interface DirectiveHandler {

    // required command
    public void requireAlive(final WarMonitorProxy mon);

    public void requireHostPort(final WarMonitorProxy mon);

    public void requireIdent(final WarMonitorProxy mon)
            throws PlayerIdException;

    public void requirePwd(final WarMonitorProxy mon);

    public void requireQuit(final WarMonitorProxy mon);

    public void requireTradeResp(final WarMonitorProxy mon, final MsgGroup mg);

    public void requireTruceResp(final WarMonitorProxy mon, final MsgGroup mg);

    public void requireWarDefend(final WarMonitorProxy mon, final MsgGroup mg);

    // result
    public void resultCrackCookie(final MsgGroup mg);

    public void resultCrackHostPort(final MsgGroup mg);

    public void resultCrackStatus(final MsgGroup mg);

    public void resultGameIds(final MsgGroup mg);

    public void resultPlayerStatus(final MsgGroup mg);

    public void resultPwd(final MsgGroup mg);

    public void resultQuit(final MsgGroup mg);

    public void resultRandomHostPort(final MsgGroup mg);

    public void resultMakeCert(final MsgGroup mg);

}
