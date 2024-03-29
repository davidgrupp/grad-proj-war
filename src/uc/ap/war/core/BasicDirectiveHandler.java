package uc.ap.war.core;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import uc.ap.war.core.ex.NoPlayerIdException;
import uc.ap.war.core.ex.SecurityServiceException;
import uc.ap.war.core.model.WarModelManager;
import uc.ap.war.core.model.WarPlayer;
import uc.ap.war.core.protocol.DirectiveHelper;

public class BasicDirectiveHandler implements DirectiveHandler {
    private static Logger log = Logger.getLogger(BasicDirectiveHandler.class);

    @Override
    public void requireAlive(final WarMonitorProxy mon) {
        mon.cmdAlive();
    }

    @Override
    public void requireHostPort(final WarMonitorProxy mon) {
        mon.cmdHostPort();
    }

    @Override
    public void requireIdent(final WarMonitorProxy mon)
            throws NoPlayerIdException, SecurityServiceException {
        mon.cmdIdent();
    }

    @Override
    public void requirePwd(final WarMonitorProxy mon) {
        mon.cmdPwd();
    }

    @Override
    public void requireQuit(final WarMonitorProxy mon) {
        mon.cmdQuit();
    }

    @Override
    public void requireTradeResp(final WarMonitorProxy mon, final MsgGroup mg) {
    }

    @Override
    public void requireTruceResp(final WarMonitorProxy mon, final MsgGroup mg) {

    }

    @Override
    public void requireWarDefend(final WarMonitorProxy mon, final MsgGroup mg) {

    }

    @Override
    public void resultChanePwd(MsgGroup mg) throws NoPlayerIdException,
            IOException {
        log.debug("got new monitor cookie: " + mg.getResultStr());
        WarPlayer.ins().setCookie(mg.getResultStr());
        WarPlayer.ins().activateNewPwd();
        WarModelManager.storePlayer();
    }

    @Override
    public void resultCrackCookie(final MsgGroup mg) {

    }

    @Override
    public void resultCrackHostPort(final MsgGroup mg) {
        log.debug(mg.getResult());
    }

    @Override
    public void resultCrackStatus(MsgGroup mg) {

    }

    @Override
    public void resultGameIds(final MsgGroup mg) {
        log.debug(mg.getResult());
    }

    @Override
    public void resultMakeCert(final MsgGroup mg) {
        log.debug(mg.getResult());
    }

    @Override
    public void resultPlayerStatus(final MsgGroup mg) {
        log.debug(mg.getResult());
        final HashMap<String, Integer> res = DirectiveHelper
                .parsePlayerStatResources(mg.getResultStr());
        WarPlayer.ins().updateResources(res);
    }

    @Override
    public void resultPwd(final MsgGroup mg) throws NoPlayerIdException,
            IOException {
        log.debug("got monitor cookie: " + mg.getResultStr());
        WarPlayer.ins().setCookie(mg.getResultStr());
        WarModelManager.storePlayer();
    }

    @Override
    public void resultQuit(final MsgGroup mg) {
        log.debug(mg.getResult());
    }

    @Override
    public void resultRandomHostPort(final MsgGroup mg) {
        log.debug(mg.getResult());
    }

}
