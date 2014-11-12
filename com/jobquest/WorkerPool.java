package com.jobquest;

import java.util.*;
import java.util.concurrent.*;

import java.io.*;
import java.nio.channels.FileChannel;

import com.jobquest.io.ReadFileWithMappedByteBuffer;
import com.jobquest.io.WriteFileWithMappedByteBuffer;
import com.jobquest.RejectedExecutionHandlerImpl;

import com.jobquest.process.InputQueueProcess;
import com.jobquest.process.MonitorRunningThread;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkerPool {

   public static final int MAX_THREADS = 4;

   public static void main(String[] args){
       int numargs = args.length;
       boolean hasinp  = false;
       boolean hasoutp = false;
       File f;

       // Assuming args[0] has input filename and args[1] has output-path.
       if ( numargs != 2 ){
           System.out.println("java com.jobquest.WorkerPool <input-file-path> <output-directory-path>");
       } else {
           if ( new File(args[0]).exists() )
               hasinp = true;
           else 
               System.out.println("The file "+args[0]+" does not exist");

           if ( new File(args[1]).exists() )
               hasoutp = true;
           else
               System.out.println("The output file path "+args[1]+" does not exist");

           if ( hasinp && hasoutp )
                runworker(args[0], args[1]);
       }
   }

   public static void runworker(String filename, String outputFilePath){
      //RejectedExecutionHandler implementation
      RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();

      //Get the ThreadFactory implementation to use
      ThreadFactory threadFactory = Executors.defaultThreadFactory();
    
      //creating the ThreadPoolExecutor (which is resposible for executing threads)
      ThreadPoolExecutor executorPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, MAX_THREADS, TimeUnit.SECONDS, 
            new LinkedBlockingQueue<Runnable>(MAX_THREADS), threadFactory, rejectionHandler);

      // This input queue is handling the lines read from large file
      BlockingQueue<String> input_queue = new LinkedBlockingQueue<String>();
      BlockingQueue<String> output_queue = new LinkedBlockingQueue<String>();

      // The file reads can only happen sequentially - but using Java NIO API's one can
      // read files much faster using buffers
      ReadFileWithMappedByteBuffer rfbuffer = new ReadFileWithMappedByteBuffer(filename, input_queue, output_queue); 
      WriteFileWithMappedByteBuffer writer = new WriteFileWithMappedByteBuffer(outputFilePath+"/results.txt", output_queue); 

         SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss a zzz");
         System.out.println("Started ... @ "+ft.format(new Date()));

         MonitorRunningThread monitor = new MonitorRunningThread(executorPool);
         InputQueueProcess  inpQ[] = new InputQueueProcess[MAX_THREADS];
         
         //submit work to the thread pool
         for(int i=0; i<MAX_THREADS; i++){
               inpQ[i] = new InputQueueProcess(input_queue, output_queue);
               executorPool.execute(inpQ[i]);
               if ( i == 1 ){
                 rfbuffer.run();
                 writer.run();
               }
         }

         // monitor will keep track when the job ends and exit the main process
         monitor.setQueue(writer);
         monitor.run();
   }

}
