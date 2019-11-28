package dcmtools.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteOrder;

public class BinaryInputStream extends CountingInputStream implements DataInput {

    private boolean _closeable;
    private byte[] _buffer;
    private ByteOrder _order;

    public BinaryInputStream(InputStream in, ByteOrder order, boolean closeable) {
        super(in);
        _buffer = new byte[8];
        _order = order;
    }

    public BinaryInputStream(InputStream in, ByteOrder order) {
        this(in, order, true);
    }

    public BinaryInputStream(InputStream in) {
        this(in, ByteOrder.BIG_ENDIAN, true);
    }

    public ByteOrder byteOrder() {
        return _order;
    }

    @Override
    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public final void readFully(byte[] b, int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = super.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    @Override
    public final int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;
        while ((total < n) && ((cur = (int) super.skip(n - total)) > 0)) {
            total += cur;
        }
        return total;
    }

    public final void skipFully(int n) throws IOException {
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
    public final boolean readBoolean() throws IOException {
        int ch = super.read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    @Override
    public final byte readByte() throws IOException {
        int ch = super.read();
        if (ch < 0)
            throw new EOFException();
        return (byte) (ch);
    }

    @Override
    public final int readUnsignedByte() throws IOException {
        int ch = super.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    @Override
    public short readShort() throws IOException {
        readFully(_buffer, 0, 2);
        if (_order == ByteOrder.BIG_ENDIAN) {
            return (short) ((_buffer[0] << 8) + (_buffer[1] << 0));
        } else {
            return (short) ((_buffer[1] << 8) + (_buffer[0] << 0));
        }
    }

    @Override
    public int readUnsignedShort() throws IOException {
        readFully(_buffer, 0, 2);
        if (_order == ByteOrder.BIG_ENDIAN) {
            return ((_buffer[0] << 8) + (_buffer[1] << 0));
        } else {
            return ((_buffer[1] << 8) + (_buffer[0] << 0));
        }
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public int readInt() throws IOException {
        readFully(_buffer, 0, 4);
        if (_order == ByteOrder.BIG_ENDIAN) {
            return (((_buffer[0] & 255) << 24) + ((_buffer[1] & 255) << 16) + ((_buffer[2] & 255) << 8)
                    + ((_buffer[3] & 255) << 0));
        } else {
            return (((_buffer[3] & 255) << 24) + ((_buffer[2] & 255) << 16) + ((_buffer[1] & 255) << 8)
                    + ((_buffer[0] & 255) << 0));
        }
    }

    @Override
    public long readLong() throws IOException {
        readFully(_buffer, 0, 8);
        if (_order == ByteOrder.BIG_ENDIAN) {
            // @formatter:off
            return (((long) _buffer[0] << 56) 
                    + ((long) (_buffer[1] & 255) << 48)
                    + ((long) (_buffer[2] & 255) << 40)
                    + ((long) (_buffer[3] & 255) << 32)
                    + ((long) (_buffer[4] & 255) << 24)
                    + ((_buffer[5] & 255) << 16)
                    + ((_buffer[6] & 255) << 8)
                    + ((_buffer[7] & 255) << 0));
            // @formatter:on
        } else {
            // @formatter:off
            return (((long) _buffer[7] << 56) 
                    + ((long) (_buffer[6] & 255) << 48)
                    + ((long) (_buffer[5] & 255) << 40)
                    + ((long) (_buffer[4] & 255) << 32)
                    + ((long) (_buffer[3] & 255) << 24)
                    + ((_buffer[2] & 255) << 16)
                    + ((_buffer[1] & 255) << 8)
                    + ((_buffer[0] & 255) << 0));
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

    private char lineBuffer[];

    @Override
    @Deprecated
    public String readLine() throws IOException {
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int room = buf.length;
        int offset = 0;
        int c;

        loop: while (true) {
            switch (c = super.read()) {
            case -1:
            case '\n':
                break loop;

            case '\r':
                int c2 = super.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream) in).unread(c2);
                }
                break loop;

            default:
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    @Override
    public final String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    @Override
    public void close() throws IOException {
        if (_closeable) {
            super.close();
        }
    }

}
