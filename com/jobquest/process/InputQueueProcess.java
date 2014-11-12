package com.jobquest.process;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
 
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//import javax.script.*;

import java.util.concurrent.locks.ReentrantLock;

public class InputQueueProcess implements Runnable {

    private BlockingQueue<String> input_queue;
    private BlockingQueue<String> output_queue;
  
    public boolean isalive = true;

    //private ScriptEngine engine;        

    //private final ReentrantLock lock = new ReentrantLock();

    public InputQueueProcess(BlockingQueue<String> inp_queue, 
                             BlockingQueue<String> out_queue){
        this.input_queue = inp_queue;
        this.output_queue = out_queue;

        //ScriptEngineManager manager = new ScriptEngineManager();
        //engine = manager.getEngineByName("js");        
    }

    @Override
    public void run(){
         boolean hasdata=false;
         String  lineRead;
       
         try {
             while(isalive){
               hasdata = false;
               //lock.lock();
               try{
               if ( input_queue.size() > 0 ){
                 hasdata = true;
                 lineRead = input_queue.take();
                 if ( lineRead.equals("DONE") ){
                    isalive = false;
                    output_queue.put("DONE");                     
                 } else {
                     Context context = new Context(new LineDataProcessAsExpression());
                     output_queue.put(context.processLine(lineRead)); 
                     context = null;
                    //try {
                      //Object result = engine.eval(lineRead);
                      //output_queue.put(lineRead + " = "+result.toString());
                    //}catch(ScriptException scx) {
                    //   System.out.println("Evaluating "+lineRead+" resulted in exception"); 
                    //}
                 }
               } 
              }finally{
                 //lock.unlock();
                 if ( !hasdata ){
                   Thread.sleep(500);
                 }
              }
             }
         }catch(InterruptedException itx) { 
            System.out.println("Interrupred Writing Thread"); 
         } 
    }


}
