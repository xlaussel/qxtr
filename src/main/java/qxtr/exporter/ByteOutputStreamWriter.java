package qxtr.exporter;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteOutputStreamWriter implements Flushable {

    public ByteOutputStreamWriter(OutputStream stream) {
        this(stream,2048*32);
    } //TODO determine the default size

    public ByteOutputStreamWriter(OutputStream stream,int bufferSize) {
        this.stream = stream;
        buffer=ByteBuffer.allocate(bufferSize);
    }

    private final OutputStream stream;
    private final ByteBuffer buffer;

    public ByteOutputStreamWriter writeInt(int val) throws IOException {
        if (buffer.remaining()<4) this.flush();
        buffer.putInt(val);
        return this;
    }

    public ByteOutputStreamWriter writeInts(int[] val) throws IOException {
        var intBuffer=buffer.asIntBuffer();
        int nIntRemaining=val.length;
        do {
            if (intBuffer.remaining()==0) {
                flush();
                intBuffer=buffer.asIntBuffer();
            }
            int toWrite=Integer.min(intBuffer.remaining(),nIntRemaining);
            intBuffer.put(val,val.length-nIntRemaining,toWrite);
            nIntRemaining-=toWrite;
        } while (nIntRemaining!=0);
        return this;
    }

    public ByteOutputStreamWriter writeLong(long val) throws IOException {
        if (buffer.remaining()<8) this.flush();
        buffer.putLong(val);
        return this;
    }

    public ByteOutputStreamWriter writeShort(short val) throws IOException {
        if (buffer.remaining()<2) this.flush();
        buffer.putShort(val);
        return this;
    }

    public ByteOutputStreamWriter writeByte(byte val) throws IOException {
        if (buffer.remaining()<1) this.flush();
        buffer.put(val);
        return this;
    }

    public ByteOutputStreamWriter writeBoolean(boolean val) throws IOException {
        if (buffer.remaining()<1) this.flush();
        buffer.put((byte)(val?1:0));
        return this;
    }

    public ByteOutputStreamWriter writeBytes(byte[] bytes) throws IOException {
        int nBytesRemaining=bytes.length;
        do {
            if (buffer.remaining()==0) flush();
            int toWrite=Integer.min(buffer.remaining(),nBytesRemaining);
            buffer.put(bytes,bytes.length-nBytesRemaining,toWrite);
            nBytesRemaining-=toWrite;
        } while (nBytesRemaining!=0);
        return this;
    }

    public ByteOutputStreamWriter writeString(String val) throws IOException {
        return writeBytes(val.getBytes(StandardCharsets.UTF_16));
    }

    public void flush() throws IOException {
        stream.write(buffer.array(),buffer.arrayOffset(),buffer.position());
        buffer.rewind();
    }

}
