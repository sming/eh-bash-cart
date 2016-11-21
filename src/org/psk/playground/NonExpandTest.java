package org.psk.playground;

import org.junit.Test;

/**
 * Tests that exercise that the logic does not incorrectly expand non-curly-wrapped text
 */
public class NonExpandTest {

	@Test
	public void testExpandPlainText() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "abcdef";
		org.junit.Assert.assertEquals("abcdef", p.render(s));
	}

	@Test
	public void testExpandEmptyText() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "";
		org.junit.Assert.assertEquals("", p.render(s));
	}

	@Test
	public void testExpandCommaText() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "abc,d,ef";
		org.junit.Assert.assertEquals("abc,d,ef", p.render(s));
	}

	@Test
	public void testExpandCommaText2() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "abc,,,d,ef";
		org.junit.Assert.assertEquals("abc,,,d,ef", p.render(s));
	}
}
