package com.jobquest.process;

import java.util.concurrent.*;

import java.text.SimpleDateFormat;
import java.util.Date;
 
import com.jobquest.io.WriteFileWithMappedByteBuffer;

public class MonitorRunningThread implements Runnable {

    private WriteFileWithMappedByteBuffer writerThread;

    // The handle to executor is required in order to shutdown
    // when the reading process is done
    private ThreadPoolExecutor executor;
     
    public MonitorRunningThread(ThreadPoolExecutor exct) {
       this.executor = exct;
    }
     
    public void setQueue(WriteFileWithMappedByteBuffer wrthread){
       this.writerThread = wrthread;
    }

    @Override
    public void run() {
       boolean input_completed = false;
       int i=0;
 
       while (!input_completed){
         if ( writerThread.isalive == false )
          input_completed = true;
         System.out.println("Monitoring all reading threads - are they complete "+input_completed);
         // Even if the threads have been completed reading, give some time
         // for any un-processed thread to finish up and then shutdown
         try {
           Thread.sleep(5000); // sleep for 5 seconds
           if ( input_completed ){
             executor.shutdownNow();
             SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss a zzz");
             System.out.println("Ended ... @ "+ft.format(new Date()));
           }
         }catch(InterruptedException iex) { }
       }
    }
}
