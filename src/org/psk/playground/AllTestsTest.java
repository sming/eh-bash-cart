package org.psk.playground;

import org.junit.Test;

public class AllTestsTest {

	@Test
	public void testFindClosingCurly() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,d}ef";
		org.junit.Assert.assertEquals(6, p.findMatchingClosingCurlyIdx(s));
	}

	@Test
	public void testFindClosingCurlyMissing() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,def";
		org.junit.Assert.assertEquals(-1, p.findMatchingClosingCurlyIdx(s));
	}

	@Test
	public void testFindClosingCurlyLast() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,d,e,f}";
		org.junit.Assert.assertEquals(10, p.findMatchingClosingCurlyIdx(s));
	}

	@Test
	public void testFindClosingCurlySub() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,d{e,f}g}hi";
		org.junit.Assert.assertEquals(12, p.findMatchingClosingCurlyIdx(s));
	}

	@Test
	public void testFindClosingCurlyMissingSub() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,d{e,f}ghi";
		org.junit.Assert.assertEquals(-1, p.findMatchingClosingCurlyIdx(s));
	}

	@Test
	public void testFindClosingCurlyLastSub() {
		BashCartesianProducer p = new BashCartesianProducer();
		String s = "ab{c,d{e,f}ghi}";
		org.junit.Assert.assertEquals(14, p.findMatchingClosingCurlyIdx(s));
	}
}
