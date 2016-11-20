package org.psk.playground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

R { Expand amble (to list). }		return exp-amble with preamble appended to each item
R { Expand postamble (to list). }	result of above with postamble appended to each item
 * @author peter
 *
 */
public class BashCartesianProducer {
	public static final String SEPARATOR_CHAR = ",";
	public static void main(String[] args) {
		BashCartesianProducer p = new BashCartesianProducer();
		List<String> s = p.expand("a{b,c}d");
		System.out.println("TODO " + s);
	}
	// 
	/**
	 * Perform catesian product expansion on a string. 
	 * The example string we're using in comments is: ab{cd{e,f}gh}ij{k,l}mn
	 * We assume that all expandable input strings are well-formed i.e. have correct curlies.
	 * @param string
	 * @param sb
	 * @return
	 */
	private ArrayList<String> expand(String s) {

		int ambleIdx = findOpeningCurlyIdx(s);		
		int postAmbleIdx = findMatchingClosingCurlyIdx(s);	// this would be the ...h}i... one 
		
		/////////////////////////////
		// OK, first of all let's expand Amble if it contains curlies and a comma
		// Our example Amble would be cd{e,f}gh 
		/////////////////////////////
		// TODO everything wants to be an ArrayList cos of array reification rules (I'd rather pass List<>).
		ArrayList<String> ambleExpandResult = new ArrayList<String>();
		if (ambleIdx != -1) {	// no "amble" if no opening brackets
			String amb = s.substring(ambleIdx, postAmbleIdx);	// grab just the Amble text	cd{e,f}gh
			
			// If Amble itself has an Amble, recurse... Our example would recurse with {e,f}
			if (findOpeningCurlyIdx(amb) != -1) {
				ambleExpandResult.addAll(expand(amb));
			} else {
				// OK If we're here we know there isn't a sub Amble so split-out by the special char
				ambleExpandResult.addAll(Arrays.asList(amb.split(SEPARATOR_CHAR)));
			}
		}
		
		/////////////////////////////
		// Next we prepend PreAmble if its present
		/////////////////////////////
	    String preamble = s.substring(0, ambleIdx);  
	    ArrayList<String> preAmblePlusAmble = prependToEach(preamble, ambleExpandResult);
		    
	    // NO NEED to expand Postamble because we do it via recursion at the end.

		/////////////////////////////
		// OK so we have Amble and PostAmble. Postpend PostAmble.
		/////////////////////////////
		int idxPostAmbleEnd = findOpeningCurlyIdx(postAmbleIdx, s);
		if (idxPostAmbleEnd == -1) {
			// Great, no more expansion is necessary. Just postpend the postamble per element and return.
		    String postamble = s.substring(postAmbleIdx);  
		    return postpendToEach(postamble, preAmblePlusAmble);
		}
		
		/////////////////////////////
		// Right, so there is another chunk of curlies that need expanding after this chunk e.g. {k,l}mn in
	    // our example. So we need to recurse again.
		/////////////////////////////
		preAmblePlusAmble.addAll(expand(s.substring(idxPostAmbleEnd)));
	    return preAmblePlusAmble;
	}
	
	private ArrayList<String> prependToEach(String toAdd, ArrayList<String> l) {
		return concatToEach(toAdd, l, true);
	}
	
	private ArrayList<String> postpendToEach(String toAdd, ArrayList<String> l) {
		return concatToEach(toAdd, l, false);
	}
	
	
	private ArrayList<String> concatToEach(String toAdd, ArrayList<String> l, boolean pre) {
		// TODO use a cleaner approach to appending string per element
//		Stream<String> ss = expandedAmble.stream().map(s -> s + preamble);
//		String[] ls = ss.toArray(String[]::new);
		if (toAdd == null || toAdd.length() == 0)
			return l;
		
		ArrayList<String> retVal = new ArrayList<>();
		for (String s : l)
			retVal.add(pre ? toAdd + s : s + toAdd);
		
		return retVal;
	}

	private int findOpeningCurlyIdx(String s) {
		return s.substring(0).indexOf('{');
	}
	
	private int findOpeningCurlyIdx(int startIdx, String s) {
		return s.substring(startIdx).indexOf('{');
	}
	
	int findMatchingClosingCurlyIdx(String s) {
		return findClosingCurlyIdx(0, s);
	}
	
	/**
	 * Given a string (say ab{c,d{e,f}gh}ij, return the index of the ..h}i.. bracket i.e. the closing bracket
	 * that matches the first opening bracket.
	 * @param startIdx
	 * @param s
	 * @return
	 */
	public int findClosingCurlyIdx(int startIdx, String s) {
		int idxOpener = findOpeningCurlyIdx(startIdx, s);
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
