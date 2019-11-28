package dcmtools.io;

import java.io.IOException;
import java.io.InputStream;

public class SizedInputStream extends CountingInputStream {

    private final long _size;
    private final boolean _closeable;

    public SizedInputStream(InputStream in, long size, boolean closeable) {
        super(in);
        _size = size;
        _closeable = closeable;
    }

    public SizedInputStream(InputStream in, long size) {
        this(in, size, false);
    }

    @Override
    public int read() throws IOException {
        if (count() >= _size) {
            return -1;
        }
        return super.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (count() >= _size) {
            return -1;
        }
        if (_size > 0 && count() + len > _size) {
            len = (int) (_size - count());
        }
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        if (count() >= _size) {
            return 0;
        }
        if (_size > 0 && count() + n > _size) {
            n = _size - count();
        }
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        if (remaining() == 0) {
            return 0;
        }
        int a = in.available();
        if (_size >= 0 && a > _size) {
            return (int) _size;
        } else {
            return a;
        }
    }

    public long remaining() {
        if (_size < 0) {
            return -1;
        }
        if (_size == 0) {
            return 0;
        }
        return _size - count();
    }

    @Override
    public void close() throws IOException {
        if (_closeable) {
            in.close();
        }
    }
}