package uc.ap.war.protocol;

import uc.ap.war.protocol.exp.PlayerIdException;

public interface DirectiveHandler {
    public void requireIdent(final WarMonitorProxy mon)
            throws PlayerIdException;

    public void requirePwd(final WarMonitorProxy mon);

    public void requireHostPort(final WarMonitorProxy mon);

    public void requireAlive(final WarMonitorProxy mon);

    public void requireQuit(final WarMonitorProxy mon);

    public void requireTradeResp(final WarMonitorProxy mon, final MsgGroup mg);

    public void requestWar(final WarMonitorProxy mon, final MsgGroup mg);

    public void resultPwd(final MsgGroup mg);

    public void resultPlayerStatus(final MsgGroup mg);

    public void resultQuit(final MsgGroup mg);

    public void resultGameIds(final MsgGroup mg);

    public void resultRandomPlayerHp(final MsgGroup mg);

    public void resultPlayerHp(final MsgGroup mg);

}
