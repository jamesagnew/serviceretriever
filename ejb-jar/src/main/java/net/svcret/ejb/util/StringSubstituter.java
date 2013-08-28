/*
 * Created on Dec 3, 2007
 */
package net.svcret.ejb.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Searches through a string, and searches for any strings and replaces them with the appropriate
 * substitutions
 * 
 * <p>
 * <b>Thread Safety:</b> This class IS thread safe
 * </p>
 * 
 * <p>
 * <b>Warning about Regex Use:</b> This class uses regex internally to perform matchings. Target
 * strings (the strings being searched for, or in other words the keys in the substitution maps) may
 * contain basic regex, but if they use advanced regex indeterminate behaviour may occur. Also take
 * note that any PARENS (shift-9 and shift-0) will be automatically escaped by the
 * StringSubstituter, to they will never be treated as regex groupings by the substituter
 * </p>
 * <p>
 * The following regex usage is known to work in target strings:
 * <ul>
 * <li>. (the dot character)
 * <li>* (the star character)
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:james.agnew@uhn.on.ca">James Agnew</a>
 * @version $Revision: 79 $ updated on $Date: 2008-06-02 12:46:51 -0400 (Mon, 02 Jun 2008) $ by $Author: jagnew $
 */
public class StringSubstituter
{
    private static final Log ourLog = LogFactory.getLog(StringSubstituter.class);

    private Pattern myPattern;
    private Map<String, String> mySubstitutions;
    private Map<String, Pattern> myTargetToMatcher;
    private boolean myCaseInsensitive;


    /**
     * Creates a new string substituter
     * 
     * @param theSubstitutions
     *            The string substitutions. When this substitutrer runs, the substitutions in the
     *            map will be applied, with keys representing target strings to search for, and
     *            values representing replacement strings to replace them with. Note: See warning in
     *            class documentation about regex use
     * @param theWholeWord
     *            If true, the target string will be assumed to be whole words, and a word boundary
     *            is searched for before and after each target string
     * @param theCaseInsensitive
     *            If true, target strings are considered case insensitive
     */
    public StringSubstituter(Map<String, String> theSubstitutions, boolean theWholeWord, boolean theCaseInsensitive) {

        myCaseInsensitive = theCaseInsensitive;

        HashMap<String, String> newSubstitutions = new HashMap<String, String>();
        mySubstitutions = new HashMap<String, String>();

        Set<Entry<String, String>> entrySet = theSubstitutions.entrySet();
        for (Entry<String, String> entry : entrySet) {
            String key = entry.getKey();
            if (theCaseInsensitive) {
                key = key.toLowerCase();
            }
            String value = entry.getValue();

            mySubstitutions.put(key, value);

            key = key.replaceAll("\\(", "\\\\(");
            key = key.replaceAll("\\)", "\\\\)");

            newSubstitutions.put(key, value);
        }

        StringBuilder patternBuffer = new StringBuilder();
        for (String string : newSubstitutions.keySet()) {
            if (patternBuffer.length() > 0) {
                patternBuffer.append("|");
            }
            patternBuffer.append(string);
        }

        if (theWholeWord) {
            patternBuffer.insert(0, "\\b");
            patternBuffer.append("\\b");
        }

        int flags = 0;
        if (theCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        myPattern = Pattern.compile(patternBuffer.toString(), flags);

        myTargetToMatcher = new HashMap<String, Pattern>();
        for (String nextTarget : theSubstitutions.keySet()) {
            String targetToMatcherKey = theCaseInsensitive ? nextTarget.toLowerCase() : nextTarget;
            myTargetToMatcher.put(targetToMatcherKey, Pattern.compile(nextTarget, flags));
        }

    }


    /**
     * @param theInputString
     *            The string to applye the substitutions to
     * @return Returns true if any substitutions were performed
     */
    public boolean apply(StringBuffer theInputString) {

        Matcher matcher = myPattern.matcher(theInputString);
        int startIndex = 0;
        boolean retVal = false;
        while (matcher.find(startIndex)) {

            retVal = true;
            int start = matcher.start();
            int end = matcher.end();
            String subString = theInputString.substring(start, end);

            String replacement;
            if (myCaseInsensitive) {
                replacement = mySubstitutions.get(subString.toLowerCase());
            } else {
                replacement = mySubstitutions.get(subString);
            }

            if (replacement == null) {
                for (Map.Entry<String, Pattern> next : myTargetToMatcher.entrySet()) {
                    if (next.getValue().matcher(subString).matches()) {
                        String key = next.getKey();
                        replacement = mySubstitutions.get(key);
                        break;
                    }
                }
            }

            if (replacement != null) {
                // This is the expected case. We found a match
                replacement = MessageFormat.format(replacement, subString);
                theInputString.replace(start, end, replacement);
                startIndex = start + replacement.length();
            } else {
                ourLog.error("Error: Couldn't find match for string \"" + subString + "\"");
                startIndex += subString.length();
            }

        }

        return retVal;
    }


    /**
     * @param theInput
     *            The input string
     * @return The output string
     */
    public String apply(String theInput) {
        StringBuffer buf = new StringBuffer(theInput);
        if (apply(buf)) {
            return buf.toString();
        } else {
            return theInput;
        }
    }

}
