package dcmtools.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {

    private long _count;
    private long _mark = -1;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    /**
     * Number of bytes have been read.
     * 
     * @return
     */
    public long count() {
        return _count;
    }

    @Override
    public int read() throws IOException {
        int result = in.read();
        if (result != -1) {
            _count++;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            _count += result;
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = in.skip(n);
        _count += result;
        return result;
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        _mark = _count;
        // it's okay to mark even if mark isn't supported, as reset won't work
    }

    @Override
    public synchronized void reset() throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Mark not supported");
        }
        if (_mark == -1) {
            throw new IOException("Mark not set");
        }
        in.reset();
        _count = _mark;
    }
}