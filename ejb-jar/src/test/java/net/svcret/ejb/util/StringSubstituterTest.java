/*
 * Created on Dec 4, 2007
 */
package net.svcret.ejb.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * -
 */
public class StringSubstituterTest extends TestCase
{

    /**
     * -
     */
    public void testNormalString() {
        
        Map<String, String> subs = new HashMap<String, String>();
        subs.put("TEST1", "THENEWSUB1");
        subs.put("test2", "THENEWSUB2({0})");
        StringSubstituter substituter = new StringSubstituter(subs, true, true);

        // Try normal string
        String input = "the TEST1 and TEST2";
        String expected = "the THENEWSUB1 and THENEWSUB2(TEST2)";
        StringBuffer actualBuffer = new StringBuffer(input);
        substituter.apply(actualBuffer);
        String actual = actualBuffer.toString();
        assertEquals(expected, actual);
        
    }

    
    /**
     * -
     */
    public void testWithRegex() {
        
        Map<String, String> subs = new HashMap<String, String>();
        subs.put("T.*T", "NEW");
        
        StringSubstituter substituter = new StringSubstituter(subs, false, true);

        StringBuffer input = new StringBuffer("AAAATOOOTAAAA");
        substituter.apply(input);
        
        String expected = "AAAANEWAAAA";
        String actual = input.toString();
        
        assertEquals(expected, actual);
    }

    
    /**
     * -
     */
    public void testWithParens() {
        
        Map<String, String> subs = new HashMap<String, String>();
        subs.put("HELLO(GOODBYE)HELLO", "NEW");
        StringSubstituter substituter = new StringSubstituter(subs, false, true);

        StringBuffer input = new StringBuffer("ACKHELLO(GOODBYE)HELLOACK");
        substituter.apply(input);
        
        String expected = "ACKNEWACK";
        String actual = input.toString();
        
        assertEquals(expected, actual);
    }
    
    
    /**
     * -
     */
    public void testWithAmpersand() {
        
        Map<String, String> subs = new HashMap<String, String>();
        subs.put("&amp;", "&");
        StringSubstituter substituter = new StringSubstituter(subs, false, true);

        StringBuffer input = new StringBuffer("MSH|&amp;AAAAA");
        substituter.apply(input);
        
        String expected = "MSH|&AAAAA";
        String actual = input.toString();
        
        assertEquals(expected, actual);
    }
    
}
