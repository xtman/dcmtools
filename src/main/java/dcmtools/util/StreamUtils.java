package dcmtools.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

	public static void skipInsistently(InputStream in, long n) throws IOException {
		long remaining = n;
		while (remaining > 0) {
			long skipped = in.skip(remaining);
			if (skipped > 0) {
				remaining -= skipped;
			} else if (skipped == 0) {
				if (in.read() == -1) {
					throw new EOFException();
				} else {
					n--;
				}
			}
		}
	}
}
