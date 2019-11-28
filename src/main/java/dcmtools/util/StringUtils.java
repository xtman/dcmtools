package dcmtools.util;

public class StringUtils {

    public static String repeat(char c, int n) {
        return new String(new char[n]).replace("\0", Character.toString(c));
    }

    public static String trimLeft(String s, char c) {
        while (s.startsWith(String.valueOf(c))) {
            s = s.substring(1);
        }
        return s;
    }

    public static String trimRight(String s, char c) {
        while (s.endsWith(String.valueOf(c))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String trim(String s, char c) {
        return trimRight(trimLeft(s, c), c);
    }

}
