package org.psk.playground;

import org.junit.Test;

public class ExpandTest {

	@Test
	public void testExpandEmptyCurlies() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "abc{}def";
		org.junit.Assert.assertEquals("abcdef", p.render(s));
	}

	@Test
	public void testExpandNoCommaCurlies() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{cd}ef";
		org.junit.Assert.assertEquals("ab{cd}ef", p.render(s));
	}

	@Test
	public void testExpandSimpleCurlies() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,d}ef";
		org.junit.Assert.assertEquals("abcef abdef", p.render(s));
	}

	@Test
	public void testExpandSimpleCurlies2() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{cd,efg}hi";
		org.junit.Assert.assertEquals("abcdhi abefghi", p.render(s));
	}
}
