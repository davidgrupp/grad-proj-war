package uc.ap.war.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MsgGroupTest {

    @Test
    public void testMsgIdent() {
        MsgGroup mg = new MsgGroup();
        mg.addMsg("COMMENT: Game Monitor Version 2.2");
        mg.addMsg("PLAYER_PASSWORD_CHECKSUM:  bb570974bc673613c278e60a0a6843f3e4b17dbc");
        mg.addMsg("REQUIRE: IDENT");
        mg.addMsg("WAITING:");
        assertEquals(mg.getResultArg(), "");
        assertEquals(mg.getRequiredCmd(), "IDENT");
        assertEquals(mg.getCmdError(), "");
        assertEquals(mg.getPwCheckSum(), "bb570974bc673613c278e60a0a6843f3e4b17dbc");
    }

    @Test
    public void testMsgPassword() {
        MsgGroup mg = new MsgGroup();
        mg.addMsg("RESULT: IDENT");
        mg.addMsg("REQUIRE: PASSWORD");
        mg.addMsg("WAITING:");
        assertEquals(mg.getResultArg(), "IDENT");
        assertEquals(mg.getRequiredCmd(), "PASSWORD");
        assertEquals(mg.getCmdError(), "");
    }

    @Test
    public void testMsgPasswordResult() {
        MsgGroup mg = new MsgGroup();
        mg.addMsg("RESULT: PASSWORD 3IC1S9KV2KBCUXZA8RU");
        mg.addMsg("REQUIRE: HOST_PORT");
        mg.addMsg("WAITING:");
        assertEquals(mg.getResultArg(), "PASSWORD");
        assertEquals(mg.getResultStr(), "3IC1S9KV2KBCUXZA8RU");
        assertEquals(mg.getRequiredCmd(), "HOST_PORT");
        assertEquals(mg.getCmdError(), "");
    }

    @Test
    public void testMsgHostPort() {
        MsgGroup mg = new MsgGroup();
        mg.addMsg("RESULT: Whatever you like");
        mg.addMsg("REQUIRE: HOST_PORT");
        mg.addMsg("WAITING:");
        mg.addMsg("RESULT: UNEXPECTED result again");
        assertEquals(mg.getResultArg(), "Whatever");
        assertEquals(mg.getResultStr(), "you like");
        assertEquals(mg.getRequiredCmd(), "HOST_PORT");
        assertEquals(mg.getCmdError(), "");
    }

    @Test
    public void testMsgEndOfTransaction() {
        MsgGroup mg = new MsgGroup();
        mg.addMsg("RESULT: QUIT");
        mg.addMsg("REQUIRE: HOST_PORT");
        mg.addMsg("WAITING:");
        assertEquals(mg.getResultArg(), "QUIT");
        assertEquals(mg.getResultStr(), "");
        assertEquals(mg.getRequiredCmd(), "");
    }
}
