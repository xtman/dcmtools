package dcmtools.util;

public class ObjectUtils {

	public static <T> boolean equals(T a, T b) {
		if (a != null && b != null) {
			return a.equals(b);
		}
		if (a == null && b == null) {
			return true;
		}
		return false;
	}

	public static <T extends Comparable<T>> int compareTo(T a, T b) {
		if (a == null || b == null) {
			if (a != null && b == null) {
				return 1;
			} else if (a == null && b != null) {
				return -1;
			} else {
				// a == null && b == null
				return 0;
			}
		} else {
			return a.compareTo(b);
		}
	}

}
