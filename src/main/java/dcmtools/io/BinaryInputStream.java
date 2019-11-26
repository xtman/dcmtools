package dcmtools.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class BinaryInputStream extends FilterInputStream implements DataInput {

    private DataInputStream dis;
    private byte[] buffer;
    private ByteOrder order;

    public BinaryInputStream(InputStream in, ByteOrder order, boolean closeable) {
        super(closeable ? new DataInputStream(in) : new DataInputStream(in) {
            public void close() {
                // not to close
            }
        });
        this.dis = (DataInputStream) this.in;
        this.buffer = new byte[8];
        this.order = order;
    }

    public BinaryInputStream(InputStream in, ByteOrder order) {
        this(in, order, true);
    }

    public BinaryInputStream(InputStream in) {
        this(in, ByteOrder.BIG_ENDIAN, true);
    }

    public ByteOrder byteOrder() {
        return this.order;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        this.dis.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        this.dis.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return this.dis.skipBytes(n);
    }

    public void skipFully(int n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            long skipped = skip(remaining);
            if (skipped <= 0) {
                throw new IOException("Skip failed with " + remaining + " bytes remaining to be skipped, wanted " + n);
            }
            remaining -= skipped;
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        return this.dis.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return this.dis.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return this.dis.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        readFully(this.buffer, 0, 2);
        if (this.order == ByteOrder.BIG_ENDIAN) {
            return (short) ((this.buffer[0] << 8) + (this.buffer[1] << 0));
        } else {
            return (short) ((this.buffer[1] << 8) + (this.buffer[0] << 0));
        }
    }

    @Override
    public int readUnsignedShort() throws IOException {
        readFully(this.buffer, 0, 2);
        if (this.order == ByteOrder.BIG_ENDIAN) {
            return ((this.buffer[0] << 8) + (this.buffer[1] << 0));
        } else {
            return ((this.buffer[1] << 8) + (this.buffer[0] << 0));
        }
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public int readInt() throws IOException {
        readFully(this.buffer, 0, 4);
        if (this.order == ByteOrder.BIG_ENDIAN) {
            return (((this.buffer[0] & 255) << 24) + ((this.buffer[1] & 255) << 16) + ((this.buffer[2] & 255) << 8)
                    + ((this.buffer[3] & 255) << 0));
        } else {
            return (((this.buffer[3] & 255) << 24) + ((this.buffer[2] & 255) << 16) + ((this.buffer[1] & 255) << 8)
                    + ((this.buffer[0] & 255) << 0));
        }
    }

    @Override
    public long readLong() throws IOException {
        readFully(this.buffer, 0, 8);
        if (this.order == ByteOrder.BIG_ENDIAN) {
            // @formatter:off
            return (((long) this.buffer[0] << 56) 
                    + ((long) (this.buffer[1] & 255) << 48)
                    + ((long) (this.buffer[2] & 255) << 40)
                    + ((long) (this.buffer[3] & 255) << 32)
                    + ((long) (this.buffer[4] & 255) << 24)
                    + ((this.buffer[5] & 255) << 16)
                    + ((this.buffer[6] & 255) << 8)
                    + ((this.buffer[7] & 255) << 0));
            // @formatter:on
        } else {
            // @formatter:off
            return (((long) this.buffer[7] << 56) 
                    + ((long) (this.buffer[6] & 255) << 48)
                    + ((long) (this.buffer[5] & 255) << 40)
                    + ((long) (this.buffer[4] & 255) << 32)
                    + ((long) (this.buffer[3] & 255) << 24)
                    + ((this.buffer[2] & 255) << 16)
                    + ((this.buffer[1] & 255) << 8)
                    + ((this.buffer[0] & 255) << 0));
            // @formatter:on
        }
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @SuppressWarnings("deprecation")
    @Override
    public String readLine() throws IOException {
        return this.dis.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return this.dis.readUTF();
    }

}
