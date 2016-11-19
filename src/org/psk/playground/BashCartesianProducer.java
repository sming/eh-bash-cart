package org.psk.playground;

import java.util.List;

/**
 * 
For example:
$ echo a{b,c}d{e,f,g}hi
abdehi abdfhi abdghi acdehi acdfhi acdghi
$ echo a{b,c{d,e,f}g,h}ij{k,l}
abijk abijl acdgijk acdgijl acegijk acegijl acfgijk acfgijl ahijk ahijl

 * 
Segregate the text into 3 sections: 
preamble (stuff before an open brace),   
postamble (stuff after the matching close brace) and 
amble (stuff after preamble, and before postamble).  
   
(* R ~ recursively)

R-{1. Expand amble. 2. return preamble + exp-amble.}
R-{1. Expand postamble. 2. result so far + exp-post}
 * @author peter
 *
 */
public class BashCartesianProducer {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BashCartesianProducer p = new BashCartesianProducer();
		String s = p.produce("a{b,c}d");
		System.out.println("TODO " + s);
	}

	private String produce(String string) {
		StringBuilder sb = new StringBuilder();
		//TODO implement
		//return getAmble(0, 0, sb, string);
		return "";
	}

	// 
	/**
	 * take a string like ab{c,d{e,f}g}h 
	 * and return c,d{e,f}g - everything inside the first curly pair. 
	 * Note that you can't just search backward from the end for a closing curly e.g. a{b}c{d}
	 * @param string
	 * @param sb
	 * @return
	 */
	private int getAmble(String s, char c, StringBuilder sb) {//(int i, int numOpenBraces, StringBuilder sb, String string) {
		int firstIdx = s.indexOf(c);
		if (firstIdx == -1)
			return firstIdx;
		
		int closingIdx = findClosingCurlyIdx(s);
		
		//TODO implement
		return 0;
	}
	
	public int findOpeningCurly(String s) {
		return s.indexOf('{');
	}
	
	public int findClosingCurlyIdx(String s) {
		int idxOpener = findOpeningCurly(s);
		if (idxOpener == -1)
			return -1;
		
		int i = idxOpener + 1;	// don't count the opener again
		int openCount = 1;		// we've found the first opener
		while (openCount > 0 && i < s.length()) {
			if (s.charAt(i) == '{')
				++openCount;
			else if (s.charAt(i) == '}')
				--openCount;
			
			++i;
		}
		
		if (openCount > 0 && i >= s.length())
			return -1;		// we didn't find a closing curly - malformed string
		else
			return i - 1;
	}
	
}
