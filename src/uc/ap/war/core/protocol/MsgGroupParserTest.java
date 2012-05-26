package uc.ap.war.core.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class MsgGroupParserTest {

	@Test
	public void testMsgIdent() {
		final String s = "COMMENT: Game Monitor Version 2.2\nREQUIRE: IDENT\nWAITING:\n";
		final StringReader reader = new StringReader(s);
		try {
			MsgGroupParser mgp = new MsgGroupParser(reader);
			MsgGroup mg = mgp.next();
			assertEquals(mg.getResultArg(), "");
			assertEquals(mg.getRequiredCmd(), "IDENT");
			assertEquals(mg.getCmdError(), "");
			assertEquals(s, mg.toString());
		} catch (IOException e) {
			fail("IO Exception" + e);
		}
	}

	@Test
	public void testMsgPassword() {
		final String s = "RESULT: IDENT\nREQUIRE: PASSWORD\nWAITING:\n";
		final StringReader reader = new StringReader(s);
		try {
			MsgGroupParser mgp = new MsgGroupParser(reader);
			MsgGroup mg = mgp.next();
			assertEquals(mg.getResultArg(), "IDENT");
			assertEquals(mg.getRequiredCmd(), "PASSWORD");
			assertEquals(mg.getCmdError(), "");
			assertEquals(s, mg.toString());
		} catch (IOException e) {
			fail("IO Exception" + e);
		}
	}
	

	@Test
	public void testMsgPasswordResult() {
		final String s = "RESULT: PASSWORD 3IC1S9KV2KBCUXZA8RU\nREQUIRE: HOST_PORT\nWAITING:\n";
		final StringReader reader = new StringReader(s);
		try {
			MsgGroupParser mgp = new MsgGroupParser(reader);
			MsgGroup mg = mgp.next();
			assertEquals(mg.getResultArg(), "PASSWORD");
			assertEquals(mg.getResultStr(), "3IC1S9KV2KBCUXZA8RU");
			assertEquals(mg.getRequiredCmd(), "HOST_PORT");
			assertEquals(mg.getCmdError(), "");
			assertEquals(s, mg.toString());
		} catch (IOException e) {
			fail("IO Exception" + e);
		}
	}

	@Test
	public void testMsgHostPort() {
		final String s1 = "RESULT: Whatever you like\nREQUIRE: HOST_PORT\nWAITING:\n";
		final String s2 = "RESULT: QUIT\n";
		final StringReader reader = new StringReader(s1 + s2);
		try {
			MsgGroupParser mgp = new MsgGroupParser(reader);
			MsgGroup mg = mgp.next();
			assertEquals(mg.getResultArg(), "Whatever");
			assertEquals(mg.getResultStr(), "you like");
			assertEquals(mg.getRequiredCmd(), "HOST_PORT");
			assertEquals(mg.getCmdError(), "");
			assertEquals(s1, mg.toString());
			mg = mgp.next();
			assertEquals(mg, null);
		} catch (IOException e) {
			fail("IO Exception" + e);
		}
	}

	@Test
	public void testMsgEndOfTransaction() {
		final String s = "RESULT: QUIT\nREQUIRE: HOST_PORT\nWAITING:";
		final StringReader reader = new StringReader(s);
		try {
			MsgGroupParser mgp = new MsgGroupParser(reader);
			MsgGroup mg = mgp.next();
			assertEquals(mg, null);
		} catch (IOException e) {
			fail("IO Exception" + e);
		}
	}
}
