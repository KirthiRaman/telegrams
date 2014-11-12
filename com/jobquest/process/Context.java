package com.jobquest.process;

public class Context {

 private LineDataProcess lineProcess;

 public Context(LineDataProcess lineProcess){ // Dependency Injection
     this.lineProcess = lineProcess;
 }

 public String processLine(String line){
     return lineProcess.processLine(line);
 }

}

