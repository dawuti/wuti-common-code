package com.wuti.common.stream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BufferedMultipleOutputStream extends BufferedOutputStream {

    private IStreamHandler iStreamHandler;
    // 即会有多少个数据流
    private int pushCount=0;
    // 缓存１兆　　每个输出流都这么大，写满一个　在新建一个
    public static int bufferSize = 1024 * 1024;

    public BufferedMultipleOutputStream(OutputStream out, IStreamHandler iStreamHandler) {
        super(out, bufferSize);
        this.iStreamHandler = iStreamHandler;

    }
    private OutputStream nextOutputStream() throws IOException {
        out.flush();
        OutputStream outputStream = null;
        Class class_ = out.getClass();
        try {
            iStreamHandler.handler(out,pushCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pushCount++;
        try {
            System.out.println("class_=" + class_);
            outputStream = (OutputStream) class_.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    /**
     * Flush the internal buffer
     */
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }


    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param b the byte to be written.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void write(int b) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
            out = nextOutputStream();
        }
        buf[count++] = (byte) b;
    }


    public synchronized void write(byte b[], int off, int len) throws IOException {

        int finalOffset = 0;
        while (len > finalOffset) {
            int offset =Math.min (len-off,bufferSize - count);
            System.arraycopy(b, off, buf, count, offset);
            finalOffset = finalOffset + offset;
            off = off + offset;
            count= count+offset;
            if(count == bufferSize){
                flushBuffer();
                out = nextOutputStream();
            }
        }
    }


    public synchronized void flush() throws IOException {
       //正常读写

    }

    public synchronized void lastFlush() throws Exception {
        // 最后处理
        flushBuffer(); // 最后的缓存写入流
        out.flush();
        iStreamHandler.handler(out,pushCount);

    }

}
