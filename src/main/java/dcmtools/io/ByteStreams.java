package dcmtools.io;

import java.io.IOException;
import java.io.InputStream;

public class ByteStreams {

    public static final int BUFFER_SIZE = 8192;

    public static long exhaust(InputStream in) throws IOException {
        long total = 0;
        long read;
        byte[] buf = new byte[BUFFER_SIZE];
        while ((read = in.read(buf)) != -1) {
            total += read;
        }
        return total;
    }

}
