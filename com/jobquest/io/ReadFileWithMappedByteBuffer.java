package com.jobquest.io;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
 
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.locks.ReentrantLock;


public class ReadFileWithMappedByteBuffer implements Runnable
{

    // Input file name - which is expected to be large size
    private String input_filename;

    // Using RandomAccessFile to read large file
    private RandomAccessFile aFile;

    private final ReentrantLock lock = new ReentrantLock();

    // Use FileChannel and buffer to read large file
    private FileChannel inChannel;

    private int linecount=1;

    // This queue holds the line read from file - which is processed by threads to evaluate
    private BlockingQueue<String> input_queue;
    // This queue holds the expression and their result - which is appended by thread into a file
    private BlockingQueue<String> output_queue;

    public ReadFileWithMappedByteBuffer(String fname, BlockingQueue<String> inp_queue,
                        BlockingQueue<String> outp_queue){
        this.input_filename = fname;
        this.input_queue    = inp_queue;
        this.output_queue   = outp_queue;
    }

    private void readBufferAndQueue(byte[] buffer, boolean multiple_buffers){
       BufferedReader in;
       String line=null;
       try {
         in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer)));
         lock.lock();
         try {
           for (line = in.readLine(); line != null; line = in.readLine()) {
             input_queue.put(line);
             //System.out.println(linecount +" ==>"+line);
             //linecount++;
             //if ( !multiple_buffers ){
             // Thread.sleep(10);
             //}
             line = null;
           }
         }finally {
           lock.unlock();
         }
       }catch(IOException ioex) {
          // Ideally if we use log4j, we can log error messages to keep track of 
          // the problems. Right here, I am just using basic messagedisplay
          System.out.println("IOEX:: "+ioex.getMessage());
       }catch(InterruptedException itx) {
          System.out.println("ITX::" + itx.getMessage());
       }
    }

    public void run() {
       MappedByteBuffer buffer = null;
       try {
        aFile = new RandomAccessFile(input_filename, "r");
        inChannel = aFile.getChannel();
        int maxsize = Integer.MAX_VALUE;

        long remaining_size;
        long channel_size = inChannel.size();
        System.out.println("channel size "+channel_size);
        long num_buffers = channel_size / maxsize;
        if ( maxsize * num_buffers < channel_size ) num_buffers++;

        // There might be memory limitation that might lead to
        // failure - and therefore we need to consider the situation
        // where the channel size is too large - in which case break down
        // the reading process into chunks
        if ( num_buffers == 1 ){
          buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, channel_size);
          byte[] charbuffer = new byte[(int)channel_size];
          buffer.get(charbuffer);
          System.out.println("In reading thread");
          readBufferAndQueue(charbuffer, false);
          try {
            // This marks the end of process
            input_queue.put("DONE");
          }catch(InterruptedException iex) { 
             // logging should be done
             // for now I am just doing Sysout
             System.out.println("File reading Process is interrupted");
          }
          buffer.clear(); 
        } else {
          maxsize = 65536;
          channel_size = inChannel.size();
          num_buffers = channel_size / maxsize;
          System.out.println("num buffers = "+num_buffers);
          
          remaining_size = maxsize;
          while ( remaining_size > 0 ){
            buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, remaining_size);
            byte[] charbuffer = new byte[(int)remaining_size];
            buffer.get(charbuffer);
            readBufferAndQueue(charbuffer, true);
            buffer.clear(); 
            charbuffer = null;
            channel_size = channel_size - remaining_size;
            if ( channel_size > maxsize )
                remaining_size = maxsize;
            else
                remaining_size = channel_size;
            charbuffer = null;
          }
        }
       }catch(IOException ioex) { 
       }finally {
         try{
          // This marks the end of process
          Thread.sleep(2000);
          input_queue.put("DONE");
          inChannel.close();
          aFile.close();
         }catch(IOException ioex) { 
         }catch(InterruptedException iex) { 
             // logging should be done
             // for now I am just doing Sysout
             System.out.println("File reading Process is interrupted");
          }
       }
    }
}
