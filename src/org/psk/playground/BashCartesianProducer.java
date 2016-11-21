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


HIGH-LEVEL DESIGN -------------------------------------
Segregate the text into 3 sections: 
Preamble (stuff before the first open brace),   
Postamble (stuff after the matching closing brace) and 
Amble (stuff after preamble, and before postamble).  

Once you've done that, 
recursively expand the Amble since it may contain curlies & commas and thus sub-Ambles.
Next, prepend the Preamble to each element of the expanded Amble.
Then if Postamble doesn't need expanding, just postpend it to each element. Else, recurse-expand it and
append the results.
Then space-separated print out result.

PERFORMANCE -------------------------------------
Assuming reasonable input, the recursion won't present a threat to the stack - it's just not that deep. 
Add'ing to ArrayList is relatively cheap. Could use StringBuilders but then you're working with more mutable
objects which makes it more difficult and less flexible.
Order of complexity I'm not sure about. It depends on the number of sequential expandable sequences and the
depth of the recursion into sub-sequences (or "Amble"s). 
Something like 
O(N) = N^M where N is the number of elements in a contiguous sequence (non-expanded) and M is the Max of 
(max depth of the sub-expansions OR the max number of sequential expansions).    

TODO -------------------------------------
- decompose into a couple more classes
- write a bucketload of unit tests
- logging
- make more SOLID ((single responsibility, open-closed, Liskov substitution, interface segregation and 
dependency inversion) e.g.
	- single responsibility: decompose this big class
	- open-closed: add extension points for specialisation
	- Liskov substitution: ensure we obey this TRULY IS A maxim
	- interface segregation: doesn't really apply, the interface is tiny
	- and dependency inversion: don't new-up ArrayLists but allow a List<String> factory to be injected
 */
public class BashCartesianProducer {
	public static final String SEPARATOR_CHAR = ",";
	private static final ArrayList<String> EMPTY_STRING_LIST = new ArrayList<String>();
	
	public static void main(String[] args) {
		BashCartesianProducer p = new BashCartesianProducer();
		List<String> s = p.expand("a{b,c}d");
		String output = p.render(s);
		System.out.println("TODO " + output);
	}
	
	static {
		EMPTY_STRING_LIST.add("");
	}
	
	public String render(List<String> s) {
		return String.join(" ", s);
	}
	
	public String render(String s) {
		return String.join(" ", expand(s));
	}
	
	/**
	 * expand(s)
	 * 
	 * Perform Bash shell catesian product expansion on a string. 
	 * 
	 * We do assume that all expandable input strings have correctly balanced curlies i.e. a { will have a }.
	 * 
	 * Examples:
	 * $ echo a{b,c}d{e,f,g}hi
	 * abdehi abdfhi abdghi acdehi acdfhi acdghi
	 * $ echo a{b,c{d,e,f}g,h}ij{k,l}
	 * abijk abijl acdgijk acdgijl acegijk acegijl acfgijk acfgijl ahijk ahijl
	 *
	 * So from the above we can see that there can be multiple sequential expansions e.g. ..{a,b}..{y,z}.. and
	 * also sub-expansions e.g. ..{a,b{c,d}e}.., both of which cases we handle via recursion. This is described
	 * above in the class comment.
	 * 
	 * !!! The example string we're using in comments throughout the implementation is: ab{cd{e,f}gh}ij{k,l}mn
	 * @param s the string to expand into a List<String.
	 * @param sb
	 * @return
	 */
	public ArrayList<String> expand(String s) {
		if (s == null || s.isEmpty())
			return EMPTY_STRING_LIST;
		
		int ambleIdx = findOpeningCurlyIdx(s);		
		int postAmbleIdx = findPostAmbleStartIdx(s);	// this would be the ...h}i... one 
		
		/////////////////////////////
		// OK, first of all let's expand Amble if it contains curlies and a comma
		// Our example Amble would be cd{e,f}gh 
		/////////////////////////////
		// TODO everything wants to be an ArrayList cos of array reification rules (I'd rather pass List<>).
		ArrayList<String> ambleExpandResult = null;
		if (ambleIdx != -1) {	// no "amble" if no opening brackets
			String amb = s.substring(ambleIdx+1, postAmbleIdx-1);	// grab just the Amble text	cd{e,f}gh
			
			// If Amble itself has an Amble, recurse... Our example would recurse with {e,f}
			if (findOpeningCurlyIdx(amb) != -1) {
				ambleExpandResult = expand(amb);
			} else {
				// OK If we're here we know there isn't a sub Amble so split-out by the special char
				ambleExpandResult = toList(amb.split(SEPARATOR_CHAR));
			}
		} else {
			// I *think* we can return now cos there are no curlies at all.
			// So there wasn't an "Amble" (i.e. curlies) so just add the text. TODO this doesn't feel right.
			return toList(s);
		}
		
		/////////////////////////////
		// Next we prepend PreAmble if it's present
		// We we know Preamble doesn't need expanding since it does not contain an open curly by definition.
		// So the only multiple here is Amble. 
		/////////////////////////////
	    String preamble = s.substring(0, ambleIdx);  
	    ArrayList<String> preAmblePlusAmble = prependToEach(ambleExpandResult, preamble);
		    
	    // NO NEED to expand Postamble because we do it via recursion at the end.

		/////////////////////////////
		// OK so we have Amble and PostAmble. Postpend PostAmble. If there aren't any more 
	    // expandable parts (i.e. any more open curlies) then just append and we're done here.  
		/////////////////////////////
		if (!hasOpeningCurly(postAmbleIdx, s)) {
			// Great, no more expansion is necessary i.e. we didn't find another open curly.
			// Just postpend the postamble per element and return.
		    String postamble = s.substring(postAmbleIdx);
		    return postpendToEach(preAmblePlusAmble, toList(postamble));
		}
		
		/////////////////////////////
		// Right, so there is another chunk of curlies that need expanding after this chunk 
		// e.g. {k,l}mn in our example. So we need to recurse again on just that bit of text - which 
		// is the substring(findOpening.. bit.
		/////////////////////////////
		return postpendToEach(preAmblePlusAmble, expand(s.substring(findOpeningCurlyIdx(postAmbleIdx, s, true))));
	}
	
	private ArrayList<String> prependToEach(ArrayList<String> l, String toAdd) {
		return concatToEach(l, toList(toAdd), true);
	}

	@Deprecated	// remove if unused
	private ArrayList<String> postpendToEach(ArrayList<String> l, String toAdd) {
		return concatToEach(l, toList(toAdd), false);
	}

	private ArrayList<String> postpendToEach(ArrayList<String> elements, ArrayList<String> toAdd) {
		return concatToEach(elements, toAdd, false);
	}
	
	@Deprecated	// remove if unused
	private ArrayList<String> prependToEach(ArrayList<String> elements, ArrayList<String> toAdd) {
		return concatToEach(elements, toAdd, true);
	}
	
	private ArrayList<String> concatToEach(ArrayList<String> elements, ArrayList<String> toAdd, boolean pre) {
		ArrayList<String> result = new ArrayList<>();
		if (toAdd == null || toAdd.isEmpty())
			return result;
		
		// TODO use map or some other builtin function to make the product
		for (String e : elements)
			for (String t : toAdd) 
				result.add(pre ? t + e : e + t);
		
		return result;
	}
	
	private int findOpeningCurlyIdx(String s) {
		return s.substring(0).indexOf('{');
	}
	
	private boolean hasOpeningCurly(int startIdx, String s) {
		return findOpeningCurlyIdx(startIdx, s, false) != -1;
	}
	
	private int findOpeningCurlyIdx(int startIdx, String s, boolean addStartIdx) {
		// TODO add startIdx?
		int idx = s.substring(startIdx).indexOf('{');
		return idx + (addStartIdx ? startIdx : 0);
	}
	
	int findMatchingClosingCurlyIdx(String s) {
		return findClosingCurlyIdx(0, s);
	}
	
	int findPostAmbleStartIdx(String s) {
		int idx = findMatchingClosingCurlyIdx(s);
		return idx == -1 ? -1 : idx + 1;
	}
	
	/**
	 * Given a string (say ab{c,d{e,f}gh}ij, return the index of the ..h}i.. bracket i.e. the closing bracket
	 * that matches the first opening bracket.
	 * @param startIdx
	 * @param s
	 * @return
	 */
	public int findClosingCurlyIdx(int startIdx, String s) {
		int idxOpener = findOpeningCurlyIdx(startIdx, s, false);
		if (idxOpener == -1)
			return -1;
		
		int i = idxOpener + 1;	// don't count the opener again
		int openCount = 1;		// we've found the first opener
		while (openCount > 0 && i < s.length()) {
			if (s.charAt(i) == '{')
				++openCount;
			else if (s.charAt(i) == '}')
				--openCount;
			
			if (openCount > 0)
				++i;	// only advance i if we haven't found it. TODO find way of having only one test on openCount
		}
		
		if (openCount > 0 && i >= s.length())
			return -1;		// we didn't find a closing curly - malformed string
		else
			return i;
	}
	
	private ArrayList<String> toList(String s) {
		return new ArrayList<String>(Arrays.asList(s));
	}

	private ArrayList<String> toList(String[] s) {
		return new ArrayList<String>(Arrays.asList(s));
	}
}
