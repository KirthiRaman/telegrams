package com.jobquest.process;

public interface LineDataProcess {
/**
 * Processes a line of data from the large data file
 * by following a specific algorithm.
 * @param s1 first string to be used by the computation process
 * @return String value of the result
 *
 * One example implementation is - treat single line as an expression that
 *                                 needs to be evaluated and convert result to 
 *                                 contain <input string> = <output value>
 *     another implementation could be - a bag of words
 *    Later if someone wants to use the same framework for a different process
 *          they can implement another algorithm and use that in InputQueueProcess
 *          
 */

 public String processLine(String s1);
}

