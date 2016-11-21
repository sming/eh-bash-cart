package org.psk.playground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
For example:
$ echo a{b,c}d{e,f,g}hi
abdehi abdfhi abdghi acdehi acdfhi acdghi
$ echo a{b,c{d,e,f}g,h}ij{k,l}
abijk abijl acdgijk acdgijl acegijk acegijl acfgijk acfgijl ahijk ahijl


HIGH-LEVEL DESIGN -------------------------------------
Confession: I pinched this approach from the bash source code! However that code is really obtuse and performance-obsessed and 
written in C so the only thing that I got was the concept, via the comments. Honest!

Segregate the text into 3 sections: 
Preamble (stuff before the first open curly),   
Postamble (stuff after the matching closing curly) and 
Amble (stuff after preamble, and before postamble).  

Once you've done that, recursively expand the three sections Amble since they may contain curlies & commas 
and thus sub-Ambles.
Next, prepend the Preamble to each element of the expanded Amble.
Then if Postamble doesn't need expanding, just postpend it to each element. Else, recurse-expand it and
append the results.
Then space-separated print out result.

COMMENTARY -------------------------------------
This implementation is not clean. I believe that the three sections can be handled by the same recursive block of logic,
rather than explicitly handling each. It also does not quite work with sub-expansion e.g. a{b,c{d,e}f}g. It does work with
sequential expansion e.g. a{b,c}d{e,f}g. 
A tricky part is detecting that commas are present inside the "current" sub string. Not that easy with sub-expansion going on. 
Again, I believe there's a clean, short solution to that too.
I'd also like to decompose expand() further.  

Also I'm not sure why I didn't look into using regexp's. It would be a gnarly one (recursion etc.) but would save a lot of code
and probably be fast to execute once compiled (the regexp, not the code).

