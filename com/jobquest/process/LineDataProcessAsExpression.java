package com.jobquest.process;

import javax.script.*;

public class LineDataProcessAsExpression implements LineDataProcess {
/**
 * Processes a line of data from the large data file
 * by following a specific algorithm.
 * @param line - string to be used by the computation process
 * @return String value of the result
 *
 * treat single line as an expression that
 * needs to be evaluated and convert result to 
 * contain <input string> = <output value>
 */

 public String processLine(String line){
     ScriptEngineManager manager = new ScriptEngineManager();
     ScriptEngine engine = manager.getEngineByName("js");    

     try {
       Object result = engine.eval(line);
       return line + " = "+result.toString();
     }catch(ScriptException scx) {
        System.out.println("Evaluating "+line+" resulted in exception");
        return "";
     }finally {
       manager = null; // no longer need these
       engine = null;  // no longer need these 
     }
 }

}

