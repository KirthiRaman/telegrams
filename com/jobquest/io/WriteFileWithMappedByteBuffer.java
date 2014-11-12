package com.jobquest.io;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
 
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.locks.ReentrantLock;

public class WriteFileWithMappedByteBuffer implements Runnable
{

    public static final String NEW_LINE = System.getProperty("line.separator").toString();

    // Using RandomAccessFile to read large file
    private RandomAccessFile aFile;

    //private final ReentrantLock lock = new ReentrantLock();

    // Use FileChannel and buffer to read large file
    private FileChannel channel;

    public boolean isalive = true;

    private int linecount = 1;

    // This queue holds the expression and their result - which is appended by thread into a file
    private BlockingQueue<String> output_queue;

    public WriteFileWithMappedByteBuffer(String output_filename, BlockingQueue<String> outp_queue){
        this.output_queue   = outp_queue;
        try {
          channel = new FileOutputStream(new File(output_filename)).getChannel();
        }catch(FileNotFoundException fnf) {
        }
    }

    public void run() {
      String line="";
      boolean hasdata = false;
      try {
        while ( isalive ){
         hasdata = false;
         //lock.lock();
         if ( output_queue.size() > 0 ){
          hasdata = true;
          try{
            line = output_queue.take();
            if ( !line.equals("DONE") ){
               //System.out.println(linecount +" ==>" + line);
               //linecount++;
               channel.write(ByteBuffer.wrap(line.getBytes()));
               channel.write(ByteBuffer.wrap(NEW_LINE.getBytes()));
            } else {
               // Synchronizing read threads with write threads was a bit tricky
               // If I dont have this, it skips 3 lines of input
               while ( !output_queue.isEmpty() ){
                 line = output_queue.take();
                 if ( !line.equals("DONE") ){
                   System.out.println("Remaining in qeue are shown here ...");
                   System.out.println(linecount +" ==>" + line);
                   linecount++;
                   channel.write(ByteBuffer.wrap(line.getBytes()));
                   channel.write(ByteBuffer.wrap(NEW_LINE.getBytes()));
                 }
               }
               isalive = false;
            }
          }finally {
           //lock.unlock();
          }
         }
         if ( !hasdata ){
            Thread.sleep(1000);
         }
         line = null;
        }
       }catch(IOException ioex) { 
          ioex.printStackTrace();
       }catch(InterruptedException iex) { 
          iex.printStackTrace();
       }finally {
         try{
          // This marks the end of process
          channel.close();
         }catch(IOException ioex) { }
       }
    }
}