RUNTIME PERFORMANCE -------------------------------------
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

	/**
	 * Example usage of this class
	 * @param args
	 */
	public static void main(String[] args) {
		BashCartesianProducer p = new BashCartesianProducer();
		String t = "a{b,c}d";
		List<String> s = p.expand(t);
		String output = p.render(s);
		System.out.println("Cartesian product of " + t + ": " + output);
	}
	
	static {
		EMPTY_STRING_LIST.add("");
	}
	
	/**
	 * Return a space-concatenated string of a list of strings
	 */
	public String render(List<String> s) {
		return String.join(" ", s);
	}
	
	/**
	 * Return a space-concatenated string of the bash cartesian product of a string 
	 */
	public String render(String s) {
		return String.join(" ", expand(s));
	}
	
	/**
	 * expand(s)
	 * 
	 * Perform Bash shell catesian product expansion on a string. 
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
	 * NOTE The example string we're using in comments throughout this method is: ab{cd{e,f}gh}ij{k,l}mn
	 * @param s the string to expand into a List<String> bash cartesian product.
	 * @return cartesian product of s as a list of strings
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
		ArrayList<String> ambleExpandResult = new ArrayList<>();
		if (ambleIdx != -1 && postAmbleIdx != -1) {	// no "amble" if no opening brackets with matching close
			addExpandedSubstring(s, ambleIdx, postAmbleIdx, ambleExpandResult);
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
	    ArrayList<String> preAmblePlusAmble = prependToEach(ambleExpandResult, expand(preamble));

		/////////////////////////////
		// OK so we have Amble and PostAmble. Postpend the static part of PostAmble if there is any.
	    // If there isn't any expandable ambles in the postamble, this static part is all the way
	    // until the end of the string.
		/////////////////////////////
	    int idxFirstOpenCurlyPostamble = findOpeningCurlyIdx(postAmbleIdx, s, true);
	    boolean postambleHasCurly = idxFirstOpenCurlyPostamble != -1;
	    int idxEndStaticPostamble = findEndStaticPostAmble(s, idxFirstOpenCurlyPostamble, postambleHasCurly);
	    
	    preAmblePlusAmble = postpendToEach(
	    		preAmblePlusAmble, s.substring(postAmbleIdx, idxEndStaticPostamble + 1));

		/////////////////////////////
	    // Then prepend everything so far to any expandable remainder of the PostAmble.
		/////////////////////////////
	    if (postambleHasCurly)
	    	return prependToEach(expand(s.substring(idxFirstOpenCurlyPostamble)), preAmblePlusAmble);
	    else
	    	return preAmblePlusAmble;
	}

	/**
	 * So if you have a{b,c}de{f,g} and you were starting off at idx 0, this would return idx of e - the last non-expandable char
	 * of the current expandable section. Phew.
	 * @param s
	 * @param idxFirstOpenCurlyPostamble
	 * @param postambleHasCurly
	 * @return
	 */
	private int findEndStaticPostAmble(String s, int idxFirstOpenCurlyPostamble, boolean postambleHasCurly) {
		int idxEndStaticPostamble = 0;
	    
	    if (postambleHasCurly)
	    	idxEndStaticPostamble = idxFirstOpenCurlyPostamble - 1;
	    else
	    	idxEndStaticPostamble = s.length() - 1;
		return idxEndStaticPostamble;
	}

	/**
	 * Expand the substring and add it to the collection passed in
	 */
	private void addExpandedSubstring(String s, int ambleIdx, int postAmbleIdx, ArrayList<String> ambleExpandResult) {
		String amb = s.substring(ambleIdx+1, postAmbleIdx-1);	// grab just the Amble text	cd{e,f}gh
		
		// If Amble itself has an Amble, recurse... Our example would recurse with {e,f}
		if (findOpeningCurlyIdx(amb) != -1) {
			ambleExpandResult.addAll(expand(amb));
		} else {
			// OK If we're here we know there isn't a sub Amble so split-out by the special char
			// TODO potentially don't split on inner Ambles' commas
			// TODO add logic to detect when commas are not present in the curly section cos then it doesn't need expansion
			ambleExpandResult.addAll(toList(amb.split(SEPARATOR_CHAR)));
		}
	}
	
	private ArrayList<String> postpendToEach(List<String> elements, String toAdd) {
		return concatToEach(elements, toList(toAdd), false);
	}
	
	private ArrayList<String> prependToEach(List<String> elements, List<String> toAdd) {
		return concatToEach(elements, toAdd, true);
	}

	/**
	 * Prepend or postpend a list of strings to the elements of another list of strings
	 * @param elements list to be appended to
	 * @param toAdd strings to append to elements
	 * @param pre true == prepend, false == postpend
	 * @return resulting new list
	 */
	private ArrayList<String> concatToEach(List<String> elements, List<String> toAdd, boolean pre) {
		ArrayList<String> result = new ArrayList<>();
		if (toAdd == null || toAdd.isEmpty())
			return result;
		
		// TODO use streams map or some other J8 builtin function to make the product
		for (String t : toAdd) 
			for (String e : elements)
				result.add(pre ? t + e : e + t);
		
		return result;
	}
	
	private int findOpeningCurlyIdx(String s) {
		return s.substring(0).indexOf('{');
	}

	/**
	 * Finds index of first opening curly or -1 if not present
	 * @param startIdx where to start looking from
	 * @param s target string
	 * @param addStartIdx true == append start index to result, false == don't
	 * @return idx of first open curly
	 */
	private int findOpeningCurlyIdx(int startIdx, String s, boolean addStartIdx) {
		// TODO add startIdx?
		int idx = s.substring(startIdx).indexOf('{');
		if (idx == -1)
			return -1;
		
		return idx + (addStartIdx ? startIdx : 0);
	}
	
	int findMatchingClosingCurlyIdx(String s) {
		return findMatchingClosingCurlyIdx(0, s);
	}
	
	int findPostAmbleStartIdx(String s) {
		int idx = findMatchingClosingCurlyIdx(s);
		return idx == -1 ? -1 : idx + 1;
	}
	
	/**
	 * Given a string (say ab{c,d{e,f}gh}ij, return the index of the ..h}i.. bracket i.e. the closing bracket
	 * that matches the first opening bracket. 
	 * @param startIdx where to start searching from
	 * @param s target string
	 * @return as above.
	 */
	public int findMatchingClosingCurlyIdx(int startIdx, String s) {
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
